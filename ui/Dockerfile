FROM --platform=linux/arm64 node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

RUN npm run build

FROM --platform=linux/arm64 nginx:alpine

# Install gettext for envsubst
RUN apk add --no-cache gettext

# Copy built assets
COPY --from=build /app/dist /usr/share/nginx/html

# Copy nginx config template with reverse proxy for BFF
# Note: Uses DEPLOYMENT_COLOR and NAMESPACE env vars for runtime substitution
COPY nginx.conf.template /etc/nginx/templates/default.conf.template

EXPOSE 80

# Enable environment variable substitution for nginx templates
CMD ["sh", "-c", "envsubst '$$DEPLOYMENT_COLOR $$NAMESPACE' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]

