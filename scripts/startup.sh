#!/bin/bash
# Startup script for Java applications on Azure App Service Linux
# This script ensures Java is found and launches the application

# Find Java executable
if [ -n "$JAVA_HOME" ]; then
    JAVA_CMD="$JAVA_HOME/bin/java"
elif command -v java >/dev/null 2>&1; then
    JAVA_CMD="java"
else
    # Try common Java locations for Azure App Service
    for java_path in /usr/bin/java /usr/lib/jvm/java-17-openjdk/bin/java /opt/java/openjdk/bin/java; do
        if [ -x "$java_path" ]; then
            JAVA_CMD="$java_path"
            break
        fi
    done
fi

if [ -z "$JAVA_CMD" ] || [ ! -x "$JAVA_CMD" ]; then
    echo "Error: Java executable not found. Please ensure Java runtime is configured." >&2
    exit 1
fi

# Run the application
exec "$JAVA_CMD" -jar /home/site/wwwroot/ecompoc-service-1.0.0.jar

