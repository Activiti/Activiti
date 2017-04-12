package org.activiti.engine.impl.util;

import java.util.List;

import org.activiti.engine.cfg.security.ExecutorContext;

/**
 * @author Vasile Dirla
 */
public class ShellExecutorContext implements ExecutorContext {
    private Boolean waitFlag;
    private final Boolean cleanEnvBoolan;
    private final Boolean redirectErrorFlag;
    private final String directoryStr;
    private final String resultVariableStr;
    private final String errorCodeVariableStr;
    private List<String> argList;

    public ShellExecutorContext(Boolean waitFlag, Boolean cleanEnvBoolan, Boolean redirectErrorFlag, String directoryStr, String resultVariableStr, String errorCodeVariableStr, List<String> argList) {
        this.waitFlag = waitFlag;
        this.cleanEnvBoolan = cleanEnvBoolan;
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
