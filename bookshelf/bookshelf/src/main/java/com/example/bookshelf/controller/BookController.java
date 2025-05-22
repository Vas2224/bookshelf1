package com.example.bookshelf.controller;

import com.example.bookshelf.model.Book;
import com.example.bookshelf.repository.BookRepository;
import com.example.bookshelf.service.GoogleBooksService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookRepository bookRepository;
    private final GoogleBooksService googleBooksService;

    public BookController(BookRepository bookRepository, GoogleBooksService googleBooksService) {
        this.bookRepository = bookRepository;
        this.googleBooksService = googleBooksService;
    }

    @GetMapping
    public String showBooks(Model model) {
        model.addAttribute("books", bookRepository.findAll());
        return "books";
    }

    @PostMapping("/add")
    public String addBook(@RequestParam String title, @RequestParam String author,
                          @RequestParam(required = false) String isbn,
                          @RequestParam(required = false) String coverImageUrl,
                          @RequestParam(required = false) Integer pageCount) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setRead(false);
        book.setIsbn(isbn);
        book.setCoverImageUrl(coverImageUrl);
        book.setPageCount(pageCount);
        bookRepository.save(book);
        return "redirect:/books";
    }

    @PostMapping("/delete")
    public String deleteBook(@RequestParam Long id) {
        bookRepository.deleteById(id);
        return "redirect:/books";
    }

    @PostMapping("/markReadAsync")
    @ResponseBody
    public ResponseEntity<String> markAsReadAsync(@RequestParam Long id, @RequestParam boolean read) {
        bookRepository.findById(id).ifPresent(book -> {
            book.setRead(read);
            bookRepository.save(book);
        });
        return ResponseEntity.ok("Статус обновлён");
    }

    @GetMapping("/search")
    public String searchBooks(@RequestParam String query, Model model) {
        List<Book> searchResults = googleBooksService.searchBook(query);
        model.addAttribute("books", bookRepository.findAll());
        model.addAttribute("searchResults", searchResults);
        return "books";
    }

    @PostMapping("/addFromSearch")
    public String addFromSearch(@RequestParam String title, @RequestParam String author,
                                @RequestParam(required = false) String isbn,
                                @RequestParam(required = false) String coverImageUrl,
                                @RequestParam(required = false) Integer pageCount) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setRead(false);
        book.setIsbn(isbn);
        book.setCoverImageUrl(coverImageUrl);
        book.setPageCount(pageCount);
        bookRepository.save(book);
        return "redirect:/books";
    }

}
