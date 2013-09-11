package org.activiti.spring.test.email;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.NoSuchProviderException;
import javax.mail.Provider;
import javax.mail.Provider.Type;
import javax.mail.Session;
import javax.naming.NamingException;

import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.spring.impl.test.SpringActivitiTestCase;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration("classpath:org/activiti/spring/test/email/jndiEmailConfiguaration-context.xml")
public class JndiEmailTest extends SpringActivitiTestCase {

  static Logger logger = Logger.getLogger(JndiEmailTest.class.getName());
  
  @BeforeClass
  public void setUp(){
      Properties props = new Properties();
      props.put("mail.transport.protocol", "smtp");
      props.put("mail.smtp.provider.class", MockEmailTransport.class.getName());
      props.put("mail.smtp.class", MockEmailTransport.class.getName());
      props.put("mail.smtp.provider.vendor", "test");
      props.put("mail.smtp.provider.version", "0.0.0");
      
      Provider provider = new Provider(Type.TRANSPORT, "smtp", MockEmailTransport.class.getName(), "test", "1.0");
      Session mailSession = Session.getDefaultInstance(props);
      SimpleNamingContextBuilder builder = null;
      try {
          mailSession.setProvider(provider);
          builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
          builder.bind("java:comp/env/Session",mailSession);
      } catch (NamingException e) {
          logger.error(e);
      } catch (NoSuchProviderException e) {
        logger.error(e);
      }
  }
  
  @Deployment(resources = {"org/activiti/spring/test/email/EmailTaskUsingJndi.bpmn20.xml"})
  public void testEmailUsingJndi() {
    Map<String, Object> variables = new HashMap<String, Object>();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("EmailJndiProcess", variables);
    assertEquals(0, runtimeService.createProcessInstanceQuery().processInstanceId(processInstance.getId()).count());
  }
}
