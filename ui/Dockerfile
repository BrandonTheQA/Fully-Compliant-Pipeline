FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

RUN npm run build

FROM nginx:alpine

# Install gettext for envsubst
RUN apk add --no-cache gettext

# Copy built assets
COPY --from=build /app/dist /usr/share/nginx/html

# Create nginx config template with reverse proxy for BFF
# Note: Uses DEPLOYMENT_COLOR and NAMESPACE env vars for runtime substitution
RUN echo 'server {' > /etc/nginx/templates/default.conf.template && \
    echo '    listen 80;' >> /etc/nginx/templates/default.conf.template && \
    echo '    server_name localhost;' >> /etc/nginx/templates/default.conf.template && \
    echo '    root /usr/share/nginx/html;' >> /etc/nginx/templates/default.conf.template && \
    echo '    index index.html;' >> /etc/nginx/templates/default.conf.template && \
    echo '' >> /etc/nginx/templates/default.conf.template && \
    echo '    gzip on;' >> /etc/nginx/templates/default.conf.template && \
    echo '    gzip_vary on;' >> /etc/nginx/templates/default.conf.template && \
    echo '    gzip_min_length 1024;' >> /etc/nginx/templates/default.conf.template && \
    echo '    gzip_types text/plain text/css text/xml text/javascript application/x-javascript application/xml+rss application/json application/javascript;' >> /etc/nginx/templates/default.conf.template && \
    echo '' >> /etc/nginx/templates/default.conf.template && \
    echo '    # BFF reverse proxy configuration' >> /etc/nginx/templates/default.conf.template && \
    echo '    # Proxy /api/login requests to user service' >> /etc/nginx/templates/default.conf.template && \
    echo '    location /api/login {' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_pass http://user-${DEPLOYMENT_COLOR}.${NAMESPACE}.svc.cluster.local/api/login;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_http_version 1.1;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_set_header Host $host;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_set_header X-Real-IP $remote_addr;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_set_header X-Forwarded-Proto $scheme;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_connect_timeout 60s;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_send_timeout 60s;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_read_timeout 60s;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_buffering off;' >> /etc/nginx/templates/default.conf.template && \
    echo '    }' >> /etc/nginx/templates/default.conf.template && \
    echo '' >> /etc/nginx/templates/default.conf.template && \
    echo '    # Proxy /api/users/* requests to user service' >> /etc/nginx/templates/default.conf.template && \
    echo '    location /api/users {' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_pass http://user-${DEPLOYMENT_COLOR}.${NAMESPACE}.svc.cluster.local/api/users;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_http_version 1.1;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_set_header Host $host;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_set_header X-Real-IP $remote_addr;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_set_header X-Forwarded-Proto $scheme;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_connect_timeout 60s;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_send_timeout 60s;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_read_timeout 60s;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_buffering off;' >> /etc/nginx/templates/default.conf.template && \
    echo '    }' >> /etc/nginx/templates/default.conf.template && \
    echo '' >> /etc/nginx/templates/default.conf.template && \
    echo '    # Proxy /api/products/* requests to product service' >> /etc/nginx/templates/default.conf.template && \
    echo '    location /api/products {' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_pass http://product-${DEPLOYMENT_COLOR}.${NAMESPACE}.svc.cluster.local/api/products;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_http_version 1.1;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_set_header Host $host;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_set_header X-Real-IP $remote_addr;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_set_header X-Forwarded-Proto $scheme;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_connect_timeout 60s;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_send_timeout 60s;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_read_timeout 60s;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_buffering off;' >> /etc/nginx/templates/default.conf.template && \
    echo '    }' >> /etc/nginx/templates/default.conf.template && \
    echo '' >> /etc/nginx/templates/default.conf.template && \
    echo '    # Proxy /api/orders/* requests to order service' >> /etc/nginx/templates/default.conf.template && \
    echo '    location /api/orders {' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_pass http://order-${DEPLOYMENT_COLOR}.${NAMESPACE}.svc.cluster.local/api/orders;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_http_version 1.1;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_set_header Host $host;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_set_header X-Real-IP $remote_addr;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_set_header X-Forwarded-Proto $scheme;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_connect_timeout 60s;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_send_timeout 60s;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_read_timeout 60s;' >> /etc/nginx/templates/default.conf.template && \
    echo '        proxy_buffering off;' >> /etc/nginx/templates/default.conf.template && \
    echo '    }' >> /etc/nginx/templates/default.conf.template && \
    echo '' >> /etc/nginx/templates/default.conf.template && \
    echo '    # Serve static files for all other requests' >> /etc/nginx/templates/default.conf.template && \
    echo '    location / {' >> /etc/nginx/templates/default.conf.template && \
    echo '        try_files $uri $uri/ /index.html;' >> /etc/nginx/templates/default.conf.template && \
    echo '    }' >> /etc/nginx/templates/default.conf.template && \
    echo '' >> /etc/nginx/templates/default.conf.template && \
    echo '    # Health check endpoint' >> /etc/nginx/templates/default.conf.template && \
    echo '    location /health {' >> /etc/nginx/templates/default.conf.template && \
    echo '        access_log off;' >> /etc/nginx/templates/default.conf.template && \
    echo '        return 200 "healthy\n";' >> /etc/nginx/templates/default.conf.template && \
    echo '        add_header Content-Type text/plain;' >> /etc/nginx/templates/default.conf.template && \
    echo '    }' >> /etc/nginx/templates/default.conf.template && \
    echo '}' >> /etc/nginx/templates/default.conf.template

EXPOSE 80

# Enable environment variable substitution for nginx templates
CMD ["sh", "-c", "envsubst '$$DEPLOYMENT_COLOR $$NAMESPACE' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf && nginx -g 'daemon off;'"]

