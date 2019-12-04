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

package org.activiti.engine.impl.el;

import java.lang.reflect.Method;

import javax.el.FunctionMapper;

/**
 * Default implementation of a {@link FunctionMapper}.
 * 
 * A non-null implementation is required by the javax.el.* classes, hence the reason for this pretty useless class.
 * 

 */
public class ActivitiFunctionMapper extends FunctionMapper {

  public Method resolveFunction(String prefix, String localName) {
    return null;
  }

}
