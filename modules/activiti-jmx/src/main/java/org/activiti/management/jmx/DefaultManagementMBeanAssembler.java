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
package org.activiti.management.jmx;

import javax.management.JMException;
import javax.management.ObjectName;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.RequiredModelMBean;

import org.activiti.management.jmx.annotations.NotificationSenderAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Saeid Mirzaei
 */
public class DefaultManagementMBeanAssembler implements ManagementMBeanAssembler {

  private static final Logger LOG = LoggerFactory.getLogger(DefaultManagementMBeanAssembler.class);
  
  protected final MBeanInfoAssembler assembler;

  public DefaultManagementMBeanAssembler() {
    this.assembler = new MBeanInfoAssembler();
  }

  public ModelMBean assemble(Object obj, ObjectName name) throws JMException {
    ModelMBeanInfo mbi = null;

    // use the default provided mbean which has been annotated with JMX
    // annotations
    LOG.trace("Assembling MBeanInfo for: {} from @ManagedResource object: {}", name, obj);
    mbi = assembler.getMBeanInfo(obj, null, name.toString());

    if (mbi == null) {
      return null;
    }

    RequiredModelMBean mbean = new RequiredModelMBean(mbi);

    try {
      mbean.setManagedResource(obj, "ObjectReference");
    } catch (InvalidTargetObjectTypeException e) {
      throw new JMException(e.getMessage());
    }

    // Allows the managed object to send notifications
    if (obj instanceof NotificationSenderAware) {
      ((NotificationSenderAware) obj).setNotificationSender(new NotificationSenderAdapter(mbean));
    }

    return mbean;
  }

}
