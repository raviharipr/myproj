package com.stockmanager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class App {

    private static final String API_KEY = "YOUR_API_KEY"; // Replace with your Alpha Vantage API key
    private static final String DATABASE_NAME = "stock_db";
    private static final String STOCKS_LIST_COLLECTION = "stocks_list";

    public static void main(String[] args) {
        if ("YOUR_API_KEY".equals(API_KEY)) {
            System.err.println("Error: Please replace 'YOUR_API_KEY' with your actual Alpha Vantage API key.");
            return;
        }

        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);

            List<String> stockTickers = getStockTickers(database);

            for (String ticker : stockTickers) {
                try {
                    fetchAndStoreStockData(database, ticker);
                    calculateAndStoreMovingAverages(database, ticker);
                } catch (IOException e) {
                    System.err.println("Error processing " + ticker + ": " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static List<String> getStockTickers(MongoDatabase database) {
        MongoCollection<Document> stocksListCollection = database.getCollection(STOCKS_LIST_COLLECTION);
        List<String> tickers = new ArrayList<>();
        for (Document doc : stocksListCollection.find()) {
            tickers.add(doc.getString("ticker"));
        }
        return tickers;
    }

    private static void fetchAndStoreStockData(MongoDatabase database, String ticker) throws IOException {
        System.out.println("Fetching data for " + ticker);
        OkHttpClient client = new OkHttpClient();
        String url = String.format("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=%s&outputsize=full&apikey=%s", ticker, API_KEY);

        Request request = new Request.Builder().url(url).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(response.body().string(), JsonObject.class);
            JsonObject timeSeries = jsonObject.getAsJsonObject("Time Series (Daily)");

            if (timeSeries == null) {
                System.err.println("Could not retrieve time series data for " + ticker + ". The API response might have changed, the ticker is invalid or you have reached the API limit.");
                return;
            }

            MongoCollection<Document> stockCollection = database.getCollection(ticker);

            for (Map.Entry<String, com.google.gson.JsonElement> entry : timeSeries.entrySet()) {
                String date = entry.getKey();
                JsonObject dayData = entry.getValue().getAsJsonObject();
                Document stockDocument = new Document("date", date)
                        .append("open", dayData.get("1. open").getAsDouble())
                        .append("close", dayData.get("2. close").getAsDouble())
                        .append("high", dayData.get("3. high").getAsDouble())
                        .append("low", dayData.get("4. low").getAsDouble());

                if (stockCollection.find(new Document("date", date)).first() == null) {
                    stockCollection.insertOne(stockDocument);
                }
            }
            System.out.println("Data for " + ticker + " stored.");
        }
    }

    private static void calculateAndStoreMovingAverages(MongoDatabase database, String ticker) {
        System.out.println("Calculating moving averages for " + ticker);
        MongoCollection<Document> stockCollection = database.getCollection(ticker);
        List<Document> dailyData = stockCollection.find().sort(Sorts.ascending("date")).into(new ArrayList<>());

        if (dailyData.size() < 20) {
            System.out.println("Not enough data to calculate 20-day SMA for " + ticker);
            return;
        }

        List<Double> closePrices = new ArrayList<>();
        for (Document doc : dailyData) {
            closePrices.add(doc.getDouble("close"));
        }

        for (int i = 0; i < dailyData.size(); i++) {
            Document currentDoc = dailyData.get(i);
            String date = currentDoc.getString("date");

            if (i >= 19) {
                double sma20 = calculateSMA(closePrices, i - 19, 20);
                stockCollection.updateOne(Filters.eq("date", date), Updates.set("20_day_sma", sma20));
            }

            if (i >= 49) {
                double sma50 = calculateSMA(closePrices, i - 49, 50);
                stockCollection.updateOne(Filters.eq("date", date), Updates.set("50_day_sma", sma50));
            }
        }
        System.out.println("Moving averages for " + ticker + " calculated and stored.");
    }

    private static double calculateSMA(List<Double> prices, int startIndex, int period) {
        double sum = 0;
        for (int i = 0; i < period; i++) {
            sum += prices.get(startIndex + i);
        }
        return sum / period;
    }
}
