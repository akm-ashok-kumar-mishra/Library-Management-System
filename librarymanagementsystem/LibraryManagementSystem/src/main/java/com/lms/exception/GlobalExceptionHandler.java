package com.lms.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

	// Resource Not Found
	@ExceptionHandler(ResourceNotFoundException.class)
	public String handleNotFound(ResourceNotFoundException ex, Model model) {
		model.addAttribute("error", ex.getMessage());
		return "error";
	}

	// Business Errors (validation, logic)
	@ExceptionHandler(BadRequestException.class)
	public String handleBadRequest(BadRequestException ex, Model model) {
		model.addAttribute("error", ex.getMessage());
		return "error";
	}

	// All Other Exceptions (fallback)
	@ExceptionHandler(Exception.class)
	public String handleGeneral(Exception ex, Model model) {
		model.addAttribute("error", "Something went wrong: " + ex.getMessage());
		return "error";
	}
}