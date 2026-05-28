package com.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lms.model.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

	Optional<Book> findByIsbn(String isbn);

	List<Book> findByTitleContainingIgnoreCase(String title);

	List<Book> findByAuthorContainingIgnoreCase(String author);

	@Query("SELECT b FROM Book b WHERE b.availableCopies > 0")
	List<Book> findAvailableBooks();

	@Query("SELECT b FROM Book b WHERE " + "LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	List<Book> searchBooks(@Param("keyword") String keyword);

	boolean existsByIsbn(String isbn);
}
