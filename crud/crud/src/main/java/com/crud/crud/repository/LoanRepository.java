package com.crud.crud.repository;
import java.util.List;
import com.crud.crud.model.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LoanRepository extends JpaRepository<Loan,Long> {
    List<Loan> findByStudentId(Long studentId);

    @Query(value = """
            Select\s
                s.id as studentId,
                s.name as studentName,
                Count(l.id) as totalLoans,
                Sum(l.amount) as total,
                Avg(l.amount) as avgLoan,
            from student s
            Join loan l on s.id = l.student_id
            group by s.id,s.name
            having sum(l.amount) > :minAmount
            order by totalAmount desc
           \s""",nativeQuery = true)
    List<Object[]> getStudentLoanStats(double minAmount);

}
