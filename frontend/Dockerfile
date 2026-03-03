# ==============================================================
# Dockerfile — Front-end (Angular 17 + Nginx)
# Multi-stage build: node builder → nginx static server (alpine)
# ==============================================================

# ── Stage 1: Build ────────────────────────────────────────────
FROM node:20-alpine AS builder

WORKDIR /app

# 1. Copy package manifests first so npm ci is cached independently
#    of application code changes.
COPY package.json package-lock.json ./
RUN npm ci --prefer-offline

# 2. Copy full source and produce the optimised production bundle
COPY . .
RUN npm run build -- --configuration production

# ── Stage 2: Runtime ──────────────────────────────────────────
FROM nginx:1.25-alpine AS runtime

# Remove the default Nginx site
RUN rm -rf /usr/share/nginx/html/*

# Copy the compiled Angular application
# Adjust the dist folder name below if your angular.json uses a different output path.
COPY --from=builder /app/dist/money-contas-frontend/browser /usr/share/nginx/html

# Copy our custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
