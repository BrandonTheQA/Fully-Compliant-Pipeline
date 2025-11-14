FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

RUN npm run build

FROM nginx:alpine

# Install gettext for envsubst (environment variable substitution)
RUN apk add --no-cache gettext

# Copy built assets
COPY --from=build /app/dist /usr/share/nginx/html

# Copy nginx config template
# BACKEND_URL will be substituted at runtime via envsubst
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

# Copy startup script
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 80

# Use custom entrypoint to configure nginx with backend URL and start nginx
ENTRYPOINT ["/docker-entrypoint.sh"]

