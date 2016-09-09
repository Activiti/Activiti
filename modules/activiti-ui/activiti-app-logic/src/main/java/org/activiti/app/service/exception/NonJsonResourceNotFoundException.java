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


/**
 * This is needed for when the REST resource is stated to return for example image/png, but needs to throw a 404
 * In this case, the default ExceptionHandlerAdvice will create a message for the 404. But that will lead
 * to a Spring exception and the end result is that it will be transmitted as a 500.
 * 
 * @author jbarrez
 */
public class NonJsonResourceNotFoundException extends BaseModelerRestException {

	private static final long serialVersionUID = 1L;
	
	public NonJsonResourceNotFoundException() {
	}

}