package com.app.BookStore.model;

import com.opencsv.bean.CsvBindByName;
import lombok.Data;

@Data
public class Book {

    @CsvBindByName(column = "id")
    private int id;

    @CsvBindByName(column = "bookName")
    private String bookName;

    @CsvBindByName(column = "authorName")
    private String authorName;

    @CsvBindByName(column = "category")
    private String category;

    @CsvBindByName(column = "publisher")
    private String publisher;

    @CsvBindByName(column = "price")
    private double price;

    @CsvBindByName(column = "quantity")
    private int quantity;

    @CsvBindByName(column = "publishedYear")
    private int publishedYear;

    @CsvBindByName(column = "isbn")
    private String isbn;

    @CsvBindByName(column = "language")
    private String language;
}
