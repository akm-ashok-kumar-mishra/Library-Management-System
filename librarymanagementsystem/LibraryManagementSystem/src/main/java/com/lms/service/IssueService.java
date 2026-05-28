package com.lms.service;

import com.lms.exception.BadRequestException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.model.Book;
import com.lms.model.Issue;
import com.lms.model.Student;
import com.lms.model.Issue.IssueStatus;
import com.lms.repository.IssueRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class IssueService {

	@Autowired
	private IssueRepository issueRepository;

	@Autowired
	private BookService bookService;

	@Autowired
	private StudentService studentService;

	private static final double FINE_PER_DAY = 5.0;

	public List<Issue> getAllIssues() {
		return issueRepository.findAll();
	}

	public Optional<Issue> getIssueById(Long id) {
		return issueRepository.findById(id);
	}

	public List<Issue> findIssuesByStatus(IssueStatus status) {
		return issueRepository.findByStatus(status);
	}

	public List<Issue> getIssuesByBookId(Long bookId) {
		return issueRepository.findByBook_Id(bookId); 
	}

	public List<Issue> getIssuesByStudentId(Long studentId) {
		return issueRepository.findByStudent_Id(studentId); 
	}

	public List<Issue> findOverdueIssues() {
		return issueRepository.findOverdueIssues(IssueStatus.ISSUED, LocalDate.now()); 
	}

	public void issueBook(Issue issue) {

		Book book = bookService.getBookById(issue.getBook().getId())
				.orElseThrow(() -> new ResourceNotFoundException("Book not found"));

		if (book.getAvailableCopies() <= 0) {
			throw new BadRequestException("No copies available");
		}

		Student student = studentService.getStudentById(issue.getStudent().getId())
				.orElseThrow(() -> new ResourceNotFoundException("Student not found"));

		if (!studentService.canIssueMoreBooks(student.getId())) {
			throw new BadRequestException("Student reached limit");
		}

		issue.setIssueDate(LocalDate.now());
		issue.setDueDate(LocalDate.now().plusDays(14));
		issue.setStatus(IssueStatus.ISSUED);
		issue.setFineAmount(0.0);
		issue.setReturnDate(null);

		bookService.issueBook(book.getId());
		issueRepository.save(issue);
	}

	public void returnBook(Long issueId) {

		Issue issue = issueRepository.findById(issueId)
				.orElseThrow(() -> new ResourceNotFoundException("Issue not found"));

		if (issue.getStatus() != IssueStatus.ISSUED) {
			throw new BadRequestException("Book already returned");
		}

		LocalDate returnDate = LocalDate.now();
		issue.setReturnDate(returnDate);

		if (returnDate.isAfter(issue.getDueDate())) {
			long days = java.time.temporal.ChronoUnit.DAYS.between(issue.getDueDate(), returnDate);

			issue.setFineAmount(days * FINE_PER_DAY);
			issue.setStatus(IssueStatus.OVERDUE);
		} else {
			issue.setFineAmount(0.0);
			issue.setStatus(IssueStatus.RETURNED);
		}

		bookService.returnBook(issue.getBook().getId());
		issueRepository.save(issue);
	}
}