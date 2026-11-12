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

console.log("Prepared Clumsy web assets in www/.");
