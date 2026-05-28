package com.lms.controller;

import com.lms.exception.ResourceNotFoundException;
import com.lms.model.Book;
import com.lms.service.BookService;
import com.lms.service.IssueService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/books")
public class BookController {

	@Autowired
	private BookService bookService;

	@Autowired
	private IssueService issueService;

	@GetMapping("/")
	public String home() {
		return "redirect:/books";
	}

	@GetMapping
	public String listBooks(Model model, @RequestParam(value = "search", required = false) String search) {

		List<Book> books = (search != null && !search.trim().isEmpty()) ? bookService.searchBooks(search.trim())
				: bookService.getAllBooks();

		long availableCount = books.stream().filter(b -> b.getAvailableCopies() != null && b.getAvailableCopies() > 0)
				.count();

		long issuedCount = books.size() - availableCount;

		model.addAttribute("books", books);
		model.addAttribute("search", search);
		model.addAttribute("availableCount", availableCount);
		model.addAttribute("issuedCount", issuedCount);

		return "books/list";
	}

	@GetMapping("/add")
	public String showAddForm(Model model) {
		model.addAttribute("book", new Book());
		return "books/add";
	}

	@PostMapping("/add")
	public String addBook(@Valid @ModelAttribute("book") Book book, BindingResult result, Model model,
			RedirectAttributes redirectAttrs) {

		if (result.hasErrors()) return "books/add";

		if (bookService.isIsbnExists(book.getIsbn())) {
			model.addAttribute("errorMessage", "ISBN already exists.");
			return "books/add";
		}

		bookService.saveBook(book);
		redirectAttrs.addFlashAttribute("successMessage", "Book added successfully!");
		return "redirect:/books";
	}

	@GetMapping("/edit/{id}")
	public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {

		Book book = bookService.getBookById(id).orElseThrow(() -> new ResourceNotFoundException("Book Not Available"));

		if (book == null) {
			redirectAttrs.addFlashAttribute("errorMessage", "Book not found.");
			return "books/edit";
		}

		model.addAttribute("book", book);
		model.addAttribute("bookIssues", issueService.getIssuesByBookId(id));

		return "books/edit";
	}

	@PostMapping("/edit/{id}")
	public String updateBook(@PathVariable Long id, @Valid @ModelAttribute("book") Book book, BindingResult result,
			Model model, RedirectAttributes redirectAttrs) {

		if (result.hasErrors()) return "books/edit";

		if (bookService.isIsbnExistsForOtherBook(book.getIsbn(), id)) {
			model.addAttribute("errorMessage", "ISBN already exists.");
			return "books/edit";
		}

		try {
			bookService.updateBook(id, book);
			redirectAttrs.addFlashAttribute("successMessage", "Book updated!");
		} catch (RuntimeException ex) {
			redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
		}

		return "redirect:/books";
	}

	@PostMapping("/delete/{id}")
	public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttrs) {

		try {
			bookService.deleteBook(id);
			redirectAttrs.addFlashAttribute("successMessage", "Book deleted!");
		} catch (RuntimeException ex) {
			redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
		}

		return "redirect:/books";
	}
}