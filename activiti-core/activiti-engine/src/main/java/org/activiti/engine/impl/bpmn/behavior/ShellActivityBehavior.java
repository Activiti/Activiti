/*
 * Copyright 2010-2022 Alfresco Software, Ltd.
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
package org.activiti.engine.impl.bpmn.behavior;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.cfg.security.CommandExecutorContext;
import org.activiti.engine.cfg.security.CommandExecutorFactory;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.util.CommandExecutor;
import org.activiti.engine.impl.util.ShellCommandExecutor;
import org.activiti.engine.impl.util.ShellExecutorContext;

public class ShellActivityBehavior extends AbstractBpmnActivityBehavior {

  private static final long serialVersionUID = 1L;

  protected Expression command;
  protected Expression wait;
  protected Expression arg1;
  protected Expression arg2;
  protected Expression arg3;
  protected Expression arg4;
  protected Expression arg5;
  protected Expression outputVariable;
  protected Expression errorCodeVariable;
  protected Expression redirectError;
  protected Expression cleanEnv;
  protected Expression directory;

  String commandStr;
  String arg1Str;
  String arg2Str;
  String arg3Str;
  String arg4Str;
  String arg5Str;
  String waitStr;
  String resultVariableStr;
  String errorCodeVariableStr;
  Boolean waitFlag;
  Boolean redirectErrorFlag;
  Boolean cleanEnvBoolean;
  String directoryStr;

  private void readFields(DelegateExecution execution) {
    commandStr = getStringFromField(command, execution);
    arg1Str = getStringFromField(arg1, execution);
    arg2Str = getStringFromField(arg2, execution);
    arg3Str = getStringFromField(arg3, execution);
    arg4Str = getStringFromField(arg4, execution);
    arg5Str = getStringFromField(arg5, execution);
    waitStr = getStringFromField(wait, execution);
    resultVariableStr = getStringFromField(outputVariable, execution);
    errorCodeVariableStr = getStringFromField(errorCodeVariable, execution);

    String redirectErrorStr = getStringFromField(redirectError, execution);
    String cleanEnvStr = getStringFromField(cleanEnv, execution);

    waitFlag = waitStr == null || waitStr.equals("true");
    redirectErrorFlag = "true".equals(redirectErrorStr);
    cleanEnvBoolean = "true".equals(cleanEnvStr);
    directoryStr = getStringFromField(directory, execution);

  }

  public void execute(DelegateExecution execution) {

        readFields(execution);

        List<String> argList = new ArrayList<String>();
        argList.add(commandStr);

        if (arg1Str != null)
            argList.add(arg1Str);
        if (arg2Str != null)
            argList.add(arg2Str);
        if (arg3Str != null)
            argList.add(arg3Str);
        if (arg4Str != null)
            argList.add(arg4Str);
        if (arg5Str != null)
            argList.add(arg5Str);

        ShellExecutorContext executorContext = new ShellExecutorContext(
                waitFlag,
                cleanEnvBoolean,
                redirectErrorFlag,
                directoryStr,
                resultVariableStr,
                errorCodeVariableStr,
                argList);

        CommandExecutor commandExecutor =  null;

        CommandExecutorFactory shellCommandExecutorFactory = CommandExecutorContext.getShellCommandExecutorFactory();

        if (shellCommandExecutorFactory != null) {
            // if there is a ShellExecutorFactoryProvided
            // then it will be used to create a desired shell command executor.
            commandExecutor = shellCommandExecutorFactory.createExecutor(executorContext);
        } else {
            // default Shell executor (if the shell security is OFF)
            commandExecutor = new ShellCommandExecutor(executorContext);
        }

        try {
            commandExecutor.executeCommand(execution);
        } catch (Exception e) {
            throw new ActivitiException("Could not execute shell command ", e);
        }

        leave(execution);
    }

  protected String getStringFromField(Expression expression, DelegateExecution execution) {
    if (expression != null) {
      Object value = expression.getValue(execution);
      if (value != null) {
        return value.toString();
      }
    }
    return null;
  }

}
