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

1. **Added configurable size limit**:
   - Environment variable: `MAX_VAR_SIZE_FOR_EXPRESSION_PARSING`
   - Default value: unlimited
   - Can be overridden by setting the environment variable to any positive integer

2. **Added optional size check before expression resolution**:
   - Strings exceeding the limit skip expression parsing entirely
   - Debug logging when skipping due to size
   - Returns original string unchanged

3. **Replaced regex-based expression scanning with string scanning**:
   - `containsExpressionString()` no longer runs the regex matcher across the full value
   - `resolveExpressionsString()` and `resolveInStringPlaceHolder()` detect placeholders without the backtracking-prone pattern

4. **Added getter/configuration helpers**:
   - `getMaxVarSizeForExpressionParsing()` for testing and monitoring

5. **Added initialization logging**:
   - Logs the configured max size on startup

### Test Coverage

**File:** `../Activiti/activiti-core/activiti-api-impl/activiti-api-process-runtime-impl/src/test/java/org/activiti/runtime/api/impl/ExpressionResolverTest.java`

Added targeted test cases covering:
1. Configured size-limit skip behavior
2. Boundary behavior at the configured limit
3. Nested maps/lists with configured limits
4. Mixed large/small value handling
5. Large-value expression detection without regex matching
6. Default unlimited configuration
7. Configuration parsing helpers

**All tests pass:** ✅ 28 tests run, 0 failures, 0 errors

## Configuration

### Default Behavior
- Max size: unlimited
- All strings are parsed unless a positive limit is configured
- No environment variable configuration needed

### Custom Configuration
Set the environment variable before starting the runtime-bundle service:

```bash
# Set to 500KB
export MAX_VAR_SIZE_FOR_EXPRESSION_PARSING=512000

# Set to 1MB
export MAX_VAR_SIZE_FOR_EXPRESSION_PARSING=1048576

# Restore effectively unlimited parsing
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
✅ Avoids the unbounded regex scan in expression detection/resolution paths
✅ Maintains parsing behavior by default
✅ Optional size guard remains configurable via environment variable
✅ Logs when skipping to aid debugging  
✅ No regex backtracking risk in the detection path

### What This Fix Does NOT Break
✅ Normal expression resolution works exactly as before when no limit is configured
✅ All existing tests continue to pass  
✅ Nested structures (maps, lists) are handled correctly  
✅ Mixed scenarios (large + small strings) work correctly

### Behavior Changes
⚠️ **Strings exceeding a configured limit will NOT have expressions resolved**
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
   - Recommended: leave unlimited unless a deployment needs a hard guard

4. **Deploy and monitor:**
   - Check startup logs for: `"ExpressionResolver initialized with MAX_VAR_SIZE_FOR_EXPRESSION_PARSING: unlimited"` or a configured byte limit
   - Monitor DEBUG logs for: `"Skipping expression parsing for string exceeding max size"`

## Monitoring and Troubleshooting

### Startup Verification
Look for this log line in runtime-bundle startup:
```
INFO  o.a.r.a.i.ExpressionResolver - ExpressionResolver initialized with MAX_VAR_SIZE_FOR_EXPRESSION_PARSING: unlimited
```

### Runtime Monitoring
Enable DEBUG logging to see when large strings are skipped:
```
DEBUG o.a.r.a.i.ExpressionResolver - Skipping expression parsing for string exceeding max size: 150000 characters (limit: 102400 characters)
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
   - Replaced regex-based expression scanning with string scanning
   - Added logging and getter method

2. `../Activiti/activiti-core/activiti-api-impl/activiti-api-process-runtime-impl/src/test/java/org/activiti/runtime/api/impl/ExpressionResolverTest.java`
   - Added targeted tests for configurable limits and non-regex expression scanning
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
