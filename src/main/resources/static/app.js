const API_BASE = "/api/expenses";

const CATEGORY_LABELS = {
    WOHNEN: "Wohnen",
    LEBENSMITTEL: "Lebensmittel",
    TRANSPORT: "Transport",
    FREIZEIT: "Freizeit",
    GESUNDHEIT: "Gesundheit",
    SONSTIGES: "Sonstiges",
};

const form = document.getElementById("expense-form");
const idField = document.getElementById("expense-id");
const descriptionField = document.getElementById("description");
const amountField = document.getElementById("amount");
const categoryField = document.getElementById("category");
const dateField = document.getElementById("date");
const formTitle = document.getElementById("form-title");
const submitButton = document.getElementById("submit-button");
const cancelButton = document.getElementById("cancel-button");
const errorMessage = document.getElementById("error-message");
const categoryFilter = document.getElementById("category-filter");
const rowsBody = document.getElementById("expense-rows");
const emptyMessage = document.getElementById("empty-message");
const summaryTotal = document.getElementById("summary-total");
const summaryByCategory = document.getElementById("summary-by-category");

function formatEuro(value) {
    return new Intl.NumberFormat("de-DE", { style: "currency", currency: "EUR" }).format(value);
}

function showError(message) {
    errorMessage.textContent = message;
    errorMessage.hidden = !message;
}

function resetForm() {
    form.reset();
    idField.value = "";
    formTitle.textContent = "Neue Ausgabe";
    submitButton.textContent = "Anlegen";
    cancelButton.hidden = true;
    dateField.value = new Date().toISOString().slice(0, 10);
}

async function apiRequest(url, options) {
    const response = await fetch(url, options);
    if (!response.ok) {
        const text = await response.text();
        throw new Error(text || `Anfrage fehlgeschlagen (HTTP ${response.status})`);
    }
    if (response.status === 204) {
        return null;
    }
    return response.json();
}

async function loadExpenses() {
    const category = categoryFilter.value;
    const url = category ? `${API_BASE}?category=${category}` : API_BASE;
    const expenses = await apiRequest(url);

    rowsBody.innerHTML = "";
    emptyMessage.hidden = expenses.length > 0;

    for (const expense of expenses) {
        const row = document.createElement("tr");
        row.innerHTML = `
            <td>${expense.date}</td>
            <td>${expense.description}</td>
            <td>${CATEGORY_LABELS[expense.category] ?? expense.category}</td>
            <td class="amount">${formatEuro(expense.amount)}</td>
            <td class="actions">
                <button type="button" class="link-button" data-action="edit">Bearbeiten</button>
                <button type="button" class="link-button danger" data-action="delete">Löschen</button>
            </td>
        `;
        row.querySelector('[data-action="edit"]').addEventListener("click", () => startEdit(expense));
        row.querySelector('[data-action="delete"]').addEventListener("click", () => deleteExpense(expense.id));
        rowsBody.appendChild(row);
    }
}

async function loadSummary() {
    const summary = await apiRequest(`${API_BASE}/summary`);
    summaryTotal.textContent = formatEuro(summary.total);

    summaryByCategory.innerHTML = "";
    for (const [category, amount] of Object.entries(summary.byCategory)) {
        const item = document.createElement("li");
        item.textContent = `${CATEGORY_LABELS[category] ?? category}: ${formatEuro(amount)}`;
        summaryByCategory.appendChild(item);
    }
}

async function refresh() {
    try {
        await Promise.all([loadExpenses(), loadSummary()]);
    } catch (err) {
        showError(err.message);
    }
}

function startEdit(expense) {
    idField.value = expense.id;
    descriptionField.value = expense.description;
    amountField.value = expense.amount;
    categoryField.value = expense.category;
    dateField.value = expense.date;
    formTitle.textContent = `Ausgabe #${expense.id} bearbeiten`;
    submitButton.textContent = "Aktualisieren";
    cancelButton.hidden = false;
    descriptionField.focus();
}

async function deleteExpense(id) {
    if (!confirm("Diese Ausgabe wirklich löschen?")) {
        return;
    }
    try {
        await apiRequest(`${API_BASE}/${id}`, { method: "DELETE" });
        await refresh();
    } catch (err) {
        showError(err.message);
    }
}

form.addEventListener("submit", async (event) => {
    event.preventDefault();
    showError("");

    const payload = {
        description: descriptionField.value,
        amount: Number(amountField.value),
        category: categoryField.value,
        date: dateField.value,
    };

    const id = idField.value;
    const url = id ? `${API_BASE}/${id}` : API_BASE;
    const method = id ? "PUT" : "POST";

    try {
        await apiRequest(url, {
            method,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        });
        resetForm();
        await refresh();
    } catch (err) {
        showError(err.message);
    }
});

cancelButton.addEventListener("click", resetForm);
categoryFilter.addEventListener("change", loadExpenses);

resetForm();
refresh();
