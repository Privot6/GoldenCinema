package com.goldencinema.backend.dto;

import java.math.BigDecimal;

public class WeeklyProfitRow {

    private String movieTitle;
    private Long screeningsCount;
    private Long ticketsCount;
    private BigDecimal revenue;

    public WeeklyProfitRow(String movieTitle, Long screeningsCount, Long ticketsCount, BigDecimal revenue) {
        this.movieTitle = movieTitle;
        this.screeningsCount = screeningsCount;
        this.ticketsCount = ticketsCount;
        this.revenue = revenue;
    }

    public String getMovieTitle()       { return movieTitle; }
    public Long getScreeningsCount()    { return screeningsCount; }
    public Long getTicketsCount()       { return ticketsCount; }
    public BigDecimal getRevenue()      { return revenue; }
}
