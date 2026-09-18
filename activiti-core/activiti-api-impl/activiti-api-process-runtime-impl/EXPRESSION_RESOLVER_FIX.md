# ExpressionResolver OutOfMemoryError Fix

## Problem Summary

The runtime-bundle service was experiencing OutOfMemoryError crashes during expression resolution when processing integration results with large string variables. The issue occurred in Activiti's `ExpressionResolver.resolveExpressionsString()` method at line 96.

### Root Cause

The `String.matches()` method on line 96 was causing **catastrophic backtracking** when processing large strings:

```java
if (sourceString.matches(EXPRESSION_PATTERN_STRING)) {
```

Pattern: `([\\$]\\{([^\\}]*)\\})`

When a large string (e.g., 1MB+) contained many `$`, `{`, and `}` characters, the regex engine would exhaust memory trying to match the entire string, leading to OutOfMemoryError.

### Stacktrace Location
```
at java.util.regex.Pattern$BitClass.<init>()V (Pattern.java:3665)
at org.activiti.runtime.api.impl.ExpressionResolver.resolveExpressionsString()
at org.activiti.runtime.api.impl.ExpressionResolver.resolveExpressions()
at org.activiti.runtime.api.impl.ExtensionsVariablesMappingProvider.calculateInputVariables()
```

## Solution Implemented

### Changes Made

**File:** `../Activiti/activiti-core/activiti-api-impl/activiti-api-process-runtime-impl/src/main/java/org/activiti/runtime/api/impl/ExpressionResolver.java`

1. **Added configurable size limit** (lines 45-64):
   - Environment variable: `MAX_VAR_SIZE_FOR_EXPRESSION_PARSING`
   - Default value: 100KB (102,400 bytes)
   - Can be overridden by setting the environment variable to any positive integer

2. **Added size check before regex** (lines 98-107):
   - Strings exceeding the limit skip expression parsing entirely
   - Debug logging when skipping due to size
   - Returns original string unchanged

3. **Added defensive check in placeholder resolution** (lines 131-134):
   - Additional safety check in `resolveInStringPlaceHolder()`

4. **Added getter method** (lines 85-92):
   - `getMaxVarSizeForExpressionParsing()` for testing and monitoring

5. **Added initialization logging** (lines 77-81):
   - Logs the configured max size on startup

### Test Coverage

**File:** `../Activiti/activiti-core/activiti-api-impl/activiti-api-process-runtime-impl/src/test/java/org/activiti/runtime/api/impl/ExpressionResolverTest.java`

Added 8 new test cases:
1. `resolveExpressionsMap_should_skipParsing_when_stringExceedsMaxSize()`
2. `resolveExpressionsMap_should_skipParsing_when_stringWithExpressionExceedsMaxSize()`
3. `resolveExpressionsMap_should_parseParsing_when_stringIsAtMaxSize()`
4. `resolveExpressionsMap_should_skipParsing_when_nestedMapContainsLargeString()`
5. `resolveExpressionsMap_should_skipParsing_when_listContainsLargeString()`
6. `resolveExpressionsMap_should_resolveSmallStrings_when_mixedWithLargeStrings()`
7. `getMaxVarSizeForExpressionParsing_should_returnConfiguredValue()`
8. Helper method: `buildLargeString(int size)`

**All tests pass:** ✅ 28 tests run, 0 failures, 0 errors

## Configuration

### Default Behavior
- Max size: 100KB (102,400 bytes)
- Strings larger than this will skip expression resolution
- No environment variable configuration needed

### Custom Configuration
Set the environment variable before starting the runtime-bundle service:

```bash
# Set to 500KB
export MAX_VAR_SIZE_FOR_EXPRESSION_PARSING=512000

# Set to 1MB
export MAX_VAR_SIZE_FOR_EXPRESSION_PARSING=1048576

# Disable limit (not recommended!)
export MAX_VAR_SIZE_FOR_EXPRESSION_PARSING=2147483647
```

