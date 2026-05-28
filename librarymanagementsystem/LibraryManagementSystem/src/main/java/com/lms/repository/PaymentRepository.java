package com.lms.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lms.model.Payment;


@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long>{
	 Optional<Payment> findByIssue_Id(Long issueId);
	    List<Payment> findByStatus(Payment.PaymentStatus status);
	    
	

}
