#!/bin/sh
set -e

# Validate BACKEND_URL is set
if [ -z "$BACKEND_URL" ]; then
  echo "ERROR: BACKEND_URL environment variable is not set" >&2
  exit 1
fi

# Process nginx config template with environment variable substitution
# BACKEND_URL is required for reverse proxy configuration
envsubst '$$BACKEND_URL $$DEPLOYMENT_COLOR $$NAMESPACE' < /etc/nginx/templates/default.conf.template > /etc/nginx/conf.d/default.conf

# Start nginx
exec nginx -g 'daemon off;'

