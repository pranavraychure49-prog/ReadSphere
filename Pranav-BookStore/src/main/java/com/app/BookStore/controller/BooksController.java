package com.app.BookStore.controller;

import com.app.BookStore.model.Book;
import com.app.BookStore.model.LibraryStatistics;
import com.app.BookStore.model.PagedResponse;
import com.app.BookStore.service.BooksService;
import com.app.BookStore.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class BooksController {

    @Autowired
    private BooksService booksService;

    @Autowired
    private ReportService reportService;

    @GetMapping("/books/all")
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = booksService.getAllBooks();
        if (books == null || books.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(books);
    }

    @GetMapping("/books/{name}")
    public ResponseEntity<Book> getBooksByName(@PathVariable String name) {
        Book book = booksService.getBookByName(name);
        if (book == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(book);
    }

    @PostMapping("/books/add")
    public ResponseEntity<Book> addBook(@RequestBody Book newBook) {
        if (newBook == null) {
            return ResponseEntity.badRequest().build();
        }
        Book added = booksService.addBooks(newBook);
        return ResponseEntity.status(HttpStatus.CREATED).body(added);
    }

    @PostMapping("/books")
    public ResponseEntity<Book> createBook(@RequestBody Book newBook) throws Exception {
        Book created = booksService.createBook(newBook);
        reportService.generateAndSaveReport("report.txt");
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/admin")
    public ResponseEntity<LibraryStatistics> getLibraryStatistics() {
        return ResponseEntity.ok(booksService.getLibraryStatistics());
    }

    @GetMapping(value = "/report", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getReport() {
        return ResponseEntity.ok(reportService.generateReport());
    }

    @GetMapping("/books/byCategory/{category}")
    public ResponseEntity<List<Book>> getByCategory(@PathVariable String category) {
        List<Book> books = booksService.getBooksByCategory(category);
        if (books == null || books.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(books);
    }

    @GetMapping(value = "/books/byPublisher", params = "publisher")
    public ResponseEntity<List<Book>> getByPublisherQuery(@RequestParam String publisher) {
        List<Book> books = booksService.getBooksByPublisher(publisher);
        if (books == null || books.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(books);
    }

    @PutMapping("/books/id/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable int id,
                                           @RequestBody Book updatedBook) {
        Book updated = booksService.updateBook(id, updatedBook);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/books/id/{id}")
    public ResponseEntity<Book> deleteBook(@PathVariable int id) {
        Book deleted = booksService.deleteBook(id);
        if (deleted == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(deleted);
    }


    @GetMapping("/books/paged")
    public ResponseEntity<PagedResponse<Book>> getAllBooksPaged(
            @RequestParam(defaultValue = "0")   int    page,
            @RequestParam(defaultValue = "10")  int    size,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PagedResponse<Book> response =
                booksService.getAllBooksPaged(page, size, sortBy, sortDir);
        return ResponseEntity.ok(response);                              // 200
    }


    @GetMapping("/books/paged/byCategory/{category}")
    public ResponseEntity<PagedResponse<Book>> getBooksByCategoryPaged(
            @PathVariable                           String category,
            @RequestParam(defaultValue = "0")   int    page,
            @RequestParam(defaultValue = "10")  int    size,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PagedResponse<Book> response =
                booksService.getBooksByCategoryPaged(category, page, size, sortBy, sortDir);

        if (response.getTotalElements() == 0) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/books/paged/byPublisher")
    public ResponseEntity<PagedResponse<Book>> getBooksByPublisherPaged(
            @RequestParam                           String publisher,
            @RequestParam(defaultValue = "0")   int    page,
            @RequestParam(defaultValue = "10")  int    size,
            @RequestParam(defaultValue = "id")  String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        PagedResponse<Book> response =
                booksService.getBooksByPublisherPaged(publisher, page, size, sortBy, sortDir);

        if (response.getTotalElements() == 0) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }
}
