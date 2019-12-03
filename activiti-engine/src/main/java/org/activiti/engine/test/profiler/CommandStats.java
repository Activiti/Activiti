package org.activiti.engine.test.profiler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**

 */
public class CommandStats {

    protected long getTotalCommandTime = 0L;
    
    protected List<Long> commandExecutionTimings = new ArrayList<Long>();
    protected List<Long> databaseTimings = new ArrayList<Long>();
    
    protected Map<String, Long> dbSelects = new HashMap<String, Long>();
    protected Map<String, Long> dbInserts = new HashMap<String, Long>();
    protected Map<String, Long> dbUpdates = new HashMap<String, Long>();
    protected Map<String, Long> dbDeletes = new HashMap<String, Long>();

    public CommandStats(List<CommandExecutionResult> executions) {
        for (CommandExecutionResult execution : executions) {
            getTotalCommandTime += execution.getTotalTimeInMs();
            
            commandExecutionTimings.add(execution.getTotalTimeInMs());
            databaseTimings.add(execution.getDatabaseTimeInMs());

            addToDbOperation(execution.getDbSelects(), dbSelects);
            addToDbOperation(execution.getDbInserts(), dbInserts);
            addToDbOperation(execution.getDbUpdates(), dbUpdates);
            addToDbOperation(execution.getDbDeletes(), dbDeletes);
        }
    }

    protected void addToDbOperation(Map<String, Long> executionMap, Map<String, Long> globalMap) {
        for (String key : executionMap.keySet()) {
            if (!globalMap.containsKey(key)) {
                globalMap.put(key, 0L);
            }
            Long oldValue = globalMap.get(key);
            globalMap.put(key, oldValue + executionMap.get(key));
        }
    }

    public long getCount() {
        return commandExecutionTimings.size();
    }

    public long getGetTotalCommandTime() {
        return getTotalCommandTime;
    }

    public double getAverageExecutionTime() {
        long total = 0;
        for (Long timing : commandExecutionTimings) {
          total += timing.longValue();
        }
        double average = (double) total / (double) commandExecutionTimings.size();
        return Math.round(average * 100.0) / 100.0;
    }

    public double getAverageDatabaseExecutionTimePercentage() {
        double totalAvg = getAverageExecutionTime();
        double databaseAvg = getAverageDatabaseExecutionTime();
        double percentage = 100.0 * (databaseAvg / totalAvg);
        return Math.round(percentage * 100.0) / 100.0;
    }

    public double getAverageDatabaseExecutionTime() {
      long total = 0;
      for (Long timing : databaseTimings) {
        total += timing.longValue();
      }
      double average = (double) total / (double) commandExecutionTimings.size();
      return Math.round(average * 100.0) / 100.0;
    }

    public Map<String, Long> getDbSelects() {
        return dbSelects;
    }

    public void setDbSelects(Map<String, Long> dbSelects) {
        this.dbSelects = dbSelects;
    }

    public Map<String, Long> getDbInserts() {
        return dbInserts;
    }

    public void setDbInserts(Map<String, Long> dbInserts) {
        this.dbInserts = dbInserts;
    }

    public Map<String, Long> getDbUpdates() {
        return dbUpdates;
    }

    public void setDbUpdates(Map<String, Long> dbUpdates) {
        this.dbUpdates = dbUpdates;
    }

    public Map<String, Long> getDbDeletes() {
        return dbDeletes;
    }

    public void setDbDeletes(Map<String, Long> dbDeletes) {
        this.dbDeletes = dbDeletes;
    }

}
