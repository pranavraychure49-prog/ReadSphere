package com.app.BookStore.service;

import com.app.BookStore.config.CacheConfig;
import com.app.BookStore.model.Book;
import com.app.BookStore.model.LibraryStatistics;
import com.app.BookStore.model.PagedResponse;
import com.app.BookStore.repository.readData;
import com.app.BookStore.util.PaginationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BooksService {

    @Autowired
    private readData data;

    // =========================================================================
    // READ – Cached
    // =========================================================================

    /**
     * Returns all books. Result is cached under key "allBooks".
     * Cache is evicted whenever any write/update/delete happens.
     */
    @Cacheable(value = CacheConfig.CACHE_BOOKS, key = "'allBooks'")
    public List<Book> getAllBooks() {
        return data.getBooks();
    }

    public Book getBookByName(String name) {
        return data.getBookByName(name);
    }

    /**
     * Returns books filtered by category. Cached per category name.
     */
    @Cacheable(value = CacheConfig.CACHE_BY_CATEGORY, key = "#category.toLowerCase()")
    public List<Book> getBooksByCategory(String category) {
        List<Book> books = data.getBooks();
        if (books == null) return Collections.emptyList();
        return books.stream()
                .filter(b -> b.getCategory() != null
                          && b.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    /**
     * Returns books filtered by publisher. Cached per publisher name.
     */
    @Cacheable(value = CacheConfig.CACHE_BY_PUBLISHER, key = "#publisher.toLowerCase()")
    public List<Book> getBooksByPublisher(String publisher) {
        List<Book> books = data.getBooks();
        if (books == null) return Collections.emptyList();
        return books.stream()
                .filter(b -> b.getPublisher() != null
                          && b.getPublisher().equalsIgnoreCase(publisher))
                .collect(Collectors.toList());
    }

    public LibraryStatistics getLibraryStatistics() {
        return data.getLibraryStatistics();
    }

    // =========================================================================
    // WRITE – Evict all caches so stale data is never served
    // =========================================================================

    /**
     * Adds a book to the in-memory list only (no persistence).
     * Evicts all caches.
     */
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.CACHE_BOOKS,        allEntries = true),
        @CacheEvict(value = CacheConfig.CACHE_BY_CATEGORY,  allEntries = true),
        @CacheEvict(value = CacheConfig.CACHE_BY_PUBLISHER, allEntries = true)
    })
    public Book addBooks(Book newBook) {
        return data.addBooks(newBook);
    }

    /**
     * Persists a new book to CSV + JSON.
     * Evicts all caches.
     */
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.CACHE_BOOKS,        allEntries = true),
        @CacheEvict(value = CacheConfig.CACHE_BY_CATEGORY,  allEntries = true),
        @CacheEvict(value = CacheConfig.CACHE_BY_PUBLISHER, allEntries = true)
    })
    public Book createBook(Book newBook) throws Exception {
        if (newBook == null) throw new IllegalArgumentException("Book cannot be null");
        if (newBook.getBookName() == null || newBook.getBookName().isBlank())
            throw new IllegalArgumentException("bookName is required");
        if (newBook.getAuthorName() == null || newBook.getAuthorName().isBlank())
            throw new IllegalArgumentException("authorName is required");
        if (newBook.getPrice() < 0)    throw new IllegalArgumentException("price cannot be negative");
        if (newBook.getQuantity() < 0) throw new IllegalArgumentException("quantity cannot be negative");

        data.appendBookToCsv(newBook);
        data.convertCsvToJson();
        return newBook;
    }

    /**
     * Updates an existing book by id.
     * Evicts all caches.
     */
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.CACHE_BOOKS,        allEntries = true),
        @CacheEvict(value = CacheConfig.CACHE_BY_CATEGORY,  allEntries = true),
        @CacheEvict(value = CacheConfig.CACHE_BY_PUBLISHER, allEntries = true)
    })
    public Book updateBook(int id, Book updatedBook) {
        return data.updateBook(id, updatedBook);
    }

    /**
     * Deletes a book by id. Restricted to ADMIN role (enforced in SecurityConfig).
     * Evicts all caches.
     */
    @Caching(evict = {
        @CacheEvict(value = CacheConfig.CACHE_BOOKS,        allEntries = true),
        @CacheEvict(value = CacheConfig.CACHE_BY_CATEGORY,  allEntries = true),
        @CacheEvict(value = CacheConfig.CACHE_BY_PUBLISHER, allEntries = true)
    })
    public Book deleteBook(int id) {
        return data.deleteById(id);
    }

    // =========================================================================
    // PAGINATED (delegates to cached methods above)
    // =========================================================================

    public PagedResponse<Book> getAllBooksPaged(int page, int size,
                                               String sortBy, String sortDir) {
        List<Book> all = getAllBooks();               // served from cache after first call
        if (all == null) all = Collections.emptyList();
        return PaginationUtils.paginate(all, page, size, sortBy, sortDir);
    }

    public PagedResponse<Book> getBooksByCategoryPaged(String category,
                                                       int page, int size,
                                                       String sortBy, String sortDir) {
        List<Book> filtered = getBooksByCategory(category);  // cached per category
        return PaginationUtils.paginate(filtered, page, size, sortBy, sortDir);
    }

    public PagedResponse<Book> getBooksByPublisherPaged(String publisher,
                                                        int page, int size,
                                                        String sortBy, String sortDir) {
        List<Book> filtered = getBooksByPublisher(publisher);  // cached per publisher
        return PaginationUtils.paginate(filtered, page, size, sortBy, sortDir);
    }
}
