package com.app.BookStore.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LibraryStatistics {

    private Long totalBooks;
    private Long totalInventory;
    private Double totalInventoryValue;
    private Double averageBookPrice;

    private String highestPricedBook;
    private String lowestPricedBook;

    private Map<String, Long> categoryWiseBooks;
    private Map<String, Long> authorWiseBooks;
    private Map<String, Double> averagePriceByCategory;
    private Map<String, Long> publisherWiseBooks;
}