package com.app.BookStore.util;

import com.app.BookStore.model.Book;
import com.app.BookStore.model.PagedResponse;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class PaginationUtils {

    public static final Set<String> SORTABLE_FIELDS = Set.of(
            "id", "bookname", "authorname", "category",
            "publisher", "price", "quantity", "publishedyear"
    );

    private PaginationUtils() { }

    public static PagedResponse<Book> paginate(List<Book> source,
                                               int page,
                                               int size,
                                               String sortBy,
                                               String sortDir)

    {
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0 (received: " + page + ")");
        }
        if (size < 1 || size > 100) {
            throw new IllegalArgumentException("size must be between 1 and 100 (received: " + size + ")");
        }

        String normalizedField = (sortBy == null || sortBy.isBlank()) ? "id" : sortBy.trim().toLowerCase();
        if (!SORTABLE_FIELDS.contains(normalizedField)) {
            throw new IllegalArgumentException(
                    "Invalid sortBy field '" + sortBy + "'. Allowed: " + SORTABLE_FIELDS);
        }

        String dir = (sortDir == null || sortDir.isBlank()) ? "asc" : sortDir.trim().toLowerCase();
        if (!dir.equals("asc") && !dir.equals("desc")) {
            throw new IllegalArgumentException(
                    "sortDir must be 'asc' or 'desc' (received: '" + sortDir + "')");
        }

        Comparator<Book> comparator = resolveComparator(normalizedField);
        if (dir.equals("desc")) {
            comparator = comparator.reversed();
        }
        List<Book> sorted = source.stream().sorted(comparator).toList();

        long total     = sorted.size();
        int  fromIndex = page * size;

        if (fromIndex >= total && total > 0) {
            throw new IllegalArgumentException(
                    "page " + page + " is out of range. Total pages: "
                    + (int) Math.ceil((double) total / size));
        }

        int      toIndex = (int) Math.min((long) fromIndex + size, total);
        List<Book> slice = (total == 0) ? List.of() : sorted.subList(fromIndex, toIndex);

        return new PagedResponse<>(slice, page, size, total, sortBy, dir);
    }

    private static Comparator<Book> resolveComparator(String field) {
        return switch (field) {
            case "bookname"      -> Comparator.comparing(
                                        b -> nullSafe(b.getBookName()));
            case "authorname"    -> Comparator.comparing(
                                        b -> nullSafe(b.getAuthorName()));
            case "category"      -> Comparator.comparing(
                                        b -> nullSafe(b.getCategory()));
            case "publisher"     -> Comparator.comparing(
                                        b -> nullSafe(b.getPublisher()));
            case "price"         -> Comparator.comparingDouble(Book::getPrice);
            case "quantity"      -> Comparator.comparingInt(Book::getQuantity);
            case "publishedyear" -> Comparator.comparingInt(Book::getPublishedYear);
            default              -> Comparator.comparingInt(Book::getId);  // "id"
        };
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value.toLowerCase();
    }
}
