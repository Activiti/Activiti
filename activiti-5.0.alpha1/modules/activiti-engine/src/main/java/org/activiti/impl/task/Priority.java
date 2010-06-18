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
package org.activiti.impl.task;

/**
 * @author Joram Barrez
 */
public final class Priority {
	
	// No need to instantiate
	private Priority() { }
	
	public final static int HIGHEST = 0; // WS-HT spec
	public final static int HIGH = 1;
	public final static int NORMAL = 2;
	public final static int LOWER = 3;
	public final static int HIGHER = 4;
	
}
