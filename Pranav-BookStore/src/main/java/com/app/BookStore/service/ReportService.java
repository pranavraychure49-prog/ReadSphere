package com.app.BookStore.service;

import com.app.BookStore.model.Book;
import com.app.BookStore.model.LibraryStatistics;
import com.app.BookStore.repository.readData;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ReportService {

    private final readData readData;

    public ReportService(readData readData) {
        this.readData = readData;
    }

    public String generateReport() {
        List<Book> books = readData.getBooks();

        if (books == null || books.isEmpty()) {
            return "No book data available. Please ensure the CSV has been loaded.";
        }

        LibraryStatistics stats = readData.getLibraryStatistics();
        StringBuilder report = new StringBuilder();

        String line     = "=".repeat(70);
        String thinLine = "-".repeat(70);
        String generated = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        // ── Header ──────────────────────────────────────────────────────────
        report.append(line).append("\n");
        report.append("              BOOKSTORE - COMPLETE CATALOG REPORT\n");
        report.append(line).append("\n");
        report.append(String.format("  Generated On : %s%n", generated));
        report.append(String.format("  Total Titles : %d%n", stats.getTotalBooks()));
        report.append(line).append("\n\n");

        report.append("1. SUMMARY STATISTICS\n");
        report.append(thinLine).append("\n");
        report.append(String.format("  Total Book Titles        : %d%n",      stats.getTotalBooks()));
        report.append(String.format("  Total Stock (Inventory)  : %d copies%n", stats.getTotalInventory()));
        report.append(String.format("  Total Inventory Value    : INR %.2f%n", stats.getTotalInventoryValue()));
        report.append(String.format("  Average Book Price       : INR %.2f%n", stats.getAverageBookPrice()));
        report.append(String.format("  Highest Priced Book      : %s%n",      stats.getHighestPricedBook()));
        report.append(String.format("  Lowest Priced Book       : %s%n",      stats.getLowestPricedBook()));
        report.append("\n");

        report.append("2. FULL BOOK CATALOG\n");
        report.append(thinLine).append("\n");
        report.append(String.format("  %-4s  %-40s %-22s %-18s %8s %6s%n",
                "ID", "Book Name", "Author", "Category", "Price", "Qty"));
        report.append(thinLine).append("\n");

        for (Book book : books) {
            report.append(String.format("  %-4d  %-40s %-22s %-18s %8.2f %6d%n",
                    book.getId(),
                    truncate(book.getBookName(), 40),
                    truncate(book.getAuthorName(), 22),
                    truncate(book.getCategory(), 18),
                    book.getPrice(),
                    book.getQuantity()));
        }
        report.append("\n");

        report.append("3. BOOKS BY CATEGORY\n");
        report.append(thinLine).append("\n");
        report.append(String.format("  %-25s  %8s  %12s%n", "Category", "Books", "Avg Price"));
        report.append(thinLine).append("\n");

        Map<String, Long>   categoryCount = stats.getCategoryWiseBooks();
        Map<String, Double> categoryAvg   = stats.getAveragePriceByCategory();

        categoryCount.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry -> {
                    String cat    = entry.getKey();
                    long   count  = entry.getValue();
                    double avg    = categoryAvg.getOrDefault(cat, 0.0);
                    report.append(String.format("  %-25s  %8d  INR %8.2f%n", cat, count, avg));
                });
        report.append("\n");

        report.append("4. BOOKS BY PUBLISHER\n");
        report.append(thinLine).append("\n");
        report.append(String.format("  %-30s  %8s%n", "Publisher", "Books"));
        report.append(thinLine).append("\n");

        stats.getPublisherWiseBooks().entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry ->
                    report.append(String.format("  %-30s  %8d%n", entry.getKey(), entry.getValue()))
                );
        report.append("\n");

        report.append("5. BOOKS BY AUTHOR\n");
        report.append(thinLine).append("\n");
        report.append(String.format("  %-30s  %8s%n", "Author", "Books"));
        report.append(thinLine).append("\n");

        stats.getAuthorWiseBooks().entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .forEach(entry ->
                    report.append(String.format("  %-30s  %8d%n", entry.getKey(), entry.getValue()))
                );
        report.append("\n");

        report.append("6. DETAILED BOOK INFORMATION\n");
        report.append(thinLine).append("\n");

        for (Book book : books) {
            report.append(String.format("  [%d] %s%n", book.getId(), book.getBookName()));
            report.append(String.format("      Author       : %s%n", book.getAuthorName()));
            report.append(String.format("      Category     : %s%n", book.getCategory()));
            report.append(String.format("      Publisher    : %s%n", book.getPublisher()));
            report.append(String.format("      Price        : INR %.2f%n", book.getPrice()));
            report.append(String.format("      Quantity     : %d copies%n", book.getQuantity()));
            report.append(String.format("      Published    : %d%n", book.getPublishedYear()));
            report.append(String.format("      ISBN         : %s%n", book.getIsbn()));
            report.append(String.format("      Language     : %s%n", book.getLanguage()));
            report.append("\n");
        }

        report.append(line).append("\n");
        report.append("                        END OF REPORT\n");
        report.append(line).append("\n");

        return report.toString();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "";
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 2) + "..";
    }
    public void generateAndSaveReport(String outputPath) {
        String report = generateReport();
        try {
            Files.writeString(Path.of(outputPath), report);
            System.out.println("Report written to: " + outputPath);
        } catch (IOException e) {
            System.err.println("Failed to write report to: " + outputPath);
            e.printStackTrace();
        }
    }
}
