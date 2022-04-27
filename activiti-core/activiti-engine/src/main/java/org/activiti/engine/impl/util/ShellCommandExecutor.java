/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
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

package org.activiti.engine.impl.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;


public class ShellCommandExecutor implements CommandExecutor {
    private Boolean waitFlag;
    private final Boolean cleanEnvBoolean;
    private final Boolean redirectErrorFlag;
    private final String directoryStr;
    private final String resultVariableStr;
    private final String errorCodeVariableStr;
    private final List<String> argList;

    public ShellCommandExecutor(Boolean waitFlag, Boolean cleanEnvBoolean, Boolean redirectErrorFlag, String directoryStr, String resultVariableStr, String errorCodeVariableStr, List<String> argList) {
        this.waitFlag = waitFlag;
        this.cleanEnvBoolean = cleanEnvBoolean;
        this.redirectErrorFlag = redirectErrorFlag;
        this.directoryStr = directoryStr;
        this.resultVariableStr = resultVariableStr;
        this.errorCodeVariableStr = errorCodeVariableStr;
        this.argList = argList;
    }

    public ShellCommandExecutor(ShellExecutorContext context) {
        this(context.getWaitFlag(),
                context.getCleanEnvBoolan(),
                context.getRedirectErrorFlag(),
                context.getDirectoryStr(),
                context.getResultVariableStr(),
                context.getErrorCodeVariableStr(),
                context.getArgList());
    }


    public void executeCommand(DelegateExecution execution) throws Exception {
        if (argList != null && argList.size() > 0) {
            ProcessBuilder processBuilder = new ProcessBuilder(argList);
            processBuilder.redirectErrorStream(getRedirectErrorFlag());
            if (getCleanEnvBoolean()) {
                Map<String, String> env = processBuilder.environment();
                env.clear();
            }
            if (getDirectoryStr() != null && getDirectoryStr().length() > 0)
                processBuilder.directory(new File(getDirectoryStr()));

            Process process = processBuilder.start();

            if (getWaitFlag()) {
                int errorCode = process.waitFor();

                if (getResultVariableStr() != null) {
                    String result = convertStreamToStr(process.getInputStream());
                    execution.setVariable(getResultVariableStr(), result);
                }

                if (getErrorCodeVariableStr() != null) {
                    execution.setVariable(getErrorCodeVariableStr(), Integer.toString(errorCode));

                }

            }
        }
    }

    private String convertStreamToStr(InputStream is) throws IOException {

        if (is != null) {
            Writer writer = new StringWriter();

            char[] buffer = new char[1024];
            try {
                Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                    writer.write(buffer, 0, n);
                }
            } finally {
                is.close();
            }
            return writer.toString();
        } else {
            return "";
        }
    }

    public Boolean getWaitFlag() {
        return waitFlag;
    }

    public void setWaitFlag(Boolean waitFlag) {
        this.waitFlag = waitFlag;
    }

    public Boolean getCleanEnvBoolean() {
        return cleanEnvBoolean;
    }

    public Boolean getRedirectErrorFlag() {
        return redirectErrorFlag;
    }

    public String getDirectoryStr() {
        return directoryStr;
    }

    public String getResultVariableStr() {
        return resultVariableStr;
    }

    public String getErrorCodeVariableStr() {
        return errorCodeVariableStr;
    }

    public List<String> getArgList() {
        return argList;
    }

}
