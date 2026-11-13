import { copyFile, mkdir } from "node:fs/promises";
import { build } from "esbuild";

await mkdir("www", { recursive: true });
await copyFile("index.html", "www/index.html");
await build({
  entryPoints: ["native/native-bridge.js"],
  bundle: true,
  platform: "browser",
  format: "iife",
  target: "es2020",
  outfile: "www/native-bridge.js"
});
await build({
  entryPoints: ["native/security.mjs"],
  bundle: true,
  platform: "browser",
  format: "iife",
  globalName: "ClumsySecurity",
  target: "es2020",
  outfile: "security.js"
});
await copyFile("security.js", "www/security.js");
await build({
  entryPoints: ["native/debt.mjs"],
  bundle: true,
  platform: "browser",
  format: "iife",
  globalName: "ClumsyDebt",
  target: "es2020",
  outfile: "debt.js"
});
await copyFile("debt.js", "www/debt.js");

console.log("Prepared Clumsy web assets in www/.");
