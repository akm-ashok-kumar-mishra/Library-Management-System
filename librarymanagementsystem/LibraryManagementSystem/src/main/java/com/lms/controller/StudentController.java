package com.lms.controller;

import com.lms.model.Student;
import com.lms.service.StudentService;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    private static final List<String> DEPARTMENTS = List.of("CSE", "ECE", "MECH", "CIVIL");

    // LIST + SEARCH + PAGINATION
    @GetMapping
    public String listStudents(Model model, @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page) {

        int size = 5;
        Page<Student> studentPage;

        if (search != null && !search.trim().isEmpty()) {
            studentPage = studentService.searchStudents(search.trim(), page, size);
        } else {
            studentPage = studentService.getPaginated(page, size);
        }

        model.addAttribute("students", studentPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", studentPage.getTotalPages());
        model.addAttribute("search", search);

        return "students/student-list";
    }

    // SHOW ADD FORM
    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("student", new Student());
        model.addAttribute("departments", DEPARTMENTS);
        return "students/student-add";
    }

    // SAVE STUDENT
    @PostMapping("/add")
    public String addStudent(@Valid @ModelAttribute("student") Student student, BindingResult result, Model model,
            RedirectAttributes redirectAttrs) {

        if (result.hasErrors()) {
            model.addAttribute("departments", DEPARTMENTS);
            return "students/student-add";
        }

        if (studentService.isStudentIdExists(student.getStudentId())) {
            model.addAttribute("errorMessage", "Student ID already exists.");
            model.addAttribute("departments", DEPARTMENTS);
            return "students/student-add";
        }

        if (studentService.isEmailExists(student.getEmail())) {
            model.addAttribute("errorMessage", "Email already exists.");
            model.addAttribute("departments", DEPARTMENTS);
            return "students/student-add";
        }

        studentService.saveStudent(student);
        redirectAttrs.addFlashAttribute("successMessage", "Student added successfully!");

        return "redirect:/students";
    }

    // SHOW EDIT FORM
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttrs) {

        try {
            Student student = studentService.getStudentById(id)
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            model.addAttribute("student", student);
            model.addAttribute("departments", DEPARTMENTS);

            return "students/student-edit";

        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/students";
        }
    }

    // UPDATE STUDENT
    @PostMapping("/edit/{id}")
    public String updateStudent(@PathVariable Long id, @Valid @ModelAttribute("student") Student student,
            BindingResult result, Model model, RedirectAttributes redirectAttrs) {

        if (result.hasErrors()) {
            model.addAttribute("departments", DEPARTMENTS);
            return "students/student-edit";
        }

        if (studentService.isStudentIdExistsForOtherStudent(student.getStudentId(), id)) {
            model.addAttribute("errorMessage", "Student ID already exists.");
            model.addAttribute("departments", DEPARTMENTS);
            return "students/student-edit";
        }

        if (studentService.isEmailExistsForOtherStudent(student.getEmail(), id)) {
            model.addAttribute("errorMessage", "Email already exists.");
            model.addAttribute("departments", DEPARTMENTS);
            return "students/student-edit";
        }

        try {
            studentService.updateStudent(id, student);
            redirectAttrs.addFlashAttribute("successMessage", "Student updated successfully!");
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/students";
    }

    // DELETE STUDENT
    @PostMapping("/delete/{id}")
    public String deleteStudent(@PathVariable Long id, RedirectAttributes redirectAttrs) {

        try {
            studentService.deleteStudent(id);
            redirectAttrs.addFlashAttribute("successMessage", "Student deleted successfully!");
        } catch (RuntimeException ex) {
            redirectAttrs.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/students";
    }
}