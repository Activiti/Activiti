package org.activiti.spring.test.components;

import org.activiti.spring.components.support.ProcessScopeBeanFactoryPostProcessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;

import java.io.Serializable;


@Scope(ProcessScopeBeanFactoryPostProcessor.PROCESS_SCOPE_NAME)
public class ScopedCustomer implements Serializable, InitializingBean{
	public ScopedCustomer() {
	}
	public ScopedCustomer(String name) {
		this.name = name;
	}

	private String name = Thread.currentThread().getId()+":" +System.currentTimeMillis();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void afterPropertiesSet() throws Exception {
	 System.out.println("starting ..." + this.name) ;
	}
}
