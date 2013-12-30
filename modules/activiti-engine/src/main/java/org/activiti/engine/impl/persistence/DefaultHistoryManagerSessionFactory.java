package org.activiti.engine.impl.persistence;

import org.activiti.engine.impl.history.DefaultHistoryManager;
import org.activiti.engine.impl.history.HistoryManager;
import org.activiti.engine.impl.interceptor.Session;
import org.activiti.engine.impl.interceptor.SessionFactory;

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
