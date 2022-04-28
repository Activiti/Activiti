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

package org.activiti.engine.test.bpmn.async;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.history.HistoryLevel;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;

public class AsyncExclusiveJobsTest extends PluggableActivitiTestCase {

	/**
	 * Test for https://activiti.atlassian.net/browse/ACT-4035.
	 */
	@Deployment
	public void testExclusiveJobs() {

		if (processEngineConfiguration.getHistoryLevel().isAtLeast(HistoryLevel.AUDIT)) {

			// The process has two script tasks in parallel, both exclusive.
			// They should be executed with at least 1 second in between (as they both sleep for 1 second)
			runtimeService.startProcessInstanceByKey("testExclusiveJobs");
			waitForJobExecutorToProcessAllJobs(20000L);

			HistoricActivityInstance scriptTaskAInstance = historyService.createHistoricActivityInstanceQuery().activityId("scriptTaskA").singleResult();
			HistoricActivityInstance scriptTaskBInstance = historyService.createHistoricActivityInstanceQuery().activityId("scriptTaskB").singleResult();

			long endTimeA = scriptTaskAInstance.getEndTime().getTime();
			long endTimeB = scriptTaskBInstance.getEndTime().getTime();
			long endTimeDifference = 0;
			if (endTimeB > endTimeA) {
				endTimeDifference = endTimeB - endTimeA;
			} else {
				endTimeDifference = endTimeA - endTimeB;
			}
			assertThat(endTimeDifference > 1000).isTrue(); // > 1000 -> jobs were executed in parallel
		}

	}

}
