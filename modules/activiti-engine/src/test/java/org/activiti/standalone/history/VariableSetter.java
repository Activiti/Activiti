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

package org.activiti.standalone.history;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.test.history.SerializableVariable;


/**
 * @author Tom Baeyens
 */
public class VariableSetter implements JavaDelegate {

  public void execute(DelegateExecution execution) throws Exception {
    
    SimpleDateFormat sdf =  new SimpleDateFormat("dd/MM/yyyy hh:mm:ss SSS");
    // We set the time to check of the updated time is picked up in the history
    Date updatedDate = sdf.parse("01/01/2001 01:23:46 000");
    Context.getProcessEngineConfiguration().getClock().setCurrentTime(updatedDate);
    
    
    execution.setVariable("aVariable", "updated value");
    execution.setVariable("bVariable", 123);
    execution.setVariable("cVariable", 12345L);
    execution.setVariable("dVariable", 1234.567);
    execution.setVariable("eVariable", (short)12);
    
    Date theDate =sdf.parse("01/01/2001 01:23:45 678");
    execution.setVariable("fVariable", theDate);
    
    execution.setVariable("gVariable", new SerializableVariable("hello hello"));
    execution.setVariable("hVariable", ";-)".getBytes());
  }

}
