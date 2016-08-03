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

import java.util.Date;

import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;

/**
 * A simple WS for unit test purpose
 *
 * @author Esteban Robles Luna
 */
@WebService
public interface WebServiceMock {

  /**
   * Increase the counter in 1
   */
    void inc() throws MaxValueReachedFault;

  /**
   * Returns the current count
   *
   * @return the count
   */
  @WebResult(name="count")
  int getCount();

  /**
   * Resets the counter to 0
   */
  void reset();

  /**
   * Sets the counter to value
   *
   * @param value the value of the new counter
   */
  void setTo(@WebParam(name="value") int value);

  /**
   * Returns a formated string composed of prefix + current count + suffix
   *
   * @param prefix the prefix
   * @param suffix the suffix
   * @return the formated string
   */
  @WebResult(name="prettyPrint")
  String prettyPrintCount(@WebParam(name="prefix") String prefix, @WebParam(name="suffix") String suffix);

  /**
   * Sets the current data structure
   *
   * @param str
   *            the new string of data structure
   * @param date
   *            the new date of data structure
   */
    void setDataStructure(@WebParam(name = "eltStr") String str, @WebParam(name = "eltDate") Date date);
  
  /**
   * Returns the current data structure
   *
   * @return the current data structure
   */
  @WebResult(name="currentStructure")
  WebServiceDataStructure getDataStructure();

  @WebResult
  String noNameResult(@WebParam(name="prefix") String prefix, @WebParam(name="suffix") String suffix);

  @WebResult(name = "static")
  String reservedWordAsName(@WebParam(name="prefix") String prefix, @WebParam(name="suffix") String suffix);
}
