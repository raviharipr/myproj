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

The application is configured using the `config.properties` file located in `src/main/resources`.

1.  **Create the configuration file:**
    - If it doesn't exist, create a file named `config.properties` inside the `src/main/resources` directory.

2.  **Edit the configuration file:**
    - Open the `config.properties` file and add the following properties:

    ```properties
    # MongoDB Configuration
    mongo.uri=mongodb://localhost:27017
    mongo.database=stock_db
    mongo.stocks_list_collection=stocks_list

    # Alpha Vantage API Key
    alpha.vantage.api.key=YOUR_API_KEY
    ```

3.  **Replace `YOUR_API_KEY` with your actual Alpha Vantage API key.**

4.  **Stock Tickers:**
    - The application retrieves the list of stock tickers from the collection specified by `mongo.stocks_list_collection` in the database specified by `mongo.database`.
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

### Resetting the Data

To drop all collections except for the `stocks_list` collection, run the following command:

```bash
mvn exec:java -Dexec.mainClass="com.stockmanager.App" -Dexec.args="reset"
```

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
