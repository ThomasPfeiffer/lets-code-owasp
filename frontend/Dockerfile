# Lab-Frontend: Vite-Dev-Server mit Hot-Reload (User Story 26).
# Quellverzeichnis wird per docker-compose-Bind-Mount ueberlagert.
FROM node:22-alpine

WORKDIR /app

# Erst Manifeste kopieren -> Dependency-Layer cachebar.
COPY package.json ./
RUN npm install

# Restliche Quellen (werden zur Laufzeit per Bind-Mount ueberlagert).
COPY . .

EXPOSE 3012

# `--host` macht den Dev-Server im Container von aussen erreichbar.
CMD ["npm", "run", "dev", "--", "--host", "--port", "3012"]
