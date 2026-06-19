package com.app.BookStore.service;

import com.app.BookStore.model.Book;
import com.app.BookStore.model.LibraryStatistics;
import com.app.BookStore.repository.readData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;
@Service
public class BooksService
{
    @Autowired
    private readData data;

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
                .filter(b -> b.getCategory() != null && b.getCategory().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    public List<Book> getBooksByPublisher(String publisher) {
        List<Book> books = data.getBooks();
        if (books == null) return Collections.emptyList();
        return books.stream()
                .filter(b -> b.getPublisher() != null && b.getPublisher().equalsIgnoreCase(publisher))
                .collect(Collectors.toList());
    }

    public Book createBook(Book newBook) throws Exception {
        if (newBook == null) throw new IllegalArgumentException("Book cannot be null");
        if (newBook.getBookName() == null || newBook.getBookName().isBlank())
            throw new IllegalArgumentException("bookName is required");
        if (newBook.getAuthorName() == null || newBook.getAuthorName().isBlank())
            throw new IllegalArgumentException("authorName is required");

        // price/quantity basic validation
        if (newBook.getPrice() < 0) throw new IllegalArgumentException("price cannot be negative");
        if (newBook.getQuantity() < 0) throw new IllegalArgumentException("quantity cannot be negative");

        // Append to CSV and refresh in-memory data (readData handles id assignment)
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
}
