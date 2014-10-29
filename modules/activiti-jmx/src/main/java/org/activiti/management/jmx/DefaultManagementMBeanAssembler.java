package org.activiti.management.jmx;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.InvalidTargetObjectTypeException;
import javax.management.modelmbean.ModelMBean;
import javax.management.modelmbean.ModelMBeanInfo;
import javax.management.modelmbean.RequiredModelMBean;

import org.activiti.management.jmx.annotations.NotificationSenderAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DefaultManagementMBeanAssembler implements ManagementMBeanAssembler {
  private static final Logger LOG = LoggerFactory.getLogger(DefaultManagementAgent.class);
  protected final MBeanInfoAssembler assembler;

  public DefaultManagementMBeanAssembler() {
      this.assembler = new MBeanInfoAssembler();
  }

  public ModelMBean assemble(MBeanServer mBeanServer, Object obj, ObjectName name) throws JMException {
      ModelMBeanInfo mbi = null;


      // use the default provided mbean which has been annotated with JMX annotations
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
          ((NotificationSenderAware)obj).setNotificationSender(new NotificationSenderAdapter(mbean));
      }

      return mbean;
  }

}
