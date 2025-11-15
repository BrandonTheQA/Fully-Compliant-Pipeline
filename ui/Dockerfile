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

# Copy built assets and server.js
COPY --from=build /app/dist ./dist
COPY server.js ./

# Use PORT environment variable (Azure App Service uses WEBSITES_PORT)
# Default to 8080 for consistency
ENV PORT=8080

EXPOSE 8080

# Start Express server
CMD ["node", "server.js"]
