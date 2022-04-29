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
package org.activiti.engine.test.profiler;

import java.util.Map;


public class ConsoleLogger {

    protected ActivitiProfiler profiler;

    public ConsoleLogger(ActivitiProfiler profiler) {
        this.profiler = profiler;
    }

    public void log() {
        for (ProfileSession profileSession : profiler.getProfileSessions()) {

            System.out.println();
            System.out.println("#############################################");
            System.out.println("#############################################");
            System.out.println(profileSession.getName());
            System.out.println("#############################################");
            System.out.println("#############################################");

            System.out.println();
            System.out.println("Start time: " + profileSession.getStartTime());
            System.out.println("End time: " + profileSession.getEndTime());
            System.out.println("Total time: " + profileSession.getTotalTime() + " ms");
            System.out.println();

            Map<String, CommandStats> allStats = profileSession.calculateSummaryStatistics();
            for (String classFqn : allStats.keySet()) {
                CommandStats stats = allStats.get(classFqn);
                System.out.println("Command class: " + classFqn);
                System.out.println("Number of times invoked: " + stats.getCount());
                double commandPercentage = (double) stats.getTotalCommandTime / (double) profileSession.getTotalTime();
                System.out.println((100.0 * Math.round(commandPercentage * 100.0) / 100.0) + "% of profile session was spent executing this command");

                System.out.println();
                System.out.println("Average execution time: " + stats.getAverageExecutionTime()
                        + " ms (Average database time: " + stats.getAverageDatabaseExecutionTime()
                        + " ms (" + stats.getAverageDatabaseExecutionTimePercentage() + "%) )");

                System.out.println();
                System.out.println("Database selects:");
                for (String select : stats.getDbSelects().keySet()) {
                    System.out.println(select + " : " + stats.getDbSelects().get(select));
                }

                System.out.println();
                System.out.println("Database inserts:");
                for (String insert : stats.getDbInserts().keySet()) {
                    System.out.println(insert + " : " + stats.getDbInserts().get(insert));
                }

                System.out.println();
                System.out.println("Database updates:");
                for (String update : stats.getDbUpdates().keySet()) {
                    System.out.println(update + " : " + stats.getDbSelects().get(update));
                }

                System.out.println();
                System.out.println("Database delete:");
                for (String delete : stats.getDbDeletes().keySet()) {
                    System.out.println(delete + " : " + stats.getDbDeletes().get(delete));
                }


                System.out.println();
                System.out.println();
            }

        }
    }

}
