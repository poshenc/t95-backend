package com.t95.t95backend.schedulingTasks;

import com.t95.t95backend.dto.YahooFinanceDTO;
import com.t95.t95backend.entity.Stock;
import com.t95.t95backend.repository.StockRepository;
import com.t95.t95backend.service.StockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.*;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;

@Component
public class RetrieveYahooFinance {

    private static final Logger logger = LoggerFactory.getLogger(RetrieveYahooFinance.class);
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Long refreshPeriodInSeconds = 60L;
    @Value("${spring.profiles.active:Unknown}")
    private String activeProfile;

    private StockService stockService;

    public RetrieveYahooFinance(StockService stockService, Environment env) {
        this.stockService = stockService;
    }

    public YahooFinanceDTO findStock(final String ticker) {
        try {
            String endpoint = "https://query1.finance.yahoo.com/v8/finance/chart/" + ticker;
            RestTemplate restTemplate = new RestTemplate();
            YahooFinanceDTO stock = restTemplate.getForObject(endpoint, YahooFinanceDTO.class);
            return stock;
        } catch (Exception e) {
            logger.error("******YFiannce****** Failed to fetch ticker:" + ticker + " from Yahoo Finance at: " + LocalTime.now());
            logger.error("******YFiannce****** Error message:" + e);
        }
        return null;
    }

    public List<YahooFinanceDTO> findStocks(final List<String> tickers) {
        return tickers.stream().map(this::findStock).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public Double findPrice(final YahooFinanceDTO stock) {
        double price = stock.getChart().getResult().get(0).getMeta().getRegularMarketPrice();
        return price;
    }

    public Double findPreviousClose(final YahooFinanceDTO stock) {
        double previousClose = stock.getChart().getResult().get(0).getMeta().getPreviousClose();
        return previousClose;
    }


    //scheduled executor service with one thread pool
    @EventListener(ApplicationReadyEvent.class)
    public void shouldRefreshStockTableEvery100Minutes() throws InterruptedException {
        if(activeProfile.equals("prod")) {
            List<String> stocksToRefresh = stockService.getSymbolsList();
            scheduler.scheduleAtFixedRate(() ->
                    stocksToRefresh.forEach((ticker) -> {
                        try {
                            YahooFinanceDTO stock = findStock(ticker);
                            if (stock != null) {
                                double currentPrice = findPrice(stock);
                                double previousClose = findPreviousClose(stock);
//                            System.out.println("Fetched...:" + ticker + "price: " + currentPrice + " from Yahoo Finance at: " + LocalTime.now());
                                updateStock(ticker, currentPrice, previousClose);
                            }
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }), 0, refreshPeriodInSeconds, SECONDS);
        }

    }



    @Transactional
    public void updateStock(String symbol, double currentPrice, double previousClose) {
        Optional<Stock> optionalStock = stockService.findStockBySymbol(symbol);
        if(optionalStock.isEmpty() || !(currentPrice > 0) || !(previousClose > 0)) {
            logger.error("******YFiannce****** Something wrong with Yahoo Finance data, check updateStock()");
            return;
        }

        Stock stock = stockService.findById(optionalStock.get().getId())
                .orElseThrow(() -> new IllegalStateException("stock with id: " + optionalStock.get().getId() + " does not exist!"));

        String price = String.format( "%.2f", currentPrice );
        if(!Objects.equals(stock.getPrice(), price)) {
            //update price
            stock.setPrice(price);

            //update movementPoints
            String movementPoints = String.format( "%.2f", (currentPrice - previousClose) );
            Boolean isPositive = Character.isDigit(movementPoints.charAt(0));
            stock.setMovementPoints((isPositive ? "+" : "") + movementPoints);

            //update calcPercentage
            Double calcPercentage = ((currentPrice - previousClose) / previousClose) * 100;
            String movementPercentage = String.format( "%.2f", calcPercentage );
            stock.setMovementPercentage((isPositive ? "+" : "") + movementPercentage + "%");
//            System.out.println("Success updateStock:" + symbol + "price: " + currentPrice + " from Yahoo Finance at: " + LocalTime.now());
            stockService.updateStock(stock);
        }

    }
}
