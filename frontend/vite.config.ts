import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Vite-Dev-Server fuer das Lab. `--host` und fester Port 3012 werden hier gesetzt,
// damit der Container von aussen erreichbar ist und Hot-Reload funktioniert.
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: 3012,
    strictPort: true,
    // Im Container lauschen Datei-Watcher zuverlaessiger per Polling (Bind-Mount).
    watch: {
      usePolling: true,
    },
  },
});
