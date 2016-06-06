package org.activiti.scripting.secure.impl;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.VariableScope;
import org.mozilla.javascript.Scriptable;

/**
 * @author Joram Barrez
 */
public class SecureScriptScope implements Scriptable {

    private static final String KEYWORD_EXECUTION = "execution";
    private static final String KEYWORD_TASK = "task";

    protected VariableScope variableScope;

    public SecureScriptScope(VariableScope variableScope) {
        super();
        this.variableScope = variableScope;
    }

    @Override
    public String getClassName() {
        return variableScope.getClass().getName();
    }

    @Override
    public Object get(String s, Scriptable scriptable) {
        if (KEYWORD_EXECUTION.equals(s) && variableScope instanceof DelegateExecution) {
            return variableScope;
        } else if (KEYWORD_TASK.equals(s) && variableScope instanceof DelegateTask) {
          return variableScope;
        } else if (variableScope.hasVariable(s)) {
            return variableScope.getVariable(s);
        }
        return null;
    }

    @Override
    public Object get(int i, Scriptable scriptable) {
        return null;
    }

    @Override
    public boolean has(String s, Scriptable scriptable) {
        return variableScope.hasVariable(s);
    }

    @Override
    public boolean has(int i, Scriptable scriptable) {
        return false;
    }

    @Override
    public void put(String s, Scriptable scriptable, Object o) {
        System.out.println("Putting " + s);
    }

    @Override
    public void put(int i, Scriptable scriptable, Object o) {
    }

    @Override
    public void delete(String s) {
    }

    @Override
    public void delete(int i) {
    }

    @Override
    public Scriptable getPrototype() {
        return null;
    }

    @Override
    public void setPrototype(Scriptable scriptable) {
    }

    @Override
    public Scriptable getParentScope() {
        return null;
    }

    @Override
    public void setParentScope(Scriptable scriptable) {
    }

    @Override
    public Object[] getIds() {
        return null;
    }

    @Override
    public Object getDefaultValue(Class<?> aClass) {
        return null;
    }

    @Override
    public boolean hasInstance(Scriptable scriptable) {
        return false;
    }
}
