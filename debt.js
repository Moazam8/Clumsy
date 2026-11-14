var ClumsyDebt = (() => {
  var __defProp = Object.defineProperty;
  var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
  var __getOwnPropNames = Object.getOwnPropertyNames;
  var __hasOwnProp = Object.prototype.hasOwnProperty;
  var __export = (target, all) => {
    for (var name in all)
      __defProp(target, name, { get: all[name], enumerable: true });
  };
  var __copyProps = (to, from, except, desc) => {
    if (from && typeof from === "object" || typeof from === "function") {
      for (let key of __getOwnPropNames(from))
        if (!__hasOwnProp.call(to, key) && key !== except)
          __defProp(to, key, { get: () => from[key], enumerable: !(desc = __getOwnPropDesc(from, key)) || desc.enumerable });
    }
    return to;
  };
  var __toCommonJS = (mod) => __copyProps(__defProp({}, "__esModule", { value: true }), mod);

  // native/debt.mjs
  var debt_exports = {};
  __export(debt_exports, {
    migrateDebtBalances: () => migrateDebtBalances,
    summarizeDebts: () => summarizeDebts
  });
  var PERSON_ACTIVITY_TYPES = /* @__PURE__ */ new Set(["lent", "borrowed", "repayment", "debt_payment", "investment"]);
  var DEBT_BALANCE_TYPES = /* @__PURE__ */ new Set(["lent", "borrowed", "repayment", "debt_payment"]);
  function migrateDebtBalances(state) {
    if (state.ledgerVersion >= 1) return false;
    const accounts = new Map(state.accounts.map((account) => [account.id, account]));
    for (const transaction of state.transactions) {
      if (!DEBT_BALANCE_TYPES.has(transaction.type)) continue;
      const account = accounts.get(transaction.accountId);
      if (!account) continue;
      const amount = Number(transaction.amount) || 0;
      account.balance += transaction.type === "lent" || transaction.type === "debt_payment" ? amount : -amount;
    }
    state.ledgerVersion = 1;
    return true;
  }
  function summarizeDebts(transactions) {
    const people = /* @__PURE__ */ new Map();
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
    return [...people.values()].map((person) => ({
      ...person,
      owedToYou: Math.max(0, person.lent - person.repayments),
      youOwe: Math.max(0, person.borrowed - person.debtPayments),
      history: person.history.sort((a, b) => (a.date || "").localeCompare(b.date || "") || (a.createdAt || 0) - (b.createdAt || 0))
    })).sort((a, b) => a.name.localeCompare(b.name));
  }
  return __toCommonJS(debt_exports);
})();
