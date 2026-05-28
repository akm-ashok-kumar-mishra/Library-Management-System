package com.lms.controller;

import com.lms.model.*;
import com.lms.service.*;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/issues")
public class IssueController {

	@Autowired
	private IssueService issueService;

	@Autowired
	private BookService bookService;

	@Autowired
	private StudentService studentService;

	//  LIST ISSUES
	@GetMapping
	public String listIssues(Model model, @RequestParam(value = "status", required = false) String status) {

		List<Issue> issues;

		if ("OVERDUE".equalsIgnoreCase(status)) {
			issues = issueService.findOverdueIssues();
		} else {
			issues = issueService.getAllIssues();
		}

		model.addAttribute("issues", issues);
		model.addAttribute("filterStatus", status);

		return "issues/issue-list";
	}

	// SHOW ISSUE FORM
	@GetMapping("/issue")
	public String showIssueForm(Model model) {
		model.addAttribute("issue", new Issue());
		model.addAttribute("books", bookService.getAvailableBooks());
		model.addAttribute("students", studentService.getAllStudents());
		return "issues/issue-add"; 
	}

	// ISSUE BOOK
	@PostMapping("/issue")
	public String issueBook(@Valid @ModelAttribute("issue") Issue issue, BindingResult result, Model model,
			RedirectAttributes redirectAttrs) {

		if (result.hasErrors()) {
			model.addAttribute("books", bookService.getAvailableBooks());
			model.addAttribute("students", studentService.getAllStudents());
			return "issues/issue-add"; 
		}

		if (issue.getBook() == null || issue.getStudent() == null) {
			model.addAttribute("errorMessage", "Please select book and student.");
			return "issues/issue-add"; 
		}

		Long studentId = issue.getStudent().getId();

		if (!studentService.canIssueMoreBooks(studentId)) {
			model.addAttribute("errorMessage", "Student reached limit (5 books)");
			model.addAttribute("books", bookService.getAvailableBooks());
			model.addAttribute("students", studentService.getAllStudents());
			return "issues/issue-add"; 
		}

		try {
			issueService.issueBook(issue);
			redirectAttrs.addFlashAttribute("successMessage", "Book issued successfully!");
			return "redirect:/issues";
		} catch (RuntimeException ex) {
			model.addAttribute("errorMessage", ex.getMessage());
			model.addAttribute("books", bookService.getAvailableBooks());
			model.addAttribute("students", studentService.getAllStudents());
			return "issues/issue-add"; 
		}
	}

	// SHOW RETURN FORM
	@GetMapping("/return/{id}")
	public String showReturnForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {

		Issue issue = issueService.getIssueById(id).orElse(null);

		if (issue == null || issue.getStatus() != Issue.IssueStatus.ISSUED) {
			redirectAttrs.addFlashAttribute("errorMessage", "Invalid issue.");
			return "redirect:/issues";
		}

		model.addAttribute("issue", issue);
		return "issues/issue-return"; 
	}

	// RETURN BOOK
	@PostMapping("/return/{id}")
	public String returnBook(@PathVariable Long id, RedirectAttributes redirectAttrs) {

		try {
			issueService.returnBook(id);
			redirectAttrs.addFlashAttribute("successMessage", "Book returned successfully!");
		} catch (RuntimeException ex) {
			redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
		}

		return "redirect:/issues";
	}
}