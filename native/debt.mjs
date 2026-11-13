const PERSON_ACTIVITY_TYPES = new Set(["lent", "borrowed", "repayment", "debt_payment", "investment"]);

export function summarizeDebts(transactions) {
  const people = new Map();
  for (const transaction of transactions) {
    if (!PERSON_ACTIVITY_TYPES.has(transaction.type)) continue;
    const name = typeof transaction.person === "string" ? transaction.person.trim() : "";
    const key = name ? name.toLocaleLowerCase() : "";
    const person = people.get(key) || {
      key,
      name: name || "Unassigned",
      lent: 0,
      repayments: 0,
      borrowed: 0,
      debtPayments: 0,
      invested: 0,
      history: []
    };
    const amount = Number(transaction.amount) || 0;
    if (transaction.type === "lent") person.lent += amount;
    if (transaction.type === "repayment") person.repayments += amount;
    if (transaction.type === "borrowed") person.borrowed += amount;
    if (transaction.type === "debt_payment") person.debtPayments += amount;
    if (transaction.type === "investment") person.invested += amount;
    person.history.push(transaction);
    people.set(key, person);
  }
  return [...people.values()]
    .map(person => ({
      ...person,
      owedToYou: Math.max(0, person.lent - person.repayments),
      youOwe: Math.max(0, person.borrowed - person.debtPayments),
      history: person.history.sort((a, b) => (a.date || "").localeCompare(b.date || "") || (a.createdAt || 0) - (b.createdAt || 0))
    }))
    .sort((a, b) => a.name.localeCompare(b.name));
}
