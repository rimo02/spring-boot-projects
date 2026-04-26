package com.crud.crud.service;

import com.crud.crud.model.Loan;
import com.crud.crud.model.Student;
import com.crud.crud.repository.LoanRepository;
import com.crud.crud.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LoanService {
    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private StudentRepository studentRepository;

    public Loan issueLoan(Long studentId, Double amount) {
        Student student = studentRepository.findById(studentId).orElseThrow(()-> new RuntimeException("Student not found"));
        Loan loan = new Loan();
        loan.setAmount(amount);
        loan.setStudent(student);
        return loanRepository.save(loan);
    }

    public List<Loan> getLoansByStudent(Long studentId) {
        return loanRepository.findByStudentId(studentId);
    }

    public List<String> getStudentLoanStats(double minAmount){
        List<Object[]> rows = loanRepository.getStudentLoanStats(minAmount);
        List<String> result = new ArrayList<>();
        for (Object[] row : rows) {
            String data = "Student ID: " + row[0] +
                    ", Name: " + row[1] +
                    ", Total Loans: " + row[2] +
                    ", Total Amount: " + row[3] +
                    ", Avg Loan: " + row[4];

            result.add(data);
        }
        return result;
    }
}
