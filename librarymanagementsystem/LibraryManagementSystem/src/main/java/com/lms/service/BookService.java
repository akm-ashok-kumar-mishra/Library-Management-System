package com.lms.service;

import com.lms.exception.BadRequestException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.model.Book;
import com.lms.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookService {

	@Autowired
	private BookRepository bookRepository;

	public List<Book> getAllBooks() {
		return bookRepository.findAll();
	}

	public Optional<Book> getBookById(Long id) {
		return bookRepository.findById(id);
	}

	public Optional<Book> getBookByIsbn(String isbn) {
		return bookRepository.findByIsbn(isbn);
	}

	public List<Book> getAvailableBooks() {
		return bookRepository.findAvailableBooks();
	}

	public List<Book> searchBooks(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return getAllBooks();
		}
		return bookRepository.searchBooks(keyword.trim());
	}

	public List<Book> findByTitle(String title) {
		return bookRepository.findByTitleContainingIgnoreCase(title);
	}

	public List<Book> findByAuthor(String author) {
		return bookRepository.findByAuthorContainingIgnoreCase(author);
	}

	public Book saveBook(Book book) {
	    if (book.getTotalCopies() == null || book.getTotalCopies() < 0) {
	        book.setTotalCopies(0);
	    }
	    //  Always set availableCopies = totalCopies on new book creation
	    // Don't trust what the form sends for availableCopies
	    book.setAvailableCopies(book.getTotalCopies());

	    return bookRepository.save(book);
	}

	public Book updateBook(Long id, Book bookDetails) {
		Book book = bookRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));

		book.setTitle(bookDetails.getTitle());
		book.setAuthor(bookDetails.getAuthor());
		book.setIsbn(bookDetails.getIsbn());
		book.setPublicationDate(bookDetails.getPublicationDate());

		int issuedCopies = book.getTotalCopies() - book.getAvailableCopies();
		book.setTotalCopies(bookDetails.getTotalCopies());
		book.setAvailableCopies(Math.max(0, bookDetails.getTotalCopies() - issuedCopies));

		return bookRepository.save(book);
	}

	public void deleteBook(Long id) {
		if (!bookRepository.existsById(id)) {
			throw new ResourceNotFoundException("Book not found with id: " + id);
		}
		bookRepository.deleteById(id);
	}

	public boolean isIsbnExists(String isbn) {
		return bookRepository.existsByIsbn(isbn);
	}

	public boolean isIsbnExistsForOtherBook(String isbn, Long bookId) {
		Optional<Book> existingBook = bookRepository.findByIsbn(isbn);
		return existingBook.isPresent() && !existingBook.get().getId().equals(bookId);
	}

	public boolean issueBook(Long bookId) {
		Book book = bookRepository.findById(bookId).orElseThrow(() -> new ResourceNotFoundException("Book not found"));

		if (book.getAvailableCopies() <= 0) {
			throw new BadRequestException("No copies available");
		}

		book.setAvailableCopies(book.getAvailableCopies() - 1);
		bookRepository.save(book);

		return true;
	}

	public void returnBook(Long bookId) {
		Book book = bookRepository.findById(bookId).orElseThrow(() -> new ResourceNotFoundException("Book not found"));

		if (book.getAvailableCopies() >= book.getTotalCopies()) {
			throw new BadRequestException("All copies are already returned");
		}

		book.setAvailableCopies(book.getAvailableCopies() + 1);
		bookRepository.save(book);

	}
}