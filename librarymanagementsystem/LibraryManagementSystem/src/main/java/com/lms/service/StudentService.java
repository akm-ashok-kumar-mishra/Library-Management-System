package com.lms.service;

import com.lms.exception.BadRequestException;
import com.lms.exception.ResourceNotFoundException;
import com.lms.model.Student;
import com.lms.repository.StudentRepository;
import com.lms.repository.IssueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentService {

	@Autowired
	private StudentRepository studentRepository;

	@Autowired
	private IssueRepository issueRepository;

	public List<Student> getAllStudents() {
		return studentRepository.findAll();
	}

	public Optional<Student> getStudentById(Long id) {
		return studentRepository.findById(id);
	}

	public Optional<Student> getStudentByStudentId(String studentId) {
		return studentRepository.findByStudentId(studentId);
	}

	public Optional<Student> getStudentByEmail(String email) {
		return studentRepository.findByEmail(email);
	}

	//  Option 1 — wrap List result in a PageImpl
	public Page<Student> searchStudents(String keyword, int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		List<Student> results = studentRepository.searchStudents(keyword); // full search
		int start = (int) pageable.getOffset();
		int end = Math.min(start + size, results.size());
		List<Student> pageContent = results.subList(start, end);
		return new PageImpl<>(pageContent, pageable, results.size());
	}

	public List<Student> findByDepartment(String department) {
		return studentRepository.findByDepartmentIgnoreCase(department);
	}

	public List<String> getAllDepartments() {
		return studentRepository.findAllDepartments();
	}

	public Student saveStudent(Student student) {
		return studentRepository.save(student);
	}

	public Student updateStudent(Long id, Student studentDetails) {
		Student student = studentRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

		student.setName(studentDetails.getName());
		student.setEmail(studentDetails.getEmail());
		student.setStudentId(studentDetails.getStudentId());
		student.setDepartment(studentDetails.getDepartment());
		student.setPhone(studentDetails.getPhone());

		return studentRepository.save(student);
	}

	public void deleteStudent(Long id) {
		if (!studentRepository.existsById(id)) {
			throw new ResourceNotFoundException("Student not found with id: " + id);
		}

		long activeIssues = issueRepository.countActiveIssuesByStudent(id);
		if (activeIssues > 0) {
			throw new BadRequestException(
					"Cannot delete student with active book issues. Please return all books first.");
		}

		studentRepository.deleteById(id);
	}

	public boolean isStudentIdExists(String studentId) {
		return studentRepository.existsByStudentId(studentId);
	}

	public boolean isStudentIdExistsForOtherStudent(String studentId, Long id) {
		Optional<Student> existingStudent = studentRepository.findByStudentId(studentId);
		return existingStudent.isPresent() && !existingStudent.get().getId().equals(id);
	}

	public boolean isEmailExists(String email) {
		return studentRepository.existsByEmail(email);
	}

	public boolean isEmailExistsForOtherStudent(String email, Long id) {
		Optional<Student> existingStudent = studentRepository.findByEmail(email);
		return existingStudent.isPresent() && !existingStudent.get().getId().equals(id);
	}

	public long getActiveIssuesCount(Long studentId) {
		return issueRepository.countActiveIssuesByStudent(studentId);
	}

	public boolean canIssueMoreBooks(Long studentId) {
		return getActiveIssuesCount(studentId) < 5;
	}

	public Page<Student> getPaginated(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return studentRepository.findAll(pageable);
	}
}
