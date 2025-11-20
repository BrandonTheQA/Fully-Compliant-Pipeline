# Why Pre-commit Hooks Didn't Catch SCRUM-9 Build Failure

## Summary

The latest GitHub workflow run (#64) failed during the "Build UI Artifact" job due to a TypeScript compilation error that should have been caught by the pre-commit hook.

## The Failure

**Workflow Run:** #64 (Commit: `ee58ec1`)  
**Failed Job:** Build UI Artifact  
**Error:** TypeScript compilation error in `ProductShippingPreview.tsx`

```
src/components/ProductShippingPreview.tsx(100,5): error TS6133: 'remainingAmount' is declared but its value is never read.
```

## Root Cause Analysis

### 1. The TypeScript Error

The `remainingAmount` variable is destructured from `shippingData` on line 100 but is never used in the component:

```typescript
const {
  shippingCost,
  qualifiesForFreeShipping,
  freeShippingThreshold,
  remainingAmount,  // ← Declared but never used
  region: detectedRegion,
} = shippingData;
```

The `tsconfig.app.json` has strict TypeScript settings enabled:
- `noUnusedLocals: true` (line 21)
- `noUnusedParameters: true` (line 22)

This causes TypeScript to error when unused variables are detected.

### 2. Pre-commit Hook Should Have Caught This

The pre-commit hook (`.githooks/pre-commit` lines 84-96) runs TypeScript type checking:

```bash
run_ui_typecheck() {
  echo -e "${YELLOW}Running TypeScript type check for UI...${NC}"
  cd "$REPO_ROOT/ui"
  
  if ! npx tsc --noEmit --project tsconfig.app.json; then
    echo -e "${RED}✗ TypeScript type check failed for UI${NC}"
    return 1
  fi
  
  echo -e "${GREEN}✓ TypeScript type check passed for UI${NC}"
  return 0
}
```

**Verification:** When run manually, this command correctly catches the error:
```bash
$ cd ui && npx tsc --noEmit --project tsconfig.app.json
src/components/ProductShippingPreview.tsx(100,5): error TS6133: 'remainingAmount' is declared but its value is never read.
```

### 3. Why the Pre-commit Hook Didn't Catch It

The pre-commit hook is installed (verified: `.git/hooks/pre-commit` exists and is executable). However, the error was not caught, which suggests one of the following scenarios:

#### Most Likely: Commit Was Made with `--no-verify`

The commit may have been made using `git commit --no-verify` or `git commit -n`, which bypasses all pre-commit hooks. This is the most common reason for pre-commit hooks not running.

**Evidence:**
- The hook is installed and should have run
- The TypeScript check correctly identifies the error when run manually
- The commit was successfully created despite the error

#### Alternative: Hook Execution Issue

Less likely, but possible:
- `npx` was not available in the PATH when the hook ran
- The hook encountered an error before reaching the TypeScript check
- The file was in a different state when the hook ran (unlikely)

### 4. Build vs Pre-commit Hook Difference

**Pre-commit hook command:**
```bash
npx tsc --noEmit --project tsconfig.app.json
```

**Build command (package.json):**
```bash
tsc -b && vite build
```

Both should catch the same error since they use the same TypeScript configuration. The `tsc -b` command uses project references from `tsconfig.json`, which references `tsconfig.app.json`, so the same strict settings apply.

## The Fix

Remove the unused `remainingAmount` variable from the destructuring:

```typescript
const {
  shippingCost,
  qualifiesForFreeShipping,
  freeShippingThreshold,
  // remainingAmount,  // ← Remove this unused variable
  region: detectedRegion,
} = shippingData;
```

Or, if the variable is needed for future use, prefix it with an underscore to indicate it's intentionally unused:

```typescript
const {
  shippingCost,
  qualifiesForFreeShipping,
  freeShippingThreshold,
  remainingAmount: _remainingAmount,  // ← Prefix with underscore
  region: detectedRegion,
} = shippingData;
```

## Recommendations

1. **Never bypass pre-commit hooks**: Avoid using `--no-verify` unless absolutely necessary, and document why it was needed.

2. **Add CI check for hook bypass**: Consider adding a GitHub Actions check that verifies commits weren't made with `--no-verify` (though this is difficult to detect).

3. **Run pre-commit checks manually before committing**: As a safety measure, run the TypeScript check manually:
   ```bash
   cd ui && npx tsc --noEmit --project tsconfig.app.json
   ```

4. **Consider using Husky or similar**: Tools like Husky can provide better visibility into hook execution and make it harder to accidentally bypass hooks.

5. **Add build step to pre-commit**: Consider adding `npm run build` to the pre-commit hook to catch build-time errors, not just type errors.

## Related Files

- `.githooks/pre-commit` - Pre-commit hook script
- `ui/tsconfig.app.json` - TypeScript configuration with strict settings
- `ui/src/components/ProductShippingPreview.tsx` - File with the error
- `.github/workflows/ci-cd-appservice.yml` - GitHub Actions workflow that failed

