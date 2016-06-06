package org.activiti.impl.scripting.secure.behavior;

import org.activiti.engine.impl.bpmn.behavior.ScriptTaskActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.activiti.impl.scripting.secure.rhino.SecureScriptScope;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

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
        Context context = Context.enter();
        try {
            Scriptable scope = context.initStandardObjects();
            SecureScriptScope secureScriptScope = new SecureScriptScope(execution);
            scope.setPrototype(secureScriptScope);

            context.evaluateString(scope, script, "<script>", 0, null);
        } finally {
            Context.exit();
        }
    }

}
