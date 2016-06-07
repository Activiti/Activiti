package org.activiti.scripting.secure.behavior;

import org.activiti.engine.impl.bpmn.behavior.ScriptTaskActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.scripting.secure.impl.SecureJavascriptUtil;

/**
 * @author Joram Barrez
 */
public class SecureJavascriptTaskActivityBehavior extends ScriptTaskActivityBehavior {

    public SecureJavascriptTaskActivityBehavior(String scriptTaskId, String script,
                                            String language, String resultVariable, boolean storeScriptVariables) {
        super(scriptTaskId, script, language, resultVariable, storeScriptVariables);
    }

    @Override
    public void execute(ActivityExecution execution) throws Exception {
      SecureJavascriptUtil.evaluateScript(execution, script);
    }

}
