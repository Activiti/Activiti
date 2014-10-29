package org.activiti.management.jmx;



import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBean;

public interface ManagementMBeanAssembler {

    ModelMBean assemble(MBeanServer mBeanServer, Object obj, ObjectName name) throws JMException;

}
