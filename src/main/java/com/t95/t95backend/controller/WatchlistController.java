package com.t95.t95backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.t95.t95backend.entity.Watchlist;
import com.t95.t95backend.service.WatchlistService;

@RestController
@RequestMapping(path = "api/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    @Autowired
    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping
    public List<Watchlist> getWatchlists() {
        return watchlistService.getWatchlists();
    }

    @GetMapping(path = "{userId}")
    public List<Watchlist> getWatchlistsByUserId(@PathVariable("userId") Long userId) {
        return watchlistService.getWatchlistsByUserId(userId);
    }

    @PostMapping(path = "{userId}")
    public void addNewWatchlist(
            @PathVariable("userId") Long userId,
            @RequestParam(required = true) String name
            ){
        watchlistService.addWatchlist(userId, name);
    }

    @DeleteMapping(path = "{userId}")
    public void deleteWatchlist(
            @PathVariable("userId") Long userId,
            @RequestParam(required = true) String name
    ){
        watchlistService.deleteWatchlist(userId, name);
    }
}
