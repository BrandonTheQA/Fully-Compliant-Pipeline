FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Set placeholder values for build-time environment variables
ENV VITE_USER_API_URL=http://__VITE_USER_API_URL__
ENV VITE_PRODUCT_API_URL=http://__VITE_PRODUCT_API_URL__
ENV VITE_ORDER_API_URL=http://__VITE_ORDER_API_URL__

RUN npm run build

FROM nginx:alpine

# Copy built assets
COPY --from=build /app/dist /usr/share/nginx/html

# Create nginx config
RUN echo 'server {' > /etc/nginx/conf.d/default.conf && \
    echo '    listen 80;' >> /etc/nginx/conf.d/default.conf && \
    echo '    server_name localhost;' >> /etc/nginx/conf.d/default.conf && \
    echo '    root /usr/share/nginx/html;' >> /etc/nginx/conf.d/default.conf && \
    echo '    index index.html;' >> /etc/nginx/conf.d/default.conf && \
    echo '' >> /etc/nginx/conf.d/default.conf && \
    echo '    gzip on;' >> /etc/nginx/conf.d/default.conf && \
    echo '    gzip_vary on;' >> /etc/nginx/conf.d/default.conf && \
    echo '    gzip_min_length 1024;' >> /etc/nginx/conf.d/default.conf && \
    echo '    gzip_types text/plain text/css text/xml text/javascript application/x-javascript application/xml+rss application/json application/javascript;' >> /etc/nginx/conf.d/default.conf && \
    echo '' >> /etc/nginx/conf.d/default.conf && \
    echo '    location / {' >> /etc/nginx/conf.d/default.conf && \
    echo '        try_files $uri $uri/ /index.html;' >> /etc/nginx/conf.d/default.conf && \
    echo '    }' >> /etc/nginx/conf.d/default.conf && \
    echo '' >> /etc/nginx/conf.d/default.conf && \
    echo '    location /health {' >> /etc/nginx/conf.d/default.conf && \
    echo '        access_log off;' >> /etc/nginx/conf.d/default.conf && \
    echo '        return 200 "healthy\n";' >> /etc/nginx/conf.d/default.conf && \
    echo '        add_header Content-Type text/plain;' >> /etc/nginx/conf.d/default.conf && \
    echo '    }' >> /etc/nginx/conf.d/default.conf && \
    echo '}' >> /etc/nginx/conf.d/default.conf

# Create entrypoint script to replace placeholders at runtime
RUN echo '#!/bin/sh' > /docker-entrypoint.d/20-envsubst.sh && \
    echo 'set -e' >> /docker-entrypoint.d/20-envsubst.sh && \
    echo '' >> /docker-entrypoint.d/20-envsubst.sh && \
    echo 'find /usr/share/nginx/html -type f \( -name "*.js" -o -name "*.html" \) -exec sed -i "s|__VITE_USER_API_URL__|${VITE_USER_API_URL}|g" {} +' >> /docker-entrypoint.d/20-envsubst.sh && \
    echo 'find /usr/share/nginx/html -type f \( -name "*.js" -o -name "*.html" \) -exec sed -i "s|__VITE_PRODUCT_API_URL__|${VITE_PRODUCT_API_URL}|g" {} +' >> /docker-entrypoint.d/20-envsubst.sh && \
    echo 'find /usr/share/nginx/html -type f \( -name "*.js" -o -name "*.html" \) -exec sed -i "s|__VITE_ORDER_API_URL__|${VITE_ORDER_API_URL}|g" {} +' >> /docker-entrypoint.d/20-envsubst.sh && \
    echo '' >> /docker-entrypoint.d/20-envsubst.sh && \
    echo 'exec "$@"' >> /docker-entrypoint.d/20-envsubst.sh && \
    chmod +x /docker-entrypoint.d/20-envsubst.sh

EXPOSE 80

CMD ["/docker-entrypoint.d/20-envsubst.sh", "nginx", "-g", "daemon off;"]

