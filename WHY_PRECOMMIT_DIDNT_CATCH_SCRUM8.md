# Why Pre-commit Hooks Didn't Catch SCRUM8 Shipping Recommendations Test Failures

## Summary

The pre-commit hooks did not catch the failing `SCRUM8ShippingRecommendationsTest` tests for the following reasons:

## Root Causes

### 1. Quick Test Script Only Runs SCRUM7 Tests

The pre-commit hook uses `./scripts/run-selenium-tests-quick.sh` which only runs `SCRUM7ShippingCostCalculatorTest`, not `SCRUM8ShippingRecommendationsTest`. This is evident from line 70 of the script:

```bash
BASE_URL="$BASE_URL" mvn clean test -Dtest=SCRUM7ShippingCostCalculatorTest
```

**Impact**: Even if services were running and the hook executed, it would not have tested the SCRUM8 functionality.

### 2. Pre-commit Hook Only Warns, Doesn't Fail

The pre-commit hook (`.githooks/pre-commit` lines 172-179) only warns when Selenium tests fail; it does not abort the commit:

```bash
if "$REPO_ROOT/scripts/run-selenium-tests-quick.sh" "http://localhost:8084" >/dev/null 2>&1; then
  echo -e "${GREEN}✓ Quick Selenium test check passed${NC}"
else
  echo -e "${RED}✗ Quick Selenium test check failed${NC}"
  # Don't fail the commit, just warn
fi
```

**Impact**: Developers could commit even if tests failed, as long as they acknowledged the warning.

### 3. Conditional Execution Based on File Changes

The hook only runs Selenium tests if Selenium-related files were changed (line 161):

```bash
SELENIUM_FILES_CHANGED=$(git diff --cached --name-only | grep -E "(selenium/|ui/src/.*\.(tsx?|css))" || true)
```

**Impact**: If the bug was introduced in a commit that didn't modify these files, the hook wouldn't run at all.

### 4. Requires Services to Be Running

The hook only runs tests if services are detected as running (line 170):

```bash
if command -v nc >/dev/null 2>&1 && nc -z localhost 8084 >/dev/null 2>&1; then
```

**Impact**: If services weren't running during commit, tests would be skipped entirely.

### 5. Can Be Skipped with Environment Variable

The hook can be bypassed entirely with `SKIP_SELENIUM=1 git commit` (line 157).

**Impact**: Developers could intentionally skip Selenium tests if they were causing issues.

## The Actual Bug

The `ShippingRecommendations` component was returning `null` before checking the `loading` state, which meant:

1. When recommendations were `null` but `loading === true`, the component returned `null`
2. Selenium tests waiting for the `.shipping-recommendations` element would timeout
3. The element would only appear after loading completed AND recommendations were successfully fetched

## Fix Applied

The component was updated to check the `loading` state first, ensuring the container element with class `shipping-recommendations` is always rendered during loading, even when recommendations are `null`. This allows Selenium tests to find the element during the loading phase.

## Recommendations

1. **Update quick test script** to include SCRUM8 tests or run all test classes
2. **Make Selenium test failures block commits** (remove the warning-only behavior)
3. **Run full Selenium test suite in CI** before allowing merges to main
4. **Consider running a subset of critical tests** in pre-commit that can't be skipped

