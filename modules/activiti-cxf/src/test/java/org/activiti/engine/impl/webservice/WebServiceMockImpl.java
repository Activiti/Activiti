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

import javax.jws.WebService;

import org.apache.cxf.common.i18n.UncheckedException;

/**
 * An implementation of a Counter WS
 *
 * @author Esteban Robles Luna
 */
@WebService(endpointInterface = "org.activiti.engine.impl.webservice.WebServiceMock", serviceName = "WebServiceMock")
public class WebServiceMockImpl implements WebServiceMock {

  protected int count;

  protected WebServiceDataStructure dataStructure = new WebServiceDataStructure();

  public WebServiceMockImpl() {
    this.count = -1;
    this.dataStructure = new WebServiceDataStructure();
  }

  /**
   * {@inheritDoc}
   */
  public int getCount() {
    return this.count;
  }

  /**
   * {@inheritDoc}
   */
  public void inc() throws MaxValueReachedFault {
    if (this.count == 123456) {
      throw new RuntimeException("A runtime exception not expected in the processing of the web-service");
    } else if (this.count != Integer.MAX_VALUE) {
      this.count++;
    } else  {
      throw new MaxValueReachedFault();
    }
  }

  /**
   * {@inheritDoc}
   */
  public void reset() {
    this.setTo(0);
  }

  /**
   * {@inheritDoc}
   */
  public void setTo(int value) {
    this.count = value;
  }

  /**
   * {@inheritDoc}
   */
  public String prettyPrintCount(String prefix, String suffix) {
    return prefix + this.getCount() + suffix;
  }

  /**
   * {@inheritDoc}
   */
  public void setDataStructure(String str, Date date) {
    this.dataStructure.eltString = str;
    this.dataStructure.eltDate = date;
  }

  /**
   * {@inheritDoc}
   */
  public WebServiceDataStructure getDataStructure() {
    return this.dataStructure;
  }

  /**
   * {@inheritDoc}
   */
  public String noNameResult(String prefix, String suffix) {
    return prefix + this.getCount() + suffix;
  }

  /**
   * {@inheritDoc}
   */
  public String reservedWordAsName(String prefix, String suffix) {
    return prefix + this.getCount() + suffix;
  }
}