package com.app.BookStore.repository;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.springframework.stereotype.Component;

import com.app.BookStore.model.Book;
import com.app.BookStore.model.LibraryStatistics;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBeanBuilder;

import lombok.Getter;

@Getter
@Component
public class readData
{
    private List<Book> books;

    // Paths are stored here so other methods can reuse them when appending/refreshing data
    private final String csvPath = "src/main/java/com/app/BookStore/data/books_catalog.csv";
    private final String jsonPath = "src/main/java/com/app/BookStore/data/books_catalog.json";

    public void convertCsvToJson() throws Exception {

        try (FileReader reader = new FileReader(csvPath)) {
            books = new CsvToBeanBuilder<Book>(reader)
                    .withType(Book.class)
                    .build()
                    .parse();
        }
        catch (Exception e) {
            System.err.println("Failed to read CSV file: " + csvPath);
            e.printStackTrace();
            throw e;
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter()
              .writeValue(new File(jsonPath), books);

        System.out.println("Done! JSON saved to: " + jsonPath);
        System.out.println("Total books converted: " + books.size());
    }

    // Append a new Book to the CSV (thread-safe) and refresh the in-memory list + JSON snapshot
    public synchronized void appendBookToCsv(Book newBook) throws Exception {
        if (newBook == null) throw new IllegalArgumentException("newBook cannot be null");

        // Ensure books are loaded so we can compute the next id
        if (books == null) {
            try {
                convertCsvToJson();
            } catch (Exception e) {
                // propagate - calling code should handle
                throw e;
            }
        }

        int nextId = 1;
        if (books != null && !books.isEmpty()) {
            nextId = books.stream().mapToInt(Book::getId).max().orElse(0) + 1;
        }
        newBook.setId(nextId);

        // Build a CSV-safe line (wrap text fields with quotes and escape quotes inside)
        String csvLine = String.format("%d,%s,%s,%s,%s,%.2f,%d,%d,%s,%s",
                newBook.getId(),
                escapeCsv(newBook.getBookName()),
                escapeCsv(newBook.getAuthorName()),
                escapeCsv(newBook.getCategory()),
                escapeCsv(newBook.getPublisher()),
                newBook.getPrice(),
                newBook.getQuantity(),
                newBook.getPublishedYear(),
                escapeCsv(newBook.getIsbn()),
                escapeCsv(newBook.getLanguage())
        );

        // Append to CSV
        try (java.io.FileWriter fw = new java.io.FileWriter(csvPath, true);
             java.io.BufferedWriter bw = new java.io.BufferedWriter(fw)) {
            bw.write(csvLine);
            bw.newLine();
        }

        // Refresh in-memory list and JSON snapshot
        convertCsvToJson();
    }

    // Helper: escape a value for CSV output (simple quoting)
    private String escapeCsv(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        // wrap with quotes if it contains comma, quote or newline
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }

    public Book getBookByName(String name)
    {
        if (books != null) {
            for (Book book : books) {
                if (book.getBookName().equalsIgnoreCase(name)) {
                    return book;
                }
            }
        }
        return null;
    }

    public Book addBooks(Book newBook)
    {
        if (books != null) {
            books.add(newBook);
            return newBook;
        }
        return null;
    }

    public LibraryStatistics getLibraryStatistics()
    {
        LibraryStatistics libraryStatistics = new LibraryStatistics();

        libraryStatistics.setTotalBooks((long) books.size());
        libraryStatistics.setTotalInventory(books.stream().mapToLong(Book::getQuantity).sum());
        libraryStatistics.setTotalInventoryValue(books.stream().mapToDouble(book -> book.getPrice() * book.getQuantity()).sum());
        libraryStatistics.setAverageBookPrice(books.stream().mapToDouble(Book::getPrice).average().orElse(0.0));

        libraryStatistics.setHighestPricedBook(
            books.stream().max((b1, b2) -> Double.compare(b1.getPrice(), b2.getPrice())).map(Book::getBookName).orElse("N/A")
        );
        libraryStatistics.setLowestPricedBook(
            books.stream().min((b1, b2) -> Double.compare(b1.getPrice(), b2.getPrice())).map(Book::getBookName).orElse("N/A")
        );

        // Books count per category
        libraryStatistics.setCategoryWiseBooks(
            books.stream().collect(java.util.stream.Collectors.groupingBy(Book::getCategory, java.util.stream.Collectors.counting()))
        );

        // Books count per author
        libraryStatistics.setAuthorWiseBooks(
            books.stream().collect(java.util.stream.Collectors.groupingBy(Book::getAuthorName, java.util.stream.Collectors.counting()))
        );

        // Average price per category
        libraryStatistics.setAveragePriceByCategory(
            books.stream().collect(java.util.stream.Collectors.groupingBy(Book::getCategory, java.util.stream.Collectors.averagingDouble(Book::getPrice)))
        );

        // Books count per publisher
        libraryStatistics.setPublisherWiseBooks(
            books.stream().collect(java.util.stream.Collectors.groupingBy(Book::getPublisher, java.util.stream.Collectors.counting()))
        );

        return libraryStatistics;
    }
}
