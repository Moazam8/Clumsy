import test from "node:test";
import assert from "node:assert/strict";
import { summarizeDebts } from "../native/debt.mjs";

test("calculates per-person lending, borrowing, repayments, and history", () => {
  const people = summarizeDebts([
    { id: "1", type: "lent", person: "Amina", amount: 500, date: "2026-01-01" },
    { id: "2", type: "repayment", person: "amina", amount: 125, date: "2026-01-03" },
    { id: "3", type: "borrowed", person: "Bilal", amount: 300, date: "2026-01-02" },
    { id: "4", type: "debt_payment", person: "Bilal", amount: 100, date: "2026-01-04" },
    { id: "5", type: "expense", amount: 80, date: "2026-01-05" },
    { id: "6", type: "investment", person: "Amina", amount: 250, date: "2026-01-06", note: "Shop setup" }
  ]);

  assert.equal(people.length, 2);
  assert.deepEqual(
    { name: people[0].name, owed: people[0].owedToYou, youOwe: people[0].youOwe, invested: people[0].invested, history: people[0].history.length },
    { name: "Amina", owed: 375, youOwe: 0, invested: 250, history: 3 }
  );
  assert.deepEqual(
    { name: people[1].name, owed: people[1].owedToYou, youOwe: people[1].youOwe, history: people[1].history.length },
    { name: "Bilal", owed: 0, youOwe: 200, history: 2 }
  );
});

test("keeps historical records without a person in an unassigned group", () => {
  const [unassigned] = summarizeDebts([
    { id: "old-loan", type: "lent", amount: 90, date: "2025-12-01" },
    { id: "old-repayment", type: "repayment", amount: 20, date: "2025-12-02" }
  ]);

  assert.equal(unassigned.name, "Unassigned");
  assert.equal(unassigned.owedToYou, 70);
  assert.equal(unassigned.history.length, 2);
});
