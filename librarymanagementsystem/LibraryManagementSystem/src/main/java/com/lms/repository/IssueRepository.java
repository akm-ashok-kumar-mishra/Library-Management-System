package com.lms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lms.model.Issue;
import com.lms.model.Issue.IssueStatus;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface IssueRepository extends JpaRepository<Issue, Long> {

	// Find issues by student ID
	List<Issue> findByStudent_Id(Long studentId);

	// Find issues by book ID
	List<Issue> findByBook_Id(Long bookId);

	// Find issues by status
	List<Issue> findByStatus(IssueStatus status);

	// Find overdue issues
	@Query("SELECT i FROM Issue i WHERE i.status = :status AND i.dueDate < :currentDate")
	List<Issue> findOverdueIssues(@Param("status") IssueStatus status, @Param("currentDate") LocalDate currentDate);

	// Active issues for a student
	@Query("SELECT i FROM Issue i WHERE i.student.id = :studentId AND i.status = 'ISSUED'")
	List<Issue> findActiveIssuesByStudent(@Param("studentId") Long studentId);

	// Active issues for a book
	@Query("SELECT i FROM Issue i WHERE i.book.id = :bookId AND i.status = 'ISSUED'")
	List<Issue> findActiveIssuesByBook(@Param("bookId") Long bookId);

	// Count active issues for a student
	@Query("SELECT COUNT(i) FROM Issue i WHERE i.student.id = :studentId AND i.status = 'ISSUED'")
	long countActiveIssuesByStudent(@Param("studentId") Long studentId);

	// Issues between dates
	@Query("SELECT i FROM Issue i WHERE i.issueDate BETWEEN :startDate AND :endDate")
	List<Issue> findIssuesBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

	// Returns between dates
	@Query("SELECT i FROM Issue i WHERE i.returnDate BETWEEN :startDate AND :endDate")
	List<Issue> findReturnsBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}