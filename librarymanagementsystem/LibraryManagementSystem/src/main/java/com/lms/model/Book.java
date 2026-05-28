package com.lms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "books")
public class Book {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank(message = "Title is required")
	@Column(nullable = false)
	private String title;

	@NotBlank(message = "Author is required")
	@Column(nullable = false)
	private String author;

	@NotBlank(message = "ISBN is required")
	@Column(nullable = false, unique = true)
	private String isbn;

	@Column(name = "publication_date")
	private LocalDate publicationDate;

	@NotNull(message = "Available copies is required")
	@Column(name = "available_copies", nullable = false)
	private Integer availableCopies = 0;

	@NotNull(message = "Total copies is required")
	@Column(name = "total_copies", nullable = false)
	private Integer totalCopies = 0;

	//  Prevent recursion if Book is returned
	@OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
	@JsonIgnore
	private List<Issue> issues;

	public Book() {
	}

	public Book(String title, String author, String isbn, LocalDate publicationDate, Integer totalCopies) {
		this.title = title;
		this.author = author;
		this.isbn = isbn;
		this.publicationDate = publicationDate;
		this.totalCopies = totalCopies != null ? totalCopies : 0;
		this.availableCopies = this.totalCopies;
	}

	// Getters & Setters

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public LocalDate getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(LocalDate publicationDate) {
		this.publicationDate = publicationDate;
	}

	public Integer getAvailableCopies() {
		return availableCopies;
	}

	public void setAvailableCopies(Integer availableCopies) {
		this.availableCopies = availableCopies;
	}

	public Integer getTotalCopies() {
		return totalCopies;
	}

	public void setTotalCopies(Integer totalCopies) {
		this.totalCopies = totalCopies;
	}

	public boolean isAvailable() {
		return availableCopies != null && availableCopies > 0;
	}
}