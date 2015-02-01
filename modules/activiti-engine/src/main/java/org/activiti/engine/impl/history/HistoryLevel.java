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

package org.activiti.engine.impl.history;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;

/**
 * Enum that contains all possible history-levels. 
 * 
 * @author Frederik Heremans
 */
public enum HistoryLevel {
  
  NONE("none"),
  ACTIVITY("activity"),
  AUDIT("audit"),
  FULL("full");
  
  private String key;
  
  private HistoryLevel(String key) {
    this.key = key;
  }
  
  /**
   * @param key string representation of level
   * @return {@link HistoryLevel} for the given key
   * @throws ActivitiException when passed in key doesn't correspond to existing level
   */
  public static HistoryLevel getHistoryLevelForKey(String key) {
    for(HistoryLevel level : values()) {
      if(level.key.equals(key)) {
        return level;
      }
    }
    throw new ActivitiIllegalArgumentException("Illegal value for history-level: " + key);
  }

  /**
   * String representation of this history-level.
   */
  public String getKey() {
    return key;
  }
  
  /**
   * Checks if the given level is the same as, or higher in order than the
   * level this method is executed on.
   */
  public boolean isAtLeast(HistoryLevel level) {
    // Comparing enums actually compares the location of values declared in the enum
    return this.compareTo(level) >= 0;
  }
}