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
package org.activiti.engine.impl.util;

import java.util.Date;


/**
 * @author Joram Barrez
 */
public class DefaultClockImpl implements org.activiti.engine.runtime.Clock {
  
  private static volatile Date CURRENT_TIME = null;

  @Override
  public void setCurrentTime(Date currentTime) {
    this.CURRENT_TIME = currentTime;
  }
  
  @Override
  public void reset() {
    this.CURRENT_TIME = null;
  } 
  
  @Override
  public Date getCurrentTime() {
    if (CURRENT_TIME != null) {
      return CURRENT_TIME;
    }
    return new Date();
  }

}
