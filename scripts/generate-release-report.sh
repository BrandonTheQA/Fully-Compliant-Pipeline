#!/usr/bin/env bash

set -euo pipefail

log() {
  echo "[release-report] $*" >&2
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
require_cmd git

require_env GITHUB_TOKEN
require_env GITHUB_REPOSITORY
require_env GITHUB_SHA

API_URL="${GITHUB_API_URL:-https://api.github.com}"
REPO="${GITHUB_REPOSITORY}"
TOKEN="${GITHUB_TOKEN}"
CURRENT_SHA="${GITHUB_SHA}"
RUN_ID="${GITHUB_RUN_ID:-local}"
RUN_NUMBER="${GITHUB_RUN_NUMBER:-local}"
ACTOR="${GITHUB_ACTOR:-unknown}"
ENVIRONMENT="${RELEASE_REPORT_ENVIRONMENT:-prod}"
REPORT_DIR="${RELEASE_REPORT_DIR:-release-report}"
TIMESTAMP="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
RUN_URL="${GITHUB_SERVER_URL:-https://github.com}/${REPO}/actions/runs/${RUN_ID}"
REPORT_PATH="${RELEASE_REPORT_PATH:-${REPORT_DIR}/release-report-${RUN_ID}.md}"

mkdir -p "${REPORT_DIR}"

auth_header() {
  printf "Authorization: Bearer %s" "${TOKEN}"
}

fetch_deployments() {
  local page=1
  local per_page=10
  curl -sSf \
    -H "$(auth_header)" \
    -H "Accept: application/vnd.github+json" \
    "${API_URL}/repos/${REPO}/deployments?environment=${ENVIRONMENT}&per_page=${per_page}&page=${page}"
}

fetch_latest_status() {
  local deployment_id="$1"
  curl -sSf \
    -H "$(auth_header)" \
    -H "Accept: application/vnd.github+json" \
    "${API_URL}/repos/${REPO}/deployments/${deployment_id}/statuses?per_page=1"
}

determine_previous_prod_sha() {
  local deployments_json="$1"
  local previous_sha=""
  local previous_created=""
  local found_current=""

  if [ "$(echo "${deployments_json}" | jq 'length')" -eq 0 ]; then
    echo ""
    return 0
  fi

  echo "${deployments_json}" | jq -c '.[] | {id: .id, sha: .sha, created_at: .created_at}' | while read -r deployment; do
    local id sha created state statuses
    id="$(echo "${deployment}" | jq -r '.id')"
    sha="$(echo "${deployment}" | jq -r '.sha')"
    created="$(echo "${deployment}" | jq -r '.created_at')"
    statuses="$(fetch_latest_status "${id}")"
    state="$(echo "${statuses}" | jq -r '.[0].state // empty')"
    if [ "${state}" != "success" ]; then
      continue
    fi

    if [ -z "${found_current}" ] && [ "${sha}" = "${CURRENT_SHA}" ]; then
      log "Latest prod deployment already points to current SHA ${CURRENT_SHA}; searching for prior deployment."
      found_current="true"
      continue
    fi

    previous_sha="${sha}"
    previous_created="${created}"
    break
  done

  if [ -n "${previous_sha}" ]; then
    log "Previous successful prod deployment: ${previous_sha} (${previous_created})"
    echo "${previous_sha}"
    return 0
  fi

  log "No successful prod deployments found before current commit."
  echo ""
}

get_repo_root_commit() {
  git rev-list --max-parents=0 HEAD | tail -n 1
}

generate_commit_list() {
  local base_sha="$1"
  if [ -n "${base_sha}" ]; then
    git rev-list --reverse "${base_sha}..${CURRENT_SHA}"
  else
    git rev-list --reverse "${CURRENT_SHA}"
  fi
}

DEPS_JSON="$(fetch_deployments)" || {
  log "Failed to read deployments from GitHub API."
  exit 1
}

PREVIOUS_SHA="$(determine_previous_prod_sha "${DEPS_JSON}")"

FALLBACK_NOTE=""
if [ -z "${PREVIOUS_SHA}" ]; then
  PREVIOUS_SHA="$(get_repo_root_commit)"
  FALLBACK_NOTE="(no prior deployments; starting from repository root)"
fi

if [ -z "${PREVIOUS_SHA}" ]; then
  log "Unable to determine any baseline commit for release diff."
  exit 1
fi

log "Computing commits between ${PREVIOUS_SHA} and ${CURRENT_SHA}"

COMMITS="$(generate_commit_list "${PREVIOUS_SHA}")"

COMMIT_COUNT="$(echo "${COMMITS}" | sed '/^$/d' | wc -l | tr -d ' ')"

{
  echo "# Release Report"
  echo
  echo "- Environment: \`${ENVIRONMENT}\`"
  echo "- Run: [#${RUN_NUMBER}](${RUN_URL})"
  echo "- Generated: ${TIMESTAMP}"
  echo "- Actor: ${ACTOR}"
  echo "- Current SHA: \`${CURRENT_SHA}\`"
  echo "- Previous Prod SHA: \`${PREVIOUS_SHA}\` ${FALLBACK_NOTE}"
  echo "- Commit Count: ${COMMIT_COUNT}"
  echo
  echo "## Commits"
  echo
} > "${REPORT_PATH}"

if [ "${COMMIT_COUNT}" -eq 0 ]; then
  {
    echo "_No new commits were found between the previous production deployment and this run._"
  } >> "${REPORT_PATH}"
else
  while read -r commit; do
    [ -z "${commit}" ] && continue
    subject="$(git log -1 --pretty=%s "${commit}")"
    author="$(git log -1 --pretty=%an "${commit}")"
    commit_url="${GITHUB_SERVER_URL:-https://github.com}/${REPO}/commit/${commit}"
    short_sha="${commit:0:7}"
    echo "- [\`${short_sha}\`](${commit_url}) ${subject} _(by ${author})_" >> "${REPORT_PATH}"
  done <<< "${COMMITS}"
fi

echo >> "${REPORT_PATH}"
echo "## Notes" >> "${REPORT_PATH}"
echo >> "${REPORT_PATH}"
if [ -n "${FALLBACK_NOTE}" ]; then
  echo "- ${FALLBACK_NOTE}" >> "${REPORT_PATH}"
fi
echo "- Commit range logged for debugging." >> "${REPORT_PATH}"

log "Release report written to ${REPORT_PATH}"

if [ -n "${GITHUB_STEP_SUMMARY:-}" ] && [ -f "${REPORT_PATH}" ]; then
  {
    echo "## Release Report"
    echo
    cat "${REPORT_PATH}"
  } >> "${GITHUB_STEP_SUMMARY}"
fi

if [ "${COMMIT_COUNT}" -eq 0 ]; then
  log "No new commits detected for this release window."
else
  log "Included ${COMMIT_COUNT} commits in the report."
fi

