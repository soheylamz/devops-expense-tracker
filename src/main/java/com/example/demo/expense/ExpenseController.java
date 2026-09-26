package com.example.demo.expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RESTful API des AusgabenTrackers: Verwaltung von Ausgaben inkl.
 * Kategorisierung und Summen-Auswertung.
 */
@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @GetMapping
    public List<Expense> findAll(@RequestParam(required = false) ExpenseCategory category) {
        return expenseService.findAll(category);
    }

    @GetMapping("/summary")
    public ExpenseSummary summary() {
        return expenseService.summary();
    }

    @GetMapping("/{id}")
    public Expense findById(@PathVariable Long id) {
        return expenseService.findById(id);
    }

    @PostMapping
    public ResponseEntity<Expense> create(@RequestBody ExpenseRequest request) {
        Expense created = expenseService.create(request.description(), request.amount(), request.category(), request.date());
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public Expense update(@PathVariable Long id, @RequestBody ExpenseRequest request) {
        return expenseService.update(id, request.description(), request.amount(), request.category(), request.date());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        expenseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ExpenseNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ExpenseNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    public record ExpenseRequest(String description, BigDecimal amount, ExpenseCategory category, LocalDate date) {
    }
}
