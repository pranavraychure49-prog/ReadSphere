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

    // =========================================================================
    // NON-PAGINATED ENDPOINTS  (existing behaviour unchanged)
    // =========================================================================

    // GET /books/all  → 200 OK | 204 No Content
    @GetMapping("/books/all")
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = booksService.getAllBooks();
        if (books == null || books.isEmpty()) {
            return ResponseEntity.noContent().build();                   // 204
        }
        return ResponseEntity.ok(books);                                 // 200
    }

    // GET /books/{name}  → 200 OK | 404 Not Found
    @GetMapping("/books/{name}")
    public ResponseEntity<Book> getBooksByName(@PathVariable String name) {
        Book book = booksService.getBookByName(name);
        if (book == null) {
            return ResponseEntity.notFound().build();                    // 404
        }
        return ResponseEntity.ok(book);                                  // 200
    }

    // POST /books/add (in-memory only)  → 201 Created | 400 Bad Request
    @PostMapping("/books/add")
    public ResponseEntity<Book> addBook(@RequestBody Book newBook) {
        if (newBook == null) {
            return ResponseEntity.badRequest().build();
        }
        Book added = booksService.addBooks(newBook);
        return ResponseEntity.status(HttpStatus.CREATED).body(added);
    }

    // POST /books (persist to CSV + JSON)  → 201 Created | 400 Bad Request
    @PostMapping("/books")
    public ResponseEntity<Book> createBook(@RequestBody Book newBook) throws Exception {
        Book created = booksService.createBook(newBook);
        reportService.generateAndSaveReport("report.txt");
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // GET /admin  → 200 OK
    @GetMapping("/admin")
    public ResponseEntity<LibraryStatistics> getLibraryStatistics() {
        return ResponseEntity.ok(booksService.getLibraryStatistics());
    }

    // GET /report  → 200 OK (plain text)
    @GetMapping(value = "/report", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> getReport() {
        return ResponseEntity.ok(reportService.generateReport());
    }

    // GET /books/byCategory/{category}  → 200 OK | 204 No Content
    @GetMapping("/books/byCategory/{category}")
    public ResponseEntity<List<Book>> getByCategory(@PathVariable String category) {
        List<Book> books = booksService.getBooksByCategory(category);
        if (books == null || books.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(books);
    }

    // GET /books/byPublisher?publisher=  → 200 OK | 204 No Content
    @GetMapping(value = "/books/byPublisher", params = "publisher")
    public ResponseEntity<List<Book>> getByPublisherQuery(@RequestParam String publisher) {
        List<Book> books = booksService.getBooksByPublisher(publisher);
        if (books == null || books.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(books);
    }

    // PUT /books/id/{id}  → 200 OK | 404 Not Found
    @PutMapping("/books/id/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable int id,
                                           @RequestBody Book updatedBook) {
        Book updated = booksService.updateBook(id, updatedBook);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }

    // DELETE /books/id/{id}  → 200 OK | 404 Not Found
    @DeleteMapping("/books/id/{id}")
    public ResponseEntity<Book> deleteBook(@PathVariable int id) {
        Book deleted = booksService.deleteBook(id);
        if (deleted == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(deleted);
    }


    // GET /books/paged
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

    // GET /books/paged/byCategory/{category}
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

    //GET /books/paged/byPublisher?publisher
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
