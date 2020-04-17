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

package org.activiti.engine.test.transactions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.delegate.ActivityBehavior;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.test.Deployment;

/**

 */
public class TransactionRollbackTest extends PluggableActivitiTestCase {

    public static class Buzzz implements ActivityBehavior {

        private static final long serialVersionUID = 1L;

        @Override
        public void execute(DelegateExecution execution) {
            throw new ActivitiException("Buzzz");
        }
    }

    public static class Fizz implements ActivityBehavior {

        private static final long serialVersionUID = 1L;

        @Override
        public void execute(DelegateExecution execution) {
            throw new Error("Fizz");
        }
    }

    @Deployment
    public void testRollback() {
        assertThatExceptionOfType(Exception.class)
            .as("Starting the process instance should throw an exception")
            .isThrownBy(() -> runtimeService.startProcessInstanceByKey("RollbackProcess"))
            .withMessage("Buzzz");

        assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
    }

    @Deployment(
        resources = {"org/activiti/engine/test/transactions/trivial.bpmn20.xml",
            "org/activiti/engine/test/transactions/rollbackAfterSubProcess.bpmn20.xml"})
    public void testRollbackAfterSubProcess() {
        assertThatExceptionOfType(Exception.class)
            .as("Starting the process instance should throw an exception")
            .isThrownBy(() -> runtimeService.startProcessInstanceByKey("RollbackAfterSubProcess"))
            .withMessage("Buzzz");

        assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
    }

    @Deployment
    public void testRollbackAfterError() {
        assertThatExceptionOfType(Throwable.class)
            .as("Starting the process instance should throw an error")
            .isThrownBy(() -> runtimeService.startProcessInstanceByKey("RollbackProcess"))
            .withMessage("Fizz");

        assertThat(runtimeService.createExecutionQuery().count()).isEqualTo(0);
    }
}
