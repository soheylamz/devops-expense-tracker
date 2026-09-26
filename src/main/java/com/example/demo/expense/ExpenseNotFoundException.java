package com.example.demo.expense;

/** Wird geworfen, wenn eine Ausgabe unter der angefragten ID nicht existiert. */
public class ExpenseNotFoundException extends RuntimeException {

    public ExpenseNotFoundException(Long id) {
        super("Ausgabe mit ID " + id + " wurde nicht gefunden");
    }
}
