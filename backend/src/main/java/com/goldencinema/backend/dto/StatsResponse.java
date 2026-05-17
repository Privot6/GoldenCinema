package com.goldencinema.backend.dto;

import java.math.BigDecimal;

public record StatsResponse(
        long totalUsers,
        long todayScreenings,
        long upcomingScreenings,
        long totalHalls,
        BigDecimal monthlyRevenue
) {}
