package com.example.demo.expense;

import java.math.BigDecimal;
import java.util.Map;

/** Ausgewertete Summen des AusgabenTrackers: Gesamtsumme und Summe je Kategorie. */
public record ExpenseSummary(BigDecimal total, Map<ExpenseCategory, BigDecimal> byCategory) {
}
