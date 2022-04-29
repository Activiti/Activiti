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


package org.activiti.engine.test.bpmn.event.timer;

import static org.assertj.core.api.Assertions.assertThat;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;

/**
 * Test case for ACT-4066
 */
public class StartTimerEventRepeatWithoutN extends PluggableActivitiTestCase {

	protected long counter = 0;
	protected StartEventListener startEventListener;

	class StartEventListener implements ActivitiEventListener {

		@Override
		public void onEvent(ActivitiEvent event) {
			if (event.getType().equals(ActivitiEventType.TIMER_FIRED)) {
				counter++;
			}
		}

		@Override
		public boolean isFailOnException() {
			return false;
		}

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		startEventListener = new StartEventListener();
		processEngineConfiguration.getEventDispatcher().addEventListener(startEventListener);
	}



	@Override
  protected void tearDown() throws Exception {
	  processEngineConfiguration.getEventDispatcher().removeEventListener(startEventListener);
    super.tearDown();
  }



  @Deployment
	public void testStartTimerEventRepeatWithoutN() {
		counter = 0;

		try {
			waitForJobExecutorToProcessAllJobs(5500, 500);
			fail("job is finished sooner than expected");
		} catch (ActivitiException e) {
			assertThat(e.getMessage().startsWith("time limit")).isTrue();
			assertThat(counter >= 2).isTrue();
		}
	}

}
