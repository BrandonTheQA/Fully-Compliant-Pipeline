#!/usr/bin/env bash

set -euo pipefail

log() {
  echo "[jira-validation] $*" >&2
}

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    log "Missing required command: $1"
    exit 1
  fi
}

require_env() {
  local name="$1"
  if [ -z "${!name:-}" ]; then
    log "Required environment variable '$name' is not set."
    exit 1
  fi
}

require_cmd curl
require_cmd jq
require_cmd grep
require_cmd sort
require_cmd uniq

# Feature toggle check
JIRA_VALIDATION_ENABLED="${JIRA_VALIDATION_ENABLED:-true}"
if [ "${JIRA_VALIDATION_ENABLED}" != "true" ]; then
  log "JIRA validation is disabled via JIRA_VALIDATION_ENABLED=${JIRA_VALIDATION_ENABLED}"
  log "Skipping validation (this is a warning, not an error)"
  exit 0
fi

# JIRA configuration
JIRA_BASE_URL="${JIRA_BASE_URL:-https://ecompoc.atlassian.net}"
JIRA_API_URL="${JIRA_BASE_URL}/rest/api/3"

# Check required environment variables
if [ -z "${JIRA_API_TOKEN:-}" ] || [ -z "${JIRA_EMAIL:-}" ]; then
  log "ERROR: JIRA_API_TOKEN and JIRA_EMAIL must be set for JIRA validation"
  log "Set JIRA_VALIDATION_ENABLED=false to skip validation"
  exit 1
fi

# Release report path (default location from GitHub Actions artifact download)
RELEASE_REPORT_DIR="${RELEASE_REPORT_DIR:-release-report}"
RELEASE_REPORT_PATTERN="${RELEASE_REPORT_DIR}/release-report-*.md"

# Find release report file
RELEASE_REPORT=""
if [ -d "${RELEASE_REPORT_DIR}" ]; then
  # Find the most recent release report file
  RELEASE_REPORT=$(find "${RELEASE_REPORT_DIR}" -name "release-report-*.md" -type f | sort -r | head -n 1)
fi

if [ -z "${RELEASE_REPORT}" ] || [ ! -f "${RELEASE_REPORT}" ]; then
  log "ERROR: Release report not found in ${RELEASE_REPORT_DIR}"
  log "Expected pattern: ${RELEASE_REPORT_PATTERN}"
  log "Make sure the release-report artifact was downloaded successfully"
  exit 1
fi

log "Using release report: ${RELEASE_REPORT}"

# Extract SCRUM-x references from release report
extract_scrum_references() {
  local report_file="$1"
  # Extract all SCRUM-{number} patterns from the markdown file
  # This regex matches SCRUM- followed by one or more digits
  grep -oE 'SCRUM-[0-9]+' "${report_file}" | sort -u
}

# Query JIRA API for story status with retry logic
query_jira_status() {
  local issue_key="$1"
  local max_retries=3
  local retry_count=0
  local delay=1

  while [ $retry_count -lt $max_retries ]; do
    local http_code
    local response
    
    # Make API call with Basic Auth
    response=$(curl -sSf -w "\n%{http_code}" \
      -u "${JIRA_EMAIL}:${JIRA_API_TOKEN}" \
      -H "Accept: application/json" \
      "${JIRA_API_URL}/issue/${issue_key}" 2>&1) || {
      local curl_exit=$?
      if [ $curl_exit -eq 22 ]; then
        # HTTP error (4xx, 5xx)
        http_code=$(echo "${response}" | tail -n 1)
        if [ "${http_code}" = "404" ]; then
          log "WARNING: Story ${issue_key} not found in JIRA (HTTP 404)"
          return 2  # Story doesn't exist
        elif [ "${http_code}" = "429" ]; then
          # Rate limit
          if [ $retry_count -lt $((max_retries - 1)) ]; then
            log "Rate limit hit for ${issue_key}, retrying in ${delay}s (attempt $((retry_count + 1))/${max_retries})"
            sleep "${delay}"
            delay=$((delay * 2))  # Exponential backoff
            retry_count=$((retry_count + 1))
            continue
          else
            log "ERROR: Rate limit exceeded for ${issue_key} after ${max_retries} attempts"
            return 1
          fi
        else
          log "ERROR: HTTP ${http_code} error querying ${issue_key}"
          if [ $retry_count -lt $((max_retries - 1)) ]; then
            log "Retrying in ${delay}s (attempt $((retry_count + 1))/${max_retries})"
            sleep "${delay}"
            delay=$((delay * 2))
            retry_count=$((retry_count + 1))
            continue
          else
            return 1
          fi
        fi
      else
        # Network or other error
        log "ERROR: Failed to query ${issue_key}: curl exit code ${curl_exit}"
        if [ $retry_count -lt $((max_retries - 1)) ]; then
          log "Retrying in ${delay}s (attempt $((retry_count + 1))/${max_retries})"
          sleep "${delay}"
          delay=$((delay * 2))
          retry_count=$((retry_count + 1))
          continue
        else
          return 1
        fi
      fi
    }

    # Extract HTTP status code
    http_code=$(echo "${response}" | tail -n 1)
    response_body=$(echo "${response}" | sed '$d')

    if [ "${http_code}" = "200" ]; then
      # Parse status category from response
      local status_category
      status_category=$(echo "${response_body}" | jq -r '.fields.status.statusCategory.key // "unknown"' 2>/dev/null || echo "unknown")
      
      if [ "${status_category}" = "unknown" ] || [ "${status_category}" = "null" ]; then
        log "WARNING: Could not parse status for ${issue_key}"
        return 1
      fi

      echo "${status_category}"
      return 0
    elif [ "${http_code}" = "404" ]; then
      log "WARNING: Story ${issue_key} not found in JIRA (HTTP 404)"
      return 2  # Story doesn't exist
    elif [ "${http_code}" = "429" ]; then
      # Rate limit
      if [ $retry_count -lt $((max_retries - 1)) ]; then
        log "Rate limit hit for ${issue_key}, retrying in ${delay}s (attempt $((retry_count + 1))/${max_retries})"
        sleep "${delay}"
        delay=$((delay * 2))
        retry_count=$((retry_count + 1))
        continue
      else
        log "ERROR: Rate limit exceeded for ${issue_key} after ${max_retries} attempts"
        return 1
      fi
    else
      log "ERROR: HTTP ${http_code} error querying ${issue_key}"
      if [ $retry_count -lt $((max_retries - 1)) ]; then
        log "Retrying in ${delay}s (attempt $((retry_count + 1))/${max_retries})"
        sleep "${delay}"
        delay=$((delay * 2))
        retry_count=$((retry_count + 1))
        continue
      else
        return 1
      fi
    fi
  done

  log "ERROR: Failed to query ${issue_key} after ${max_retries} attempts"
  return 1
}

