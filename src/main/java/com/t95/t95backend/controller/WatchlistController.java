package com.t95.t95backend.controller;

import java.util.List;
import java.util.Optional;

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
import com.t95.t95backend.entity.Watchlist;
import com.t95.t95backend.service.WatchlistService;

@RestController
@RequestMapping(path = "api/watchlists")
public class WatchlistController {

    private final WatchlistService watchlistService;

    @Autowired
    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    //get all watchLists
    @GetMapping(path = "/users")
    public ResponseEntity getWatchlists() {    	
    	List<Watchlist> watchlist = watchlistService.getWatchlists();
    	return ResponseEntity.status(HttpStatus.OK).body(watchlist);
    }       

    //get all watchLists by user
    @GetMapping(path = "/users/{userId}")
    public ResponseEntity getWatchlistsByUserId(@PathVariable("userId") Long userId) {
    	List<Watchlist> watchlists = watchlistService.getWatchlistsByUserId(userId);
    	
    	if(watchlists.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND);    	
    	return ResponseEntity.status(HttpStatus.OK).body(watchlists);
    }

    //add new watchList by user
    @PostMapping(path = "/users/{userId}")
    public ResponseEntity addNewWatchlist(
            @PathVariable("userId") Long userId,
            @RequestParam(required = true) String name
            ){
    	try {
    		watchlistService.addWatchlist(userId, name);
    		return ResponseEntity.status(HttpStatus.OK).body("{\"message\":\"success added watchlist.\"}");

    	} catch (Exception e) {
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    	}    	
        
    }

    //delete watchList by user
    @DeleteMapping(path = "/users/{userId}")
    public ResponseEntity deleteWatchlist(
            @PathVariable("userId") Long userId,
            @RequestParam(required = true) String name
    ){
    	try {
    		watchlistService.deleteWatchlist(userId, name);
    		return ResponseEntity.status(HttpStatus.NO_CONTENT).body("{\"message\":\"success deleted watchlist.\"}");
    	} catch (Exception e) {
    		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
    	}
    }
        
}
