# Stock Data Management System

## Description

This Java application retrieves, stores, and analyzes daily stock market data. It fetches data from the Alpha Vantage API, stores it in a MongoDB database, and calculates moving averages.

## Prerequisites

- Java 11 or higher
- Maven
- MongoDB instance running

## Setup

1.  **Clone the repository.**
2.  **Navigate to the `stock-data-manager` directory.**
3.  **Install dependencies using Maven:**
    ```bash
    mvn clean install
    ```

## Configuration

1.  **API Key:**
    - Open the `src/main/java/com/stockmanager/App.java` file.
    - Replace `"YOUR_API_KEY"` with your actual Alpha Vantage API key.

2.  **MongoDB:**
    - The application connects to a MongoDB instance at `mongodb://localhost:27017`.
    - The database used is `stock_db`.
    - Ensure your MongoDB server is running before starting the application.

3.  **Stock Tickers:**
    - The application retrieves the list of stock tickers from the `stocks_list` collection in the `stock_db` database.
    - Each document in this collection should have a `ticker` field with the stock symbol (e.g., "AAPL").
    - **Example document:**
      ```json
      {
        "ticker": "AAPL"
      }
      ```
    - You need to populate this collection with the tickers you want to track.

## Running the Application

After setting up and configuring the project, you can run the application using Maven:

```bash
mvn exec:java -Dexec.mainClass="com.stockmanager.App"
```

The application will then:
1.  Connect to MongoDB.
2.  Read the stock tickers from the `stocks_list` collection.
3.  Fetch daily price data for each ticker from Alpha Vantage.
4.  Store the daily data in a separate collection for each ticker.
5.  Calculate and store the 20-day and 50-day Simple Moving Averages (SMA) for each day.

## Database Schema

### `stocks_list` Collection

-   `ticker` (String): The stock ticker symbol (e.g., "AAPL", "MSFT").

### Ticker-Specific Collections (e.g., `AAPL`)

-   `date` (String): The date of the data (YYYY-MM-DD).
-   `open` (Double): The opening price.
-   `close` (Double): The closing price.
-   `high` (Double): The highest price.
-   `low` (Double): The lowest price.
-   `20_day_sma` (Double): The 20-day Simple Moving Average (optional).
-   `50_day_sma` (Double): The 50-day Simple Moving Average (optional).
