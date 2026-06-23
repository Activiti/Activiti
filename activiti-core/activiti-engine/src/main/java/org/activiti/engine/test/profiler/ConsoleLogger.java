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
package org.activiti.engine.test.profiler;

import java.util.Map;

/**

 */
public class ConsoleLogger {

    protected ActivitiProfiler profiler;

    public ConsoleLogger(ActivitiProfiler profiler) {
        this.profiler = profiler;
    }

    public void log() {
        for (ProfileSession profileSession : profiler.getProfileSessions()) {
            IO.println();
            IO.println("#############################################");
            IO.println("#############################################");
            IO.println(profileSession.getName());
            IO.println("#############################################");
            IO.println("#############################################");

            IO.println();
            IO.println("Start time: " + profileSession.getStartTime());
            IO.println("End time: " + profileSession.getEndTime());
            IO.println("Total time: " + profileSession.getTotalTime() + " ms");
            IO.println();

            Map<String, CommandStats> allStats = profileSession.calculateSummaryStatistics();
            for (String classFqn : allStats.keySet()) {
                CommandStats stats = allStats.get(classFqn);
                IO.println("Command class: " + classFqn);
                IO.println("Number of times invoked: " + stats.getCount());
                double commandPercentage = (double) stats.getTotalCommandTime / (double) profileSession.getTotalTime();
                IO.println(
                    ((100.0 * Math.round(commandPercentage * 100.0)) / 100.0) +
                    "% of profile session was spent executing this command"
                );

                IO.println();
                IO.println(
                    "Average execution time: " +
                    stats.getAverageExecutionTime() +
                    " ms (Average database time: " +
                    stats.getAverageDatabaseExecutionTime() +
                    " ms (" +
                    stats.getAverageDatabaseExecutionTimePercentage() +
                    "%) )"
                );

                IO.println();
                IO.println("Database selects:");
                for (String select : stats.getDbSelects().keySet()) {
                    IO.println(select + " : " + stats.getDbSelects().get(select));
                }

                IO.println();
                IO.println("Database inserts:");
                for (String insert : stats.getDbInserts().keySet()) {
                    IO.println(insert + " : " + stats.getDbInserts().get(insert));
                }

                IO.println();
                IO.println("Database updates:");
                for (String update : stats.getDbUpdates().keySet()) {
                    IO.println(update + " : " + stats.getDbSelects().get(update));
                }

                IO.println();
                IO.println("Database delete:");
                for (String delete : stats.getDbDeletes().keySet()) {
                    IO.println(delete + " : " + stats.getDbDeletes().get(delete));
                }

                IO.println();
                IO.println();
            }
        }
    }
}
