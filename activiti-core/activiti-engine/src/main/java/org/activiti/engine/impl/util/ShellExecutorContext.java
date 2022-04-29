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

package org.activiti.engine.impl.util;

import java.util.List;

import org.activiti.engine.cfg.security.ExecutorContext;


public class ShellExecutorContext implements ExecutorContext {
    private Boolean waitFlag;
    private final Boolean cleanEnvBoolan;
    private final Boolean redirectErrorFlag;
    private final String directoryStr;
    private final String resultVariableStr;
    private final String errorCodeVariableStr;
    private List<String> argList;

    public ShellExecutorContext(Boolean waitFlag, Boolean cleanEnvBoolean, Boolean redirectErrorFlag, String directoryStr, String resultVariableStr, String errorCodeVariableStr, List<String> argList) {
        this.waitFlag = waitFlag;
        this.cleanEnvBoolan = cleanEnvBoolean;
        this.redirectErrorFlag = redirectErrorFlag;
        this.directoryStr = directoryStr;
        this.resultVariableStr = resultVariableStr;
        this.errorCodeVariableStr = errorCodeVariableStr;
        this.argList = argList;
    }

    public Boolean getWaitFlag() {
        return waitFlag;
    }

    public void setWaitFlag(Boolean waitFlag) {
        this.waitFlag = waitFlag;
    }

    public Boolean getCleanEnvBoolan() {
        return cleanEnvBoolan;
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

    public void setArgList(List<String> argList) {
        this.argList = argList;
    }
}
