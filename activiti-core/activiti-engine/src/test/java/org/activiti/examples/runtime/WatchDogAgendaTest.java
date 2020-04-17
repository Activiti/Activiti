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
package org.activiti.examples.runtime;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.task.Task;
import org.activiti.engine.test.Deployment;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * This class shows an example of configurable agenda usage.
 */
public class WatchDogAgendaTest extends ResourceActivitiTestCase {

    public WatchDogAgendaTest() {
        super(WatchDogAgendaTest.class.getName().replace(".", File.separator)+".activiti.cfg.xml");
    }

    @Deployment(resources = "org/activiti/engine/test/api/oneTaskProcess.bpmn20.xml")
    public void testWatchDogWithOneTaskProcess() {
        this.runtimeService.startProcessInstanceByKey("oneTaskProcess");
        Task task = this.taskService.createTaskQuery().singleResult();
        this.taskService.complete(task.getId());
        assertThat(this.runtimeService.createProcessInstanceQuery().count()).isEqualTo(0L);
    }

    @Deployment(resources = "org/activiti/examples/runtime/WatchDogAgendaTest-endlessloop.bpmn20.xml")
    public void testWatchDogWithEndLessLoop() {
        assertThatExceptionOfType(ActivitiException.class)
            .as("ActivitiException with 'WatchDog limit exceeded.' message expected.")
            .isThrownBy(() -> this.runtimeService.startProcessInstanceByKey("endlessloop"))
            .withMessageContaining("WatchDog limit exceeded.");
     }

}
