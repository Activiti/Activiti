package org.activiti5.engine.impl.persistence;

import org.activiti5.engine.impl.history.DefaultHistoryManager;
import org.activiti5.engine.impl.history.HistoryManager;
import org.activiti5.engine.impl.interceptor.Session;
import org.activiti5.engine.impl.interceptor.SessionFactory;

/**
 * @author Joram Barrez
 */
public class DefaultHistoryManagerSessionFactory implements SessionFactory {
	
	public java.lang.Class<?> getSessionType() {
		return HistoryManager.class; 
	}
	
	@Override
	public Session openSession() {
		return new DefaultHistoryManager();
	}

}
