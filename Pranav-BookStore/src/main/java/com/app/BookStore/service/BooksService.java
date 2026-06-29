package com.app.BookStore.service;

import com.app.BookStore.model.Book;
import com.app.BookStore.model.LibraryStatistics;
import com.app.BookStore.model.PagedResponse;
import com.app.BookStore.repository.readData;
import com.app.BookStore.util.PaginationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BooksService {

    @Autowired
    private readData data;

    // ── Plain (non-paginated) ─────────────────────────────────────────────

    public List<Book> getAllBooks() {
        return data.getBooks();
    }

    public Book getBookByName(String name) {
        return data.getBookByName(name);
    }

    public Book addBooks(Book newBook) {
        return data.addBooks(newBook);
    }

    public LibraryStatistics getLibraryStatistics() {
        return data.getLibraryStatistics();
    }

    public List<Book> getBooksByCategory(String category) {
        List<Book> books = data.getBooks();
        if (books == null) return Collections.emptyList();
        return books.stream()
                .filter(b -> b.getCategory() != null
                          && b.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public List<Book> getBooksByPublisher(String publisher) {
        List<Book> books = data.getBooks();
        if (books == null) return Collections.emptyList();
        return books.stream()
                .filter(b -> b.getPublisher() != null
                          && b.getPublisher().equalsIgnoreCase(publisher))
                .collect(Collectors.toList());
    }

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

    public Book updateBook(int id, Book updatedBook) {
        return data.updateBook(id, updatedBook);
    }

    public Book deleteBook(int id) {
        return data.deleteById(id);
    }

    //Paginated
    public PagedResponse<Book> getAllBooksPaged(int page, int size,
                                               String sortBy, String sortDir) {
        List<Book> all = data.getBooks();
        if (all == null) all = Collections.emptyList();
        return PaginationUtils.paginate(all, page, size, sortBy, sortDir);
    }


     // Returns a paginated + sorted slice filtered by category.
    public PagedResponse<Book> getBooksByCategoryPaged(String category,
                                                       int page, int size,
                                                       String sortBy, String sortDir) {
        List<Book> filtered = getBooksByCategory(category);
        return PaginationUtils.paginate(filtered, page, size, sortBy, sortDir);
    }


    //Returns a paginated + sorted slice filtered by publisher.
    public PagedResponse<Book> getBooksByPublisherPaged(String publisher,
                                                        int page, int size,
                                                        String sortBy, String sortDir) {
        List<Book> filtered = getBooksByPublisher(publisher);
        return PaginationUtils.paginate(filtered, page, size, sortBy, sortDir);
    }
}
