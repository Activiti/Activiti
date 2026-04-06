/*
 * Copyright 2010-2026 Hyland Software, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.impl.scripting;

import java.util.regex.Pattern;
import org.activiti.engine.ActivitiException;

/**
 * Validates script content to prevent injection of dangerous code through
 * dynamic BPMN overrides. Blocks patterns that could lead to arbitrary
 * command execution, file system access, network access, or other
 * security-sensitive operations.
 */
public class ScriptContentValidator {

    private static final Pattern[] DANGEROUS_PATTERNS = {
        // Runtime command execution
        Pattern.compile("Runtime\\s*\\.\\s*getRuntime", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\.\\s*exec\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("ProcessBuilder", Pattern.CASE_INSENSITIVE),

        // Groovy shorthand for command execution
        Pattern.compile("\\.\\s*execute\\s*\\("),

        // System operations
        Pattern.compile("System\\s*\\.\\s*exit", Pattern.CASE_INSENSITIVE),
        Pattern.compile("System\\s*\\.\\s*setSecurityManager", Pattern.CASE_INSENSITIVE),
        Pattern.compile("System\\s*\\.\\s*getenv", Pattern.CASE_INSENSITIVE),
        Pattern.compile("System\\s*\\.\\s*getProperty", Pattern.CASE_INSENSITIVE),

        // Reflection-based attacks
        Pattern.compile("Class\\s*\\.\\s*forName", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\.\\s*getClass\\s*\\(\\s*\\)\\s*\\.\\s*getMethod", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\.\\s*getClass\\s*\\(\\s*\\)\\s*\\.\\s*getDeclaredMethod", Pattern.CASE_INSENSITIVE),
        Pattern.compile("java\\.lang\\.reflect\\.", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\.\\s*getClass\\s*\\(\\s*\\)\\s*\\.\\s*forName", Pattern.CASE_INSENSITIVE),

        // File system access
        Pattern.compile("new\\s+File\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("java\\.io\\.File", Pattern.CASE_INSENSITIVE),
        Pattern.compile("java\\.nio\\.file", Pattern.CASE_INSENSITIVE),
        Pattern.compile("FileInputStream", Pattern.CASE_INSENSITIVE),
        Pattern.compile("FileOutputStream", Pattern.CASE_INSENSITIVE),
        Pattern.compile("FileReader", Pattern.CASE_INSENSITIVE),
        Pattern.compile("FileWriter", Pattern.CASE_INSENSITIVE),

        // Network access
        Pattern.compile("java\\.net\\.Socket", Pattern.CASE_INSENSITIVE),
        Pattern.compile("java\\.net\\.URL", Pattern.CASE_INSENSITIVE),
        Pattern.compile("java\\.net\\.HttpURLConnection", Pattern.CASE_INSENSITIVE),
        Pattern.compile("ServerSocket", Pattern.CASE_INSENSITIVE),

        // Thread manipulation
        Pattern.compile("new\\s+Thread\\s*\\(", Pattern.CASE_INSENSITIVE),
        Pattern.compile("Thread\\s*\\.\\s*sleep", Pattern.CASE_INSENSITIVE),

        // Class loading
        Pattern.compile("ClassLoader", Pattern.CASE_INSENSITIVE),
        Pattern.compile("URLClassLoader", Pattern.CASE_INSENSITIVE),

        // Scripting engine escape
        Pattern.compile("ScriptEngine", Pattern.CASE_INSENSITIVE),
        Pattern.compile("javax\\.script\\.", Pattern.CASE_INSENSITIVE),
    };

    private ScriptContentValidator() {
    }

    /**
     * Validates that the given script content does not contain dangerous patterns
     * that could lead to arbitrary code execution or other security issues.
     *
     * @param script the script content to validate
     * @throws ActivitiException if the script contains dangerous patterns
     */
    public static void validate(String script) {
        if (script == null || script.isEmpty()) {
            return;
        }

        for (Pattern pattern : DANGEROUS_PATTERNS) {
            if (pattern.matcher(script).find()) {
                throw new ActivitiException(
                    "Script content validation failed: script contains potentially dangerous pattern '" +
                    pattern.pattern() +
                    "'. Dynamic script overrides must not contain system commands, " +
                    "file/network access, reflection, or other security-sensitive operations."
                );
            }
        }
    }
}
