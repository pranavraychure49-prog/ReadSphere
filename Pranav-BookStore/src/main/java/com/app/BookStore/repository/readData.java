package com.app.BookStore.repository;

import java.io.File;
import java.io.FileReader;
import java.util.List;

import org.springframework.stereotype.Component;

import com.app.BookStore.model.Book;
import com.app.BookStore.model.LibraryStatistics;
import com.app.BookStore.singleton.BookCatalogueManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.bean.CsvToBeanBuilder;
import com.opencsv.CSVWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class readData {

    private static final Logger logger = LoggerFactory.getLogger(readData.class);

    private final String csvPath  = "src/main/java/com/app/BookStore/data/books_catalog.csv";
    private final String jsonPath = "src/main/java/com/app/BookStore/data/books_catalog.json";

    // ── Single source of truth for the in-memory catalogue ───────────────
    private final BookCatalogueManager catalogueManager;

    public readData(BookCatalogueManager catalogueManager) {
        this.catalogueManager = catalogueManager;
    }

    // ── Convenience accessor (mirrors old getBooks() contract) ────────────
    public List<Book> getBooks() {
        return catalogueManager.getMutableCatalogue();
    }

    // ── CSV → JSON conversion / initial load ─────────────────────────────

    public void convertCsvToJson() throws Exception {
        List<Book> loaded;
        try (FileReader reader = new FileReader(csvPath)) {
            loaded = new CsvToBeanBuilder<Book>(reader)
                    .withType(Book.class)
                    .build()
                    .parse();
        } catch (Exception e) {
            logger.error("Failed to read CSV file: {}", csvPath, e);
            throw e;
        }

        // Push fresh data into the Singleton catalogue
        catalogueManager.loadCatalogue(loaded);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter()
              .writeValue(new File(jsonPath), catalogueManager.getCatalogue());

        logger.info("Done! JSON saved to: {}", jsonPath);
        logger.info("Total books converted: {}", catalogueManager.size());
    }

    // ── Write operations ──────────────────────────────────────────────────

    public synchronized void appendBookToCsv(Book newBook) throws Exception {
        if (newBook == null) throw new IllegalArgumentException("newBook cannot be null");

        if (!catalogueManager.isLoaded()) {
            try {
                convertCsvToJson();
            } catch (Exception e) {
                logger.error("Failed to load books from CSV during append operation", e);
                throw e;
            }
        }

        List<Book> books = catalogueManager.getMutableCatalogue();
        int nextId = books.isEmpty() ? 1 : books.stream().mapToInt(Book::getId).max().orElse(0) + 1;
        newBook.setId(nextId);

        try (java.io.FileWriter fw = new java.io.FileWriter(csvPath, true);
             CSVWriter csvWriter = new CSVWriter(fw)) {
            String[] row = new String[] {
                String.valueOf(newBook.getId()),
                nullToEmpty(newBook.getBookName()),
                nullToEmpty(newBook.getAuthorName()),
                nullToEmpty(newBook.getCategory()),
                nullToEmpty(newBook.getPublisher()),
                String.valueOf(newBook.getPrice()),
                String.valueOf(newBook.getQuantity()),
                String.valueOf(newBook.getPublishedYear()),
                nullToEmpty(newBook.getIsbn()),
                nullToEmpty(newBook.getLanguage())
            };
            csvWriter.writeNext(row, false);
        } catch (Exception e) {
            logger.error("Failed to append to CSV file: {}", csvPath, e);
            throw e;
        }

        convertCsvToJson();
    }

    // ── Read operations ───────────────────────────────────────────────────

    public Book getBookByName(String name) {
        return catalogueManager.getMutableCatalogue().stream()
                .filter(b -> b.getBookName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public Book addBooks(Book newBook) {
        List<Book> books = catalogueManager.getMutableCatalogue();
        if (newBook != null) {
            books.add(newBook);
            return newBook;
        }
        return null;
    }

    public LibraryStatistics getLibraryStatistics() {
        List<Book> books = catalogueManager.getMutableCatalogue();
        LibraryStatistics libraryStatistics = new LibraryStatistics();

        libraryStatistics.setTotalBooks((long) books.size());
        libraryStatistics.setTotalInventory(books.stream().mapToLong(Book::getQuantity).sum());
        libraryStatistics.setTotalInventoryValue(books.stream().mapToDouble(book -> book.getPrice() * book.getQuantity()).sum());
        libraryStatistics.setAverageBookPrice(books.stream().mapToDouble(Book::getPrice).average().orElse(0.0));

        libraryStatistics.setHighestPricedBook(
            books.stream().max((b1, b2) -> Double.compare(b1.getPrice(), b2.getPrice()))
                 .map(Book::getBookName).orElse("N/A")
        );
        libraryStatistics.setLowestPricedBook(
            books.stream().min((b1, b2) -> Double.compare(b1.getPrice(), b2.getPrice()))
                 .map(Book::getBookName).orElse("N/A")
        );

        libraryStatistics.setCategoryWiseBooks(
            books.stream().collect(java.util.stream.Collectors.groupingBy(Book::getCategory, java.util.stream.Collectors.counting()))
        );
        libraryStatistics.setAuthorWiseBooks(
            books.stream().collect(java.util.stream.Collectors.groupingBy(Book::getAuthorName, java.util.stream.Collectors.counting()))
        );
        libraryStatistics.setAveragePriceByCategory(
            books.stream().collect(java.util.stream.Collectors.groupingBy(Book::getCategory, java.util.stream.Collectors.averagingDouble(Book::getPrice)))
        );
        libraryStatistics.setPublisherWiseBooks(
            books.stream().collect(java.util.stream.Collectors.groupingBy(Book::getPublisher, java.util.stream.Collectors.counting()))
        );

        return libraryStatistics;
    }

    public synchronized Book updateBook(int id, Book updatedBook) {
        if (updatedBook == null) throw new IllegalArgumentException("updatedBook cannot be null");

        List<Book> books = catalogueManager.getMutableCatalogue();
        if (books.isEmpty()) return null;

        Book existingBook = books.stream().filter(b -> b.getId() == id).findFirst().orElse(null);
        if (existingBook == null) return null;

        if (updatedBook.getBookName() != null && !updatedBook.getBookName().isBlank())
            existingBook.setBookName(updatedBook.getBookName());
        if (updatedBook.getAuthorName() != null && !updatedBook.getAuthorName().isBlank())
            existingBook.setAuthorName(updatedBook.getAuthorName());
        if (updatedBook.getCategory() != null && !updatedBook.getCategory().isBlank())
            existingBook.setCategory(updatedBook.getCategory());
        if (updatedBook.getPublisher() != null && !updatedBook.getPublisher().isBlank())
            existingBook.setPublisher(updatedBook.getPublisher());
        if (updatedBook.getPrice() >= 0)
            existingBook.setPrice(updatedBook.getPrice());
        if (updatedBook.getQuantity() >= 0)
            existingBook.setQuantity(updatedBook.getQuantity());
        if (updatedBook.getPublishedYear() > 0)
            existingBook.setPublishedYear(updatedBook.getPublishedYear());
        if (updatedBook.getIsbn() != null && !updatedBook.getIsbn().isBlank())
            existingBook.setIsbn(updatedBook.getIsbn());
        if (updatedBook.getLanguage() != null && !updatedBook.getLanguage().isBlank())
            existingBook.setLanguage(updatedBook.getLanguage());

        try {
            rewriteCsvFile();
            new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValue(new File(jsonPath), books);
            logger.info("Book with id {} updated successfully", id);
        } catch (Exception e) {
            logger.error("Failed to update CSV or JSON file", e);
        }

        return existingBook;
    }

    public synchronized Book deleteById(int id) {
        List<Book> books = catalogueManager.getMutableCatalogue();
        if (books.isEmpty()) return null;

        Book bookToDelete = books.stream().filter(b -> b.getId() == id).findFirst().orElse(null);
        if (bookToDelete == null) return null;

        books.remove(bookToDelete);

        try {
            rewriteCsvFile();
            new ObjectMapper().writerWithDefaultPrettyPrinter()
                .writeValue(new File(jsonPath), books);
            logger.info("Book with id {} deleted successfully", id);
        } catch (Exception e) {
            logger.error("Failed to delete book from CSV or JSON file", e);
        }

        return bookToDelete;
    }

    // ── Private helpers ───────────────────────────────────────────────────

    private void rewriteCsvFile() throws Exception {
        List<Book> books = catalogueManager.getMutableCatalogue();
        try (java.io.FileWriter fw = new java.io.FileWriter(csvPath, false);
             CSVWriter csvWriter = new CSVWriter(fw)) {

            String[] header = {"id","bookName","authorName","category","publisher",
                               "price","quantity","publishedYear","isbn","language"};
            csvWriter.writeNext(header, false);

            for (Book book : books) {
                String[] row = {
                    String.valueOf(book.getId()),
                    nullToEmpty(book.getBookName()),
                    nullToEmpty(book.getAuthorName()),
                    nullToEmpty(book.getCategory()),
                    nullToEmpty(book.getPublisher()),
                    String.valueOf(book.getPrice()),
                    String.valueOf(book.getQuantity()),
                    String.valueOf(book.getPublishedYear()),
                    nullToEmpty(book.getIsbn()),
                    nullToEmpty(book.getLanguage())
                };
                csvWriter.writeNext(row, false);
            }
        }
    }

    private String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
