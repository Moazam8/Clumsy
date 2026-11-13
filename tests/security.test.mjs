import test from "node:test";
import assert from "node:assert/strict";
import { createKey, decrypt, deriveKey, encrypt, isEncrypted } from "../native/security.mjs";

test("encrypts records and restores them only with the correct password", async () => {
  const records = {
    name: "Test profile",
    accounts: [{ name: "Bank", balance: 12345 }],
    transactions: [{ type: "expense", amount: 25, note: "Private note" }]
  };
  const { key, salt } = await createKey("a secure test password");
  const envelope = await encrypt(records, key, salt);

  assert.equal(isEncrypted(envelope), true);
  assert.equal(JSON.stringify(envelope).includes("Private note"), false);
  assert.deepEqual(await decrypt(envelope, key), records);

  const wrongKey = await deriveKey("a different test password", salt);
  await assert.rejects(decrypt(envelope, wrongKey.key));
});

test("uses a fresh nonce and detects modified ciphertext", async () => {
  const { key, salt } = await createKey("another secure password");
  const first = await encrypt({ value: "private" }, key, salt);
  const second = await encrypt({ value: "private" }, key, salt);

  assert.notEqual(first.iv, second.iv);
  const changed = { ...first, ciphertext: `${first.ciphertext.slice(0, -4)}AAAA` };
  await assert.rejects(decrypt(changed, key));
});

test("rejects passwords that are too short", async () => {
  await assert.rejects(createKey("short"), /at least 10 characters/);
});
