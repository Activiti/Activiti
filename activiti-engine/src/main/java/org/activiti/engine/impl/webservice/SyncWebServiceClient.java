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
package org.activiti.engine.impl.webservice;

import org.activiti.engine.api.internal.Internal;

import java.net.URL;
import java.util.concurrent.ConcurrentMap;

import javax.xml.namespace.QName;

/**
 * A dynamic web service client that allows to perform synchronous calls to a specific web service.
 * 
 * @deprecated due integrations will be delegated to extenal components and the use of the Connector interface
 */
@Deprecated
@Internal
public interface SyncWebServiceClient {

  /**
   * Synchronously invoke a web service method with some arguments.
   * 
   * @param methodName
   *          a not null method name
   * @param arguments
   *          a not null list of arguments
   * @param overridenEndpointAddresses 
   *          a not null map of overriden enpoint addresses. The key is the endpoint qualified name.          
   * @return the result of invoking the method of the web service
   */
  Object[] send(String methodName, Object[] arguments, ConcurrentMap<QName, URL> overridenEndpointAddresses) throws Exception;
}
