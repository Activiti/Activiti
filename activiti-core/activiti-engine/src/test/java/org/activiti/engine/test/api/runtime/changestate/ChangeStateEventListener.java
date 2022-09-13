/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.engine.test.api.runtime.changestate;

import org.activiti.engine.delegate.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author LoveMyOrange
 */
public class ChangeStateEventListener implements ActivitiEventListener {
    private List<ActivitiEvent> events = new ArrayList<>();
    private static final Logger logger = LoggerFactory.getLogger(ChangeStateEventListener.class);

    @Override
    public void onEvent(ActivitiEvent event) {
        ActivitiEventType type = event.getType();
        if (ActivitiEventType.ACTIVITY_STARTED.name().equals(type.name())) {
            ActivitiActivityEvent activitiActivityEvent = (ActivitiActivityEvent) event;
            List<String> types = Arrays.asList("userTask", "subProcess", "callActivity");
            if (types.contains(activitiActivityEvent.getActivityType())) {
                events.add(event);
                logger.info("ACTIVITY_STARTED.....");
            }
        }

        if (ActivitiEventType.ACTIVITY_CANCELLED.name().equals(type.name())) {
            ActivitiActivityCancelledEvent activitiActivityCancelledEvent = (ActivitiActivityCancelledEvent) event;
            List<String> types = Arrays.asList("userTask", "subProcess", "callActivity");

            if (types.contains(activitiActivityCancelledEvent.getActivityType())) {
                events.add(event);
                logger.info("ACTIVITY_CANCELLED.....");
            }
        }
        if (ActivitiEventType.TIMER_SCHEDULED.name().equals(type.name())) {
            events.add(event);
            logger.info("TIMER_SCHEDULED.....");
        }
        if (ActivitiEventType.PROCESS_STARTED.name().equals(type.name())) {
            events.add(event);
            logger.info("PROCESS_STARTED.....");
        }
        if (ActivitiEventType.JOB_CANCELED.name().equals(type.name())) {
            events.add(event);
            logger.info("JOB_CANCELED.....");
        }
        if (ActivitiEventType.PROCESS_CANCELLED.name().equals(type.name())) {
            events.add(event);
            logger.info("PROCESS_CANCELLED.....");
        }
        if (ActivitiEventType.VARIABLE_UPDATED.name().equals(type.name())) {
            events.add(event);
            logger.info("VARIABLE_UPDATED.....");
        }
        if (ActivitiEventType.VARIABLE_CREATED.name().equals(type.name())) {
            events.add(event);
            logger.info("VARIABLE_CREATED.....");
        }
        if (ActivitiEventType.ACTIVITY_SIGNALED.name().equals(type.name())) {
            events.add(event);
            logger.info("ACTIVITY_SIGNALED.....");
        }
        if (ActivitiEventType.ACTIVITY_MESSAGE_WAITING.name().equals(type.name())) {
            events.add(event);
            logger.info("ACTIVITY_MESSAGE_WAITING.....");
        }
        if (ActivitiEventType.ACTIVITY_MESSAGE_RECEIVED.name().equals(type.name())) {
            events.add(event);
            logger.info("ACTIVITY_MESSAGE_RECEIVED.....");
        }
    }

    public void clear() {
        events.clear();
    }

    public Iterator<ActivitiEvent> iterator() {
        return events.iterator();
    }

    public List<ActivitiEvent> getEvents() {
        return events;
    }

    public boolean hasEvents() {
        return !events.isEmpty();
    }

    public int eventCount() {
        return events.size();
    }

    @Override
    public boolean isFailOnException() {
        return true;
    }
}