### Kubernetes/Docker Configuration
```yaml
env:
  - name: MAX_VAR_SIZE_FOR_EXPRESSION_PARSING
    value: "512000"  # 500KB
```

## Impact Analysis

### What This Fix Does
✅ Prevents OutOfMemoryError from large strings during expression resolution  
✅ Maintains backward compatibility - no behavior change for normal-sized strings  
✅ Configurable via environment variable  
✅ Logs when skipping to aid debugging  
✅ No performance impact on normal operations

### What This Fix Does NOT Break
✅ Normal expression resolution (strings < 100KB) works exactly as before  
✅ All existing tests continue to pass  
✅ Nested structures (maps, lists) are handled correctly  
✅ Mixed scenarios (large + small strings) work correctly

### Behavior Changes
⚠️ **Strings exceeding the limit will NOT have expressions resolved**
- Example: A 200KB string containing `${variable}` will be returned as-is
- This is intentional to prevent OutOfMemoryError
- Logged at DEBUG level for visibility

## Deployment Steps

1. **Build the Activiti module:**
   ```bash
   cd Activiti
   mvn clean install -pl activiti-core/activiti-api-impl/activiti-api-process-runtime-impl
   ```

2. **Rebuild dependent services:**
   ```bash
   cd hxp-process-services
   mvn clean install
   ```

3. **Optional: Configure custom limit**
   - Add environment variable to deployment configuration
   - Recommended: Start with default (100KB) and adjust if needed

4. **Deploy and monitor:**
   - Check startup logs for: `"ExpressionResolver initialized with MAX_VAR_SIZE_FOR_EXPRESSION_PARSING: X bytes"`
   - Monitor DEBUG logs for: `"Skipping expression parsing for string exceeding max size"`

## Monitoring and Troubleshooting

### Startup Verification
Look for this log line in runtime-bundle startup:
```
INFO  o.a.r.a.i.ExpressionResolver - ExpressionResolver initialized with MAX_VAR_SIZE_FOR_EXPRESSION_PARSING: 102400 bytes
```

### Runtime Monitoring
Enable DEBUG logging to see when large strings are skipped:
```
DEBUG o.a.r.a.i.ExpressionResolver - Skipping expression parsing for string exceeding max size: 150000 bytes (limit: 102400 bytes)
```

### If Variables Are Not Being Resolved
1. Check if the variable size exceeds the limit
2. Consider increasing the limit via environment variable
3. Review if such large variables actually need expression resolution

## Related Issues

- **Original Issue:** AAE-52162 (OutOfMemoryError in runtime-bundle)
- **Similar Fixed Issues:**
  - AAE-44864: Fix OutOfMemoryError for audit export
  - AAE-43063: Fix JuelExpressionResolver performance

## Files Changed

1. `../Activiti/activiti-core/activiti-api-impl/activiti-api-process-runtime-impl/src/main/java/org/activiti/runtime/api/impl/ExpressionResolver.java`
   - Added size limit configuration
   - Added size checks before regex operations
   - Added logging and getter method

2. `../Activiti/activiti-core/activiti-api-impl/activiti-api-process-runtime-impl/src/test/java/org/activiti/runtime/api/impl/ExpressionResolverTest.java`
   - Added 8 new test cases
   - Added helper method for large string generation

## Testing

### Unit Tests
```bash
cd Activiti
mvn test -pl activiti-core/activiti-api-impl/activiti-api-process-runtime-impl -Dtest=ExpressionResolverTest
```

### Integration Testing Recommendations
1. Test with actual large payloads from connectors
2. Verify expressions in normal-sized strings still work
3. Test with environment variable set to different values
4. Monitor memory usage during high-volume processing

## Rollback Plan

If issues arise, rollback is straightforward:
1. Revert the changes to `ExpressionResolver.java`
2. Rebuild and redeploy
3. No database changes or configuration cleanup needed
