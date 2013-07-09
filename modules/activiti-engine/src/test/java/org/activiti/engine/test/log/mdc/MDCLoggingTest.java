package org.activiti.engine.test.log.mdc;

import static org.mockito.Mockito.mock;

import org.apache.log4j.Appender;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.Log4jLoggerAdapter;


@RunWith(PowerMockRunner.class)
@PrepareForTest( { LoggerFactory.class , Log4jLoggerAdapter.class})
public class MDCLoggingTest {
	

	
	@Test
	public   void tetBasic() {
		Assert.assertEquals("1", "1");
		PowerMockito.mockStatic(LoggerFactory.class);
		ArgumentCaptor<org.apache.log4j.Logger> captor = ArgumentCaptor.forClass(org.apache.log4j.Logger.class);
		
	
	     Log4jLoggerAdapter logger = (Log4jLoggerAdapter) LoggerFactory.getLogger(String.class);
	     
	     PowerMockito.verifyStatic();
	     LoggerFactory.getLogger(String.class);
	     
	     System.out.println(captor.getValue());
	    
	      logger.debug("test");
//	      LoggerFactory mockedLoggerFactory =  PowerMockito.mockStatic(LoggerFactory.class);
	     
	     
	   //  verify(mock).doSomething(argument.capture());
	     
	    // assertEquals("John", argument.getValue().getName());
	//     PowerMockito.verifyStatic(LoggerFactory.class, Mockito.times(1))
	 //    System.out.println(argument.getValue());

	     
	     
	     final Appender mockAppender = mock(Appender.class);
	     
	     
	     
	     logger.error("Hello");
	}
	

}
