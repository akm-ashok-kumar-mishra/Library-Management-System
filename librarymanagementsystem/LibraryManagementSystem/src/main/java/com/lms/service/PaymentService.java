package com.lms.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.lms.model.Issue;
import com.lms.model.Payment;
import com.lms.repository.IssueRepository;
import com.lms.repository.PaymentRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private IssueRepository issueRepository;  // ← MAKE SURE THIS IS HERE

    public Payment collectPayment(Long issueId, String paymentMode) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        Optional<Payment> existing = paymentRepository.findByIssue_Id(issueId);
        if (existing.isPresent()) {
            throw new RuntimeException("Payment already recorded for this issue.");
        }

        // FIX: update issue status to RETURNED
        issue.setStatus(Issue.IssueStatus.RETURNED);
        issueRepository.save(issue);  // ← saves status change

        Payment payment = new Payment();
        payment.setIssue(issue);
        double fine = (issue.getFineAmount() != null) ? issue.getFineAmount() : 0.0;
        payment.setAmountPaid(fine);
        payment.setPaymentDate(LocalDate.now());
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setPaymentMode(Payment.PaymentMode.valueOf(paymentMode));

        return paymentRepository.save(payment);
    }

    public Payment waiveFine(Long issueId) {
        Issue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new RuntimeException("Issue not found"));

        Optional<Payment> existing = paymentRepository.findByIssue_Id(issueId);
        if (existing.isPresent()) {
            throw new RuntimeException("Fine already processed for this issue.");
        }

        // FIX: update issue status to RETURNED
        issue.setStatus(Issue.IssueStatus.RETURNED);
        issueRepository.save(issue);  // ← saves status change

        Payment payment = new Payment();
        payment.setIssue(issue);
        payment.setAmountPaid(0.0);
        payment.setPaymentDate(LocalDate.now());
        payment.setStatus(Payment.PaymentStatus.WAIVED);
        payment.setPaymentMode(null);

        return paymentRepository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<Payment> getPaymentByIssueId(Long issueId) {
        return paymentRepository.findByIssue_Id(issueId);
    }
    
    
}