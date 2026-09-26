package com.example.demo.expense;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integrationstest der Business-Logik gegen die Test-H2-DB
 * (siehe src/test/resources/application.properties). Prüft insbesondere die
 * Summen-Auswertung, die in ExpenseService.summary() berechnet wird.
 */
@SpringBootTest
class ExpenseServiceTest {

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Test
    void createsAndFindsExpense() {
        Expense created = expenseService.create("Miete", new BigDecimal("650.00"), ExpenseCategory.WOHNEN, LocalDate.of(2026, 8, 1));

        Expense found = expenseService.findById(created.getId());

        assertThat(found.getDescription()).isEqualTo("Miete");
        assertThat(found.getAmount()).isEqualByComparingTo("650.00");
    }

    @Test
    void updatesExpense() {
        Expense created = expenseService.create("Handy", new BigDecimal("20.00"), ExpenseCategory.SONSTIGES, LocalDate.of(2026, 8, 5));

        Expense updated = expenseService.update(created.getId(), "Handyvertrag", new BigDecimal("25.00"), ExpenseCategory.SONSTIGES, LocalDate.of(2026, 8, 5));

        assertThat(updated.getDescription()).isEqualTo("Handyvertrag");
        assertThat(updated.getAmount()).isEqualByComparingTo("25.00");
    }

    @Test
    void deletesExpense() {
        Expense created = expenseService.create("Kino", new BigDecimal("12.00"), ExpenseCategory.FREIZEIT, LocalDate.of(2026, 8, 10));
        Long id = created.getId();

        expenseService.delete(id);

        assertThatThrownBy(() -> expenseService.findById(id))
                .isInstanceOf(ExpenseNotFoundException.class);
    }

    @Test
    void computesSummaryPerCategoryAndTotal() {
        expenseRepository.deleteAll();
        expenseService.create("Miete", new BigDecimal("650.00"), ExpenseCategory.WOHNEN, LocalDate.of(2026, 8, 1));
        expenseService.create("Wocheneinkauf", new BigDecimal("42.50"), ExpenseCategory.LEBENSMITTEL, LocalDate.of(2026, 8, 15));
        expenseService.create("Zugticket", new BigDecimal("7.50"), ExpenseCategory.LEBENSMITTEL, LocalDate.of(2026, 8, 20));

        ExpenseSummary summary = expenseService.summary();

        assertThat(summary.total()).isEqualByComparingTo("700.00");
        assertThat(summary.byCategory().get(ExpenseCategory.WOHNEN)).isEqualByComparingTo("650.00");
        assertThat(summary.byCategory().get(ExpenseCategory.LEBENSMITTEL)).isEqualByComparingTo("50.00");
    }
}
