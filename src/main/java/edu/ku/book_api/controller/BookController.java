package edu.ku.book_api.controller;

import edu.ku.book_api.model.Books;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/v1/books")
public class BookController {

    // Mutable, in-memory list so books can be added, updated, and removed.
    private final List<Books> books = new ArrayList<>(List.of(
            new Books(1L, "Clean Code", "Robert C. Martin",
                    "9780132350884", 2008, "Software Engineering"),
            new Books(2L, "Effective Java", "Joshua Bloch",
                    "9780134685991", 2018, "Java"),
            new Books(3L, "Designing Data-Intensive Applications", "Martin Kleppmann",
                    "9781449373320", 2017, "Distributed Systems"),
            new Books(4L, "Spring in Action", "Craig Walls",
                    "9781617297571", 2022, "Spring"),
            new Books(5L, "Computer Networks", "Andrew S. Tanenbaum",
                    "9780132126953", 2010, "Networking")
    ));

    // Tracks the next id to assign, starting after the last seeded book.
    private final AtomicLong nextId = new AtomicLong(6L);

    @GetMapping
    public List<Books> getAllBooks() {
        return books;
    }

    // GET /api/v1/books/{id} - returns a single book by its id.
    @GetMapping("/{id}")
    public ResponseEntity<Books> getBookById(@PathVariable Long id) {
        Optional<Books> match = books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst();

        return match.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // POST /api/v1/books - creates a new book from the JSON request body.
    @PostMapping
    public ResponseEntity<Books> createBook(@RequestBody Books newBook) {
        newBook.setId(nextId.getAndIncrement());
        books.add(newBook);
        return ResponseEntity.status(HttpStatus.CREATED).body(newBook);
    }

    // PUT /api/v1/books/{id} - replaces an existing book's fields (id stays the same).
    @PutMapping("/{id}")
    public ResponseEntity<Books> updateBook(@PathVariable Long id, @RequestBody Books input) {
        Optional<Books> match = books.stream()
                .filter(book -> book.getId().equals(id))
                .findFirst();

        if (match.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Books existing = match.get();
        existing.setTitle(input.getTitle());
        existing.setAuthor(input.getAuthor());
        existing.setIsbn(input.getIsbn());
        existing.setPublishedYear(input.getPublishedYear());
        existing.setCategory(input.getCategory());
        // id is intentionally left untouched.

        return ResponseEntity.ok(existing);
    }

    // DELETE /api/v1/books/{id} - removes an existing book.
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        boolean removed = books.removeIf(book -> book.getId().equals(id));

        if (removed) {
            return ResponseEntity.noContent().build(); // 204
        }
        return ResponseEntity.notFound().build(); // 404
    }
}