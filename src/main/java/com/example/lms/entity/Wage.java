package com.example.lms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "wages", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "assignment_id", "calculated_date" })
})
public class Wage extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @OneToMany(mappedBy = "wage")
    @JsonIgnoreProperties("wage")
    private List<Payment> payments;

    private LocalDate date;
    private Double hoursWorked;
    private Double calculatedWage;

    private Integer daysWorked;
    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private WageStatus status = WageStatus.PENDING;

    private LocalDate calculatedDate;
    private LocalDate settledDate;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment = assignment;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Double getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(Double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public Double getCalculatedWage() {
        return calculatedWage;
    }

    public void setCalculatedWage(Double calculatedWage) {
        this.calculatedWage = calculatedWage;
    }

    public Integer getDaysWorked() {
        return daysWorked;
    }

    public void setDaysWorked(Integer daysWorked) {
        this.daysWorked = daysWorked;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public WageStatus getStatus() {
        return status;
    }

    public void setStatus(WageStatus status) {
        this.status = status;
    }

    public LocalDate getCalculatedDate() {
        return calculatedDate;
    }

    public void setCalculatedDate(LocalDate calculatedDate) {
        this.calculatedDate = calculatedDate;
    }

    public LocalDate getSettledDate() {
        return settledDate;
    }

    public void setSettledDate(LocalDate settledDate) {
        this.settledDate = settledDate;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }
}