# Main validation logic
log "Extracting SCRUM story references from release report..."

SCRUM_STORIES=$(extract_scrum_references "${RELEASE_REPORT}")

if [ -z "${SCRUM_STORIES}" ]; then
  log "No SCRUM story references found in release report"
  log "Validation passed (empty release is valid)"
  exit 0
fi

STORY_COUNT=$(echo "${SCRUM_STORIES}" | grep -c . || echo "0")
log "Found ${STORY_COUNT} unique SCRUM story reference(s)"

# Track validation results
NON_CLOSED_STORIES=()
QUERY_ERRORS=()
MISSING_STORIES=()

# Query each story's status
while IFS= read -r story_key; do
  [ -z "${story_key}" ] && continue
  
  log "Checking status of ${story_key}..."
  
  status_result=$(query_jira_status "${story_key}")
  query_exit=$?

  if [ $query_exit -eq 0 ]; then
    # Successfully queried status
    if [ "${status_result}" = "done" ]; then
      log "  ✓ ${story_key} is closed (status: done)"
    else
      log "  ✗ ${story_key} is NOT closed (status category: ${status_result})"
      NON_CLOSED_STORIES+=("${story_key}:${status_result}")
    fi
  elif [ $query_exit -eq 2 ]; then
    # Story doesn't exist - warn but don't fail
    log "  ⚠ ${story_key} not found in JIRA (warning only)"
    MISSING_STORIES+=("${story_key}")
  else
    # Query error - fail validation
    log "  ✗ ERROR: Failed to query ${story_key}"
    QUERY_ERRORS+=("${story_key}")
  fi

  # Small delay between requests to avoid rate limiting
  sleep 0.2
done <<< "${SCRUM_STORIES}"

# Report results
echo ""
log "=== Validation Summary ==="

if [ ${#QUERY_ERRORS[@]} -gt 0 ]; then
  log "ERROR: Failed to query ${#QUERY_ERRORS[@]} story/stories:"
  for story in "${QUERY_ERRORS[@]}"; do
    log "  - ${story}"
  done
  log ""
  log "Validation failed due to API errors"
  exit 1
fi

if [ ${#MISSING_STORIES[@]} -gt 0 ]; then
  log "WARNING: ${#MISSING_STORIES[@]} story/stories not found in JIRA:"
  for story in "${MISSING_STORIES[@]}"; do
    log "  - ${story}"
  done
  log "(This is a warning and does not fail validation)"
  log ""
fi

if [ ${#NON_CLOSED_STORIES[@]} -gt 0 ]; then
  log "ERROR: ${#NON_CLOSED_STORIES[@]} story/stories are not closed:"
  for story_status in "${NON_CLOSED_STORIES[@]}"; do
    story_key=$(echo "${story_status}" | cut -d: -f1)
    status=$(echo "${story_status}" | cut -d: -f2)
    log "  - ${story_key} (status category: ${status})"
  done
  log ""
  log "Validation FAILED: All stories must be closed before production deployment"
  log "Please ensure all referenced JIRA stories are in 'Done' status"
  exit 1
fi

log "Validation PASSED: All ${STORY_COUNT} story/stories are closed"
exit 0
