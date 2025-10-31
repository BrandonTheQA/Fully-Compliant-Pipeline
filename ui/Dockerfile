FROM node:20-alpine AS build

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

RUN npm run build

FROM nginx:alpine

# Copy built assets
COPY --from=build /app/dist /usr/share/nginx/html

# Create nginx config with reverse proxy for BFF
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
    echo '    # BFF reverse proxy configuration' >> /etc/nginx/conf.d/default.conf && \
    echo '    # Proxy /api/login requests to user service' >> /etc/nginx/conf.d/default.conf && \
    echo '    location /api/login {' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_pass http://user.dev.svc.cluster.local/api/login;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_http_version 1.1;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_set_header Host $host;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_set_header X-Real-IP $remote_addr;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_set_header X-Forwarded-Proto $scheme;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_connect_timeout 60s;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_send_timeout 60s;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_read_timeout 60s;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_buffering off;' >> /etc/nginx/conf.d/default.conf && \
    echo '    }' >> /etc/nginx/conf.d/default.conf && \
    echo '' >> /etc/nginx/conf.d/default.conf && \
    echo '    # Proxy /api/users/* requests to user service' >> /etc/nginx/conf.d/default.conf && \
    echo '    location /api/users {' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_pass http://user.dev.svc.cluster.local/api/users;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_http_version 1.1;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_set_header Host $host;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_set_header X-Real-IP $remote_addr;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_set_header X-Forwarded-Proto $scheme;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_connect_timeout 60s;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_send_timeout 60s;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_read_timeout 60s;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_buffering off;' >> /etc/nginx/conf.d/default.conf && \
    echo '    }' >> /etc/nginx/conf.d/default.conf && \
    echo '' >> /etc/nginx/conf.d/default.conf && \
    echo '    # Proxy /api/products/* requests to product service' >> /etc/nginx/conf.d/default.conf && \
    echo '    location /api/products {' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_pass http://product.dev.svc.cluster.local/api/products;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_http_version 1.1;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_set_header Host $host;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_set_header X-Real-IP $remote_addr;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_set_header X-Forwarded-Proto $scheme;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_connect_timeout 60s;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_send_timeout 60s;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_read_timeout 60s;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_buffering off;' >> /etc/nginx/conf.d/default.conf && \
    echo '    }' >> /etc/nginx/conf.d/default.conf && \
    echo '' >> /etc/nginx/conf.d/default.conf && \
    echo '    # Proxy /api/orders/* requests to order service' >> /etc/nginx/conf.d/default.conf && \
    echo '    location /api/orders {' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_pass http://order.dev.svc.cluster.local/api/orders;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_http_version 1.1;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_set_header Host $host;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_set_header X-Real-IP $remote_addr;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_set_header X-Forwarded-Proto $scheme;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_connect_timeout 60s;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_send_timeout 60s;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_read_timeout 60s;' >> /etc/nginx/conf.d/default.conf && \
    echo '        proxy_buffering off;' >> /etc/nginx/conf.d/default.conf && \
    echo '    }' >> /etc/nginx/conf.d/default.conf && \
    echo '' >> /etc/nginx/conf.d/default.conf && \
    echo '    # Serve static files for all other requests' >> /etc/nginx/conf.d/default.conf && \
    echo '    location / {' >> /etc/nginx/conf.d/default.conf && \
    echo '        try_files $uri $uri/ /index.html;' >> /etc/nginx/conf.d/default.conf && \
    echo '    }' >> /etc/nginx/conf.d/default.conf && \
    echo '' >> /etc/nginx/conf.d/default.conf && \
    echo '    # Health check endpoint' >> /etc/nginx/conf.d/default.conf && \
    echo '    location /health {' >> /etc/nginx/conf.d/default.conf && \
    echo '        access_log off;' >> /etc/nginx/conf.d/default.conf && \
    echo '        return 200 "healthy\n";' >> /etc/nginx/conf.d/default.conf && \
    echo '        add_header Content-Type text/plain;' >> /etc/nginx/conf.d/default.conf && \
    echo '    }' >> /etc/nginx/conf.d/default.conf && \
    echo '}' >> /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]

