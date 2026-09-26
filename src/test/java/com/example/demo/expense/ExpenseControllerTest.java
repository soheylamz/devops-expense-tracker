package com.example.demo.expense;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ExpenseController.class)
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ExpenseService expenseService;

    @Test
    void createsExpense() throws Exception {
        Expense saved = new Expense("Wocheneinkauf", new BigDecimal("42.50"), ExpenseCategory.LEBENSMITTEL, LocalDate.of(2026, 8, 30));
        when(expenseService.create(any(), any(), any(), any())).thenReturn(saved);

        mockMvc.perform(post("/api/expenses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"description":"Wocheneinkauf","amount":42.50,"category":"LEBENSMITTEL","date":"2026-08-30"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description", is("Wocheneinkauf")))
                .andExpect(jsonPath("$.category", is("LEBENSMITTEL")));
    }

    @Test
    void listsExpenses() throws Exception {
        Expense expense = new Expense("Miete", new BigDecimal("650.00"), ExpenseCategory.WOHNEN, LocalDate.of(2026, 8, 1));
        when(expenseService.findAll(isNull())).thenReturn(List.of(expense));

        mockMvc.perform(get("/api/expenses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description", is("Miete")));
    }

    @Test
    void returnsSummary() throws Exception {
        ExpenseSummary summaryResult = new ExpenseSummary(
                new BigDecimal("692.50"),
                Map.of(ExpenseCategory.WOHNEN, new BigDecimal("650.00"), ExpenseCategory.LEBENSMITTEL, new BigDecimal("42.50")));
        when(expenseService.summary()).thenReturn(summaryResult);

        mockMvc.perform(get("/api/expenses/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(692.50)));
    }

    @Test
    void returnsNotFoundForUnknownExpense() throws Exception {
        when(expenseService.findById(999L)).thenThrow(new ExpenseNotFoundException(999L));

        mockMvc.perform(get("/api/expenses/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletesExpense() throws Exception {
        mockMvc.perform(delete("/api/expenses/{id}", 1L))
                .andExpect(status().isNoContent());
    }
}
