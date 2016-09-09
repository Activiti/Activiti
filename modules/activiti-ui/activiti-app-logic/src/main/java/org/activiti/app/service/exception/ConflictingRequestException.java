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
package org.activiti.app.service.exception;

import java.util.Map;



/**
 * @author Frederik Heremans
 * @author Joram Barrez
 */
public class ConflictingRequestException extends BaseModelerRestException {

	private static final long serialVersionUID = 1L;
	
	public ConflictingRequestException(String s) {
		super(s);
	}
	
	public ConflictingRequestException(String message, String messageKey) {
	    this(message);
	    setMessageKey(messageKey);
	}
	
	public ConflictingRequestException(String message, String messageKey, Map<String, Object> customData) {
	    this(message, messageKey);
	    this.customData = customData;
	}

}