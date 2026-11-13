const FORMAT = "clumsy-encrypted-backup";
const VERSION = 1;
const ITERATIONS = 310000;
const encoder = new TextEncoder();
const decoder = new TextDecoder();

function toBase64(bytes) {
  let binary = "";
  for (const byte of bytes) binary += String.fromCharCode(byte);
  return btoa(binary);
}

function fromBase64(value) {
  return Uint8Array.from(atob(value), character => character.charCodeAt(0));
}

export async function createKey(password, salt = crypto.getRandomValues(new Uint8Array(16))) {
  if (typeof password !== "string" || password.length < 10) {
    throw new Error("Choose a password with at least 10 characters.");
  }
  const material = await crypto.subtle.importKey("raw", encoder.encode(password), "PBKDF2", false, ["deriveKey"]);
  const key = await crypto.subtle.deriveKey(
    { name: "PBKDF2", salt, iterations: ITERATIONS, hash: "SHA-256" },
    material,
    { name: "AES-GCM", length: 256 },
    false,
    ["encrypt", "decrypt"]
  );
  return { key, salt: toBase64(salt) };
}

export async function deriveKey(password, saltBase64) {
  return createKey(password, fromBase64(saltBase64));
}

export async function encrypt(value, key, salt) {
  const iv = crypto.getRandomValues(new Uint8Array(12));
  const ciphertext = await crypto.subtle.encrypt(
    { name: "AES-GCM", iv },
    key,
    encoder.encode(JSON.stringify(value))
  );
  return {
    format: FORMAT,
    version: VERSION,
    kdf: "PBKDF2-SHA-256",
    iterations: ITERATIONS,
    cipher: "AES-256-GCM",
    salt,
    iv: toBase64(iv),
    ciphertext: toBase64(new Uint8Array(ciphertext))
  };
}

export async function decrypt(envelope, key) {
  if (
    !envelope || envelope.format !== FORMAT || envelope.version !== VERSION ||
    envelope.kdf !== "PBKDF2-SHA-256" || envelope.iterations !== ITERATIONS ||
    envelope.cipher !== "AES-256-GCM" || typeof envelope.salt !== "string" ||
    typeof envelope.iv !== "string" || typeof envelope.ciphertext !== "string"
  ) {
    throw new Error("This encrypted Clumsy backup format is not supported.");
  }
  const plaintext = await crypto.subtle.decrypt(
    { name: "AES-GCM", iv: fromBase64(envelope.iv) },
    key,
    fromBase64(envelope.ciphertext)
  );
  return JSON.parse(decoder.decode(plaintext));
}

export function isEncrypted(value) {
  return Boolean(value && value.format === FORMAT && value.version === VERSION);
}
