package com.t95.t95backend.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity(name = "watched_stocks")
@Table(name = "watched_stocks")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class WatchedStock {

    @Id
    @SequenceGenerator(name = "watched_stocks_id_gen", sequenceName = "watched_stocks_id_seq", allocationSize = 1 )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "watched_stocks_id_gen")

    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "watchlist_id", nullable = false)
    private Long watchlistId;

    @Column(name = "stock_id", nullable = false)
    private Long stockId;
    
    public WatchedStock(Long id, Long watchlistId, Long stockId) {
		super();
		this.id = id;
		this.watchlistId = watchlistId;
		this.stockId = stockId;
	}

	public WatchedStock(Long watchlistId, Long stockId) {
        this.watchlistId = watchlistId;
        this.stockId = stockId;
    }
}
