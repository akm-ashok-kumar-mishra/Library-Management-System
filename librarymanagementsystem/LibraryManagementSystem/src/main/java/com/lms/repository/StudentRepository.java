package com.lms.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.lms.model.Student;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

	Optional<Student> findByStudentId(String studentId);

	Optional<Student> findByEmail(String email);

	Page<Student> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

	List<Student> findByDepartmentIgnoreCase(String department);

	@Query("SELECT s FROM Student s WHERE " + "LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(s.studentId) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
			+ "LOWER(s.department) LIKE LOWER(CONCAT('%', :keyword, '%'))")
	List<Student> searchStudents(@Param("keyword") String keyword);

	boolean existsByStudentId(String studentId);

	boolean existsByEmail(String email);

	@Query("SELECT DISTINCT s.department FROM Student s WHERE s.department IS NOT NULL ORDER BY s.department")
	List<String> findAllDepartments();

}
