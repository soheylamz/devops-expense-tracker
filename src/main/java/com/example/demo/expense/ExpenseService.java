package com.example.demo.expense;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    public List<Expense> findAll(ExpenseCategory category) {
        if (category != null) {
            return expenseRepository.findByCategory(category);
        }
        return expenseRepository.findAll();
    }

    public Expense findById(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ExpenseNotFoundException(id));
    }

    public Expense create(String description, BigDecimal amount, ExpenseCategory category, LocalDate date) {
        Expense expense = new Expense(description, amount, category, date);
        return expenseRepository.save(expense);
    }

    public Expense update(Long id, String description, BigDecimal amount, ExpenseCategory category, LocalDate date) {
        Expense expense = findById(id);
        expense.setDescription(description);
        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setDate(date);
        return expenseRepository.save(expense);
    }

    public void delete(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new ExpenseNotFoundException(id);
        }
        expenseRepository.deleteById(id);
    }

    public ExpenseSummary summary() {
        List<Expense> all = expenseRepository.findAll();

        BigDecimal total = all.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<ExpenseCategory, BigDecimal> byCategory = all.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)));

        return new ExpenseSummary(total, byCategory);
    }
}
