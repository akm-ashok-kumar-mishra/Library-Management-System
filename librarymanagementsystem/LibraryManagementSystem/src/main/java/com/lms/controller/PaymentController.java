package com.lms.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.lms.model.Issue;
import com.lms.model.Payment;
import com.lms.service.IssueService;
import com.lms.service.PaymentService;

@Controller
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private IssueService issueService;

   
    @GetMapping
    public String listPayments(Model model) {
        var payments = paymentService.getAllPayments();

        long waivedCount = payments.stream()
            .filter(p -> p.getStatus() == Payment.PaymentStatus.WAIVED)
            .count();

        double totalCollected = payments.stream()
            .filter(p -> p.getAmountPaid() != null)
            .mapToDouble(p -> p.getAmountPaid())
            .sum();

        model.addAttribute("payments", payments);
        model.addAttribute("waivedCount", waivedCount);
        model.addAttribute("totalCollected", totalCollected);
        return "payments/payment-list";
    }

    // Show payment form
    @GetMapping("/collect/{issueId}")
    public String showPaymentForm(@PathVariable Long issueId,
                                   Model model,
                                   RedirectAttributes redirectAttrs) {
        Issue issue = issueService.getIssueById(issueId).orElse(null);

        if (issue == null) {
            redirectAttrs.addFlashAttribute("errorMessage", "Issue not found.");
            return "redirect:/issues";
        }

        if (issue.getFineAmount() == null || issue.getFineAmount() == 0) {
            redirectAttrs.addFlashAttribute("errorMessage", "No fine to collect.");
            return "redirect:/issues";
        }

        model.addAttribute("issue", issue);
        return "payments/payment-collect";
    }

 // Collect payment
    @PostMapping("/collect/{issueId}")
    public String collectPayment(@PathVariable Long issueId,
                                  @RequestParam String paymentMode,
                                  RedirectAttributes redirectAttrs) {
        try {
            paymentService.collectPayment(issueId, paymentMode);
            redirectAttrs.addFlashAttribute("successMessage",
                    "Payment collected successfully!");
        } catch (Exception e) {
            e.printStackTrace(); // ADD THIS LINE
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/payments";
    }

    // Waive fine
    @PostMapping("/waive/{issueId}")
    public String waiveFine(@PathVariable Long issueId,
                             RedirectAttributes redirectAttrs) {
        try {
            paymentService.waiveFine(issueId);
            redirectAttrs.addFlashAttribute("successMessage",
                    "Fine waived successfully!");
        } catch (Exception e) {
            e.printStackTrace(); 
            redirectAttrs.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/payments";
    }
    
    
    
   
}
