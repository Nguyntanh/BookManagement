package com.example.bookmanagementapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private static final Logger logger = LoggerFactory.getLogger(BookController.class);

    @Autowired
    private BookRepository bookRepository;

    // GET: Lấy tất cả sách với phân trang
    @GetMapping
    public Page<Book> getAllBooks(Pageable pageable) {
        logger.info("Fetching all books with pageable: {}", pageable);
        return bookRepository.findAll(pageable);
    }

    // GET: Lấy sách theo ID
//    @GetMapping("/{id}")
//    public ResponseEntity<Book> getBookById(@PathVariable String id) {
//        logger.info("Fetching book with ID: {}", id);
//        Optional<Book> book = bookRepository.findById(id);
//        if (book.isPresent()) {
//            return ResponseEntity.ok(book.get());
//        } else {
//            logger.warn("Book not found with ID: {}", id);
//            return ResponseEntity.notFound().build();
//        }
//    }

    // GET: Tìm kiếm sách theo title và/hoặc author với phân trang
    @GetMapping("/search")
    public Page<Book> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            Pageable pageable) {
        logger.info("Searching books with title: {}, author: {}, pageable: {}", title, author, pageable);
        if (title != null && !title.trim().isEmpty() && author != null && !author.trim().isEmpty()) {
            return bookRepository.findByTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(title, author, pageable);
        } else if (title != null && !title.trim().isEmpty()) {
            return bookRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else if (author != null && !author.trim().isEmpty()) {
            return bookRepository.findByAuthorContainingIgnoreCase(author, pageable);
        } else {
            return bookRepository.findAll(pageable);
        }
    }

    // POST: Thêm sách mới
    @PostMapping
    public Book createBook(@RequestBody Book book) {
        logger.info("Creating new book: {}", book);
        Book savedBook = bookRepository.save(book);
        logger.info("Created book with ID: {}", savedBook.getId());
        return savedBook;
    }

    // PUT: Cập nhật sách
    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable String id, @RequestBody Book bookDetails) {
        logger.info("Updating book with ID: {}", id);
        Optional<Book> book = bookRepository.findById(id);
        if (book.isPresent()) {
            Book updatedBook = book.get();
            updatedBook.setTitle(bookDetails.getTitle());
            updatedBook.setAuthor(bookDetails.getAuthor());
            updatedBook.setPrice(bookDetails.getPrice());
            Book savedBook = bookRepository.save(updatedBook);
            logger.info("Updated book with ID: {}", id);
            return ResponseEntity.ok(savedBook);
        }
        logger.warn("Book not found for update with ID: {}", id);
        return ResponseEntity.notFound().build();
    }

    // DELETE: Xóa sách với xác nhận
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable String id, @RequestParam(required = false) Boolean confirm) {
        logger.info("Request to delete book with ID: {}", id);
        if (id == null || id.trim().isEmpty()) {
            logger.warn("Invalid book ID: {}", id);
            return ResponseEntity.badRequest().body("ID sách không hợp lệ.");
        }
        try {
            if (bookRepository.existsById(id)) {
                if (Boolean.TRUE.equals(confirm)) {
                    bookRepository.deleteById(id);
                    logger.info("Deleted book with ID: {}", id);
                    return ResponseEntity.ok().build();
                } else {
                    logger.warn("Missing delete confirmation for book ID: {}", id);
                    return ResponseEntity.badRequest().body("Xác nhận xóa (confirm=true) là bắt buộc.");
                }
            }
            logger.warn("Book not found for deletion with ID: {}", id);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error deleting book with ID: {} - {}", id, e.getMessage());
            return ResponseEntity.status(500).body("Lỗi khi xóa sách: " + e.getMessage());
        }
    }
}