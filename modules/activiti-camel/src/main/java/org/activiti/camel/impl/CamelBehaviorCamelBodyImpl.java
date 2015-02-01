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

package org.activiti.camel.impl;

import org.activiti.camel.ActivitiEndpoint;
import org.activiti.camel.CamelBehavior;

/**
 * This implementation of the CamelBehavior abstract class works by copying a single variable value into the Camel
 * Exchange body. The variable must be named "camelBody" to be copied into the Camel Exchange body on the producer
 * side of the transfer (i.e. when handing control from Activiti to Camel).
 * 
 * @author Ryan Johnston (@rjfsu), Tijs Rademakers, Saeid Mirzaei
 */
public class CamelBehaviorCamelBodyImpl extends CamelBehavior {
	
	private static final long serialVersionUID = 1L;

	@Override
  protected void setPropertTargetVariable(ActivitiEndpoint endpoint) {
    toTargetType = TargetType.BODY;
  }
}

