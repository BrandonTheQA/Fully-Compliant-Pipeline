FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# Production stage - use Node.js with Express
FROM node:20-alpine

WORKDIR /app

# Copy package files and install production dependencies
COPY package.json package-lock.json ./
RUN npm ci --only=production

# Copy built assets, server.js, and PM2 config
COPY --from=build /app/dist ./dist
COPY server.js ./
COPY ecosystem.config.js ./

# Use PORT environment variable (Azure App Service uses WEBSITES_PORT)
# Default to 8080 for consistency
ENV PORT=8080

EXPOSE 8080

# Start Express server with PM2 for process management
# Use npx to ensure PM2 is found in node_modules
CMD ["npx", "pm2-runtime", "start", "ecosystem.config.js"]
