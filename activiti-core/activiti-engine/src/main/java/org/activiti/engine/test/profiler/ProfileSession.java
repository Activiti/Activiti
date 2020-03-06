package org.activiti.engine.test.profiler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**

 */
public class ProfileSession {

    protected String name;
    protected Date startTime;
    protected Date endTime;
    protected long totalTime;

    protected ThreadLocal<CommandExecutionResult> currentCommandExecution = new ThreadLocal<CommandExecutionResult>();
    protected Map<String, List<CommandExecutionResult>> commandExecutionResults = new HashMap<String, List<CommandExecutionResult>>();

    public ProfileSession(String name) {
        this.name = name;
        this.startTime = new Date();
    }

    public CommandExecutionResult getCurrentCommandExecution() {
        return currentCommandExecution.get();
    }

    public void setCurrentCommandExecution(CommandExecutionResult commandExecutionResult) {
        currentCommandExecution.set(commandExecutionResult);
    }

    public void clearCurrentCommandExecution() {
        currentCommandExecution.set(null);
    }

    public synchronized void addCommandExecution(String classFqn, CommandExecutionResult commandExecutionResult) {
        if (!commandExecutionResults.containsKey(classFqn)) {
            commandExecutionResults.put(classFqn, new ArrayList<CommandExecutionResult>());
        }
        commandExecutionResults.get(classFqn).add(commandExecutionResult);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTimeStamp) {
        this.endTime = endTimeStamp;

        if (startTime != null) {
            this.totalTime = this.endTime.getTime() - this.startTime.getTime();
        }
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(long totalTime) {
        this.totalTime = totalTime;
    }

    public Map<String, List<CommandExecutionResult>> getCommandExecutions() {
        return commandExecutionResults;
    }

    public void setCommandExecutions(Map<String, List<CommandExecutionResult>> commandExecutionResults) {
        this.commandExecutionResults = commandExecutionResults;
    }

    public Map<String, CommandStats> calculateSummaryStatistics() {
        Map<String, CommandStats> result = new HashMap<String, CommandStats>();
        for (String className : commandExecutionResults.keySet()) {
            List<CommandExecutionResult> executions = commandExecutionResults.get(className);
            CommandStats commandStats = new CommandStats(executions);
            result.put(className, commandStats);
        }
        return result;
    }

}
