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
package org.activiti.impl.execution;


/**
 * @author Tom Baeyens
 */
public class ExeOpTransferOperationLoop implements ExeOp {

  ExecutionImpl otherExecution;
  ExeOp nextOperation;
  
  public ExeOpTransferOperationLoop(ExecutionImpl otherExecution, ExeOp nextOperation) {
    this.otherExecution = otherExecution;
    this.nextOperation = nextOperation;
  }

  public void execute(ExecutionImpl execution) {
    otherExecution.performOperation(nextOperation);
  }

  public boolean isAsync() {
    return false;
  }
  
  public String toString() {
    return "TransferOperation["+nextOperation+"|"+otherExecution+"]";
  }
}
