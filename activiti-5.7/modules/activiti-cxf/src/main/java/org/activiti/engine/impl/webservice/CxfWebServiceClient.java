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

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.jaxws.endpoint.dynamic.JaxWsDynamicClientFactory;


/**
 * A CXF's synchronous web service client
 * 
 * @author Esteban Robles Luna
 */
public class CxfWebServiceClient implements SyncWebServiceClient {

  protected Client client;
  
  public CxfWebServiceClient(String wsdl) {
    JaxWsDynamicClientFactory dcf = JaxWsDynamicClientFactory.newInstance();
    this.client = dcf.createClient(wsdl);
  }
  
  /**
   * {@inheritDoc}}
   */
  public Object[] send(String methodName, Object[] arguments) throws Exception {
    return client.invoke(methodName, arguments);
  }
}
