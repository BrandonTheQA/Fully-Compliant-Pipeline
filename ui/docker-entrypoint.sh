#!/bin/sh
set -e

# Inject API base URL into index.html if VITE_API_BASE_URL is set and not empty
if [ -n "$VITE_API_BASE_URL" ] && [ "$VITE_API_BASE_URL" != "" ]; then
  # Inject configuration script before closing </head> tag
  # Use single quotes in JavaScript to avoid issues with special characters
  sed -i "s|</head>|<script>window.__APP_CONFIG__ = { VITE_API_BASE_URL: '${VITE_API_BASE_URL}' };</script></head>|" /usr/share/nginx/html/index.html
fi

# Process nginx config template with environment variable substitution
# (DEPLOYMENT_COLOR and NAMESPACE are no longer used but kept for compatibility)
envsubst '$$DEPLOYMENT_COLOR $$NAMESPACE' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf

# Start nginx
exec nginx -g 'daemon off;'

