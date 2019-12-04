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
package org.activiti.engine.impl.variable;

import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandContextCloseListener;

/**
 * A {@link CommandContextCloseListener} that holds one {@link DeserializedObject} instance that
 * is added by the {@link SerializableType}.
 * 
 * On the {@link #closing(CommandContext)} of the {@link CommandContext}, the {@link DeserializedObject}
 * will be verified if it is dirty. If so, it will update the right entities such that changes will be flushed.
 * 
 * It's important that this happens in the {@link #closing(CommandContext)}, as this happens before
 * the {@link CommandContext#close()} is called and when all the sessions are flushed 
 * (including the {@link DbSqlSession} in the relational DB case (the data needs to be ready then). 
 * 

 */
public class VerifyDeserializedObjectCommandContextCloseListener implements CommandContextCloseListener {

  protected DeserializedObject deserializedObject;
  
  public VerifyDeserializedObjectCommandContextCloseListener(DeserializedObject deserializedObject) {
    this.deserializedObject = deserializedObject;
  }
  
  public void closing(CommandContext commandContext) {
    deserializedObject.verifyIfBytesOfSerializedObjectChanged();
  }

  public void closed(CommandContext commandContext) {
    
  }

  public void afterSessionsFlush(CommandContext commandContext) {
    
  }

  public void closeFailure(CommandContext commandContext) {
    
  }

}
