package com.t95.t95backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.t95.t95backend.entity.Stock;
import com.t95.t95backend.service.StockService;

@RestController
@RequestMapping(path = "api/stocks")
public class StockController {

    private StockService stockService;

    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    //get all stocks names and id
    @GetMapping
    public List<Map> getStocksList() {
        return stockService.getStocksList();
    }

    //get a stock current price and movements
    @GetMapping(path = "{symbol}")
    public Optional<Stock> findStockBySymbol(@PathVariable("symbol") String symbol) {
    	Optional<Stock> stock = stockService.findStockBySymbol(symbol);
    	
    	if(stock.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);    	
    	return stock;
    }
        
    // ************************* stocks current price and movements in watchLists *******************************
    
    //get stocks current price and movements in a watchLists
    @GetMapping(path = "/watchlists/{watchlistId}")
    public ResponseEntity getWatchedStocksByWatchlistId(
    		@PathVariable("watchlistId") Long watchlistId) {
    	List<Stock> watchedStocks = stockService.getWatchedStockByWatchlistId(watchlistId);
    	
    	if(watchedStocks.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);    	
    	return ResponseEntity.status(HttpStatus.OK).body(watchedStocks);
//    	List<HashMap<String, String>> returnWatchlist = new ArrayList<HashMap<String, String>>();
//
//		for (Stock watchedStock : watchedStocks) {
//			HashMap<String, String> map = new HashMap<String, String>();
//			
//			map.put("name", watchedStock.getName());
//			map.put("symbol", watchedStock.getStockCode());
//			map.put("price", watchedStock.getPrice());
//			map.put("movementPoints", watchedStock.getMovementPoints());
//			map.put("movementPercentage", watchedStock.getMovementPercentage());
//			returnWatchlist.add(map);
//		}

    }
    
    //add stock to a watchList
    @Transactional
    @PostMapping(path = "/watchlists/{watchlistId}")
    public ResponseEntity addNewWatchedStock(
            @PathVariable("watchlistId") Long watchlistId,
            @RequestParam(required = true) Long stockId
            ){
    	try {
    		Boolean exists = stockService.findWatchedPair(watchlistId, stockId);
        	if(exists) { 
        		return ResponseEntity.status(HttpStatus.CONFLICT).body("Stock already exist in watchlist.");
        	} else {
        		stockService.addNewWatchedStock(watchlistId, stockId);
        		return ResponseEntity.status(HttpStatus.CREATED).body("Success added to watchlist.");
        	}
    		
    	} catch (Exception e) {
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    	}    	
    }
    
    
    
    //remove a stock from watchList
    @Transactional
    @DeleteMapping(path = "/watchlists/{watchlistId}")
    public ResponseEntity deleteWatchedStock(
    		@PathVariable("watchlistId") Long watchlistId,
            @RequestParam(required = true) Long stockId
    		) {
    	try {
    		Boolean exists = stockService.findWatchedPair(watchlistId, stockId);
    		if(!exists) {
    			return ResponseEntity.status(HttpStatus.CONFLICT).body("Stock does not exist in watchlist.");
    		} else {
    			stockService.deleteWatchedStock(watchlistId, stockId);
    			return ResponseEntity.status(HttpStatus.OK).body("Success delete watched stock.");   			
    		}
    		
    	} catch (Exception e) {
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    	}
    	
    }
}
