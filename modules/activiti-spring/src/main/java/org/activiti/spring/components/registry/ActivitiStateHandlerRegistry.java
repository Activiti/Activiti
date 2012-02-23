/*
 * Copyright 2010 the original author or authors
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.activiti.spring.components.registry;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.impl.bpmn.behavior.ReceiveTaskActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;


/**
 * this class records and manages all known {@link org.activiti.engine.annotations.State} - responding
 * beans in the JVM. It <em>should</em> have metadata on all methods, and what
 * those methods expect from a given invocation (ie: which process, which process variables).
 *
 * @author Josh Long
 * @since 1.0
 */
public class ActivitiStateHandlerRegistry extends ReceiveTaskActivityBehavior implements BeanFactoryAware, BeanNameAware, ActivityBehavior, InitializingBean {

	private Logger logger = Logger.getLogger(getClass().getName());

	private String beanName;

	private BeanFactory beanFactory;

	private volatile ConcurrentHashMap<String, ActivitiStateHandlerRegistration> registrations = new ConcurrentHashMap<String, ActivitiStateHandlerRegistration>();

	private ProcessEngine processEngine;

	public void setProcessEngine(ProcessEngine processEngine) {
		this.processEngine = processEngine;
	}

	@Override
	public void execute(ActivityExecution execution) throws Exception {

	}

	@Override
	public void signal(ActivityExecution execution, String signalName, Object data) throws Exception {
		leave(execution);
	}

	protected String registrationKey(String stateName, String processName) {
		return (org.apache.commons.lang.StringUtils.defaultString(processName) +
				":" + org.apache.commons.lang.StringUtils.defaultString(stateName)).toLowerCase();
	}

	/**
	 * used at runtime to register state handlers as they are registered with the spring context
	 *
	 * @param registration the {@link org.activiti.spring.components.registry.ActivitiStateHandlerRegistration}
	 */
	public void registerActivitiStateHandler(
			ActivitiStateHandlerRegistration registration) {
		String regKey = registrationKey(registration.getProcessName(),
				registration.getStateName());
		this.registrations.put(regKey, registration);
	}

	/**
	 * this is responsible for looking up components in the registry and returning the appropriate handler based
	 * on specificity of the {@link org.activiti.spring.components.registry.ActivitiStateHandlerRegistration}
	 *
	 * @param processName the process name to look for (optional)
	 * @param stateName	 the state name to look for (not optional)
	 * @return all matching options
	 */
	public Collection<ActivitiStateHandlerRegistration> findRegistrationsForProcessAndState(
			String processName, String stateName) {
		Collection<ActivitiStateHandlerRegistration> registrationCollection = new ArrayList<ActivitiStateHandlerRegistration>();
		String regKeyFull = registrationKey(processName, stateName);
		String regKeyWithJustState = registrationKey(null, stateName);

		for (String k : this.registrations.keySet())
			if (k.contains(regKeyFull)) {
				registrationCollection.add(this.registrations.get(k));
			}

		if (registrationCollection.size() == 0) {
			for (String k : this.registrations.keySet())
				if (k.contains(regKeyWithJustState)) {
					registrationCollection.add(this.registrations.get(k));
				}
		}

		return registrationCollection;
	}

	/**
	 * this scours the registry looking for candidate registrations that match a given process name and/ or state nanme
	 *
	 * @param processName the name of the process
	 * @param stateName	 the name of the state
	 * @return an unambiguous {@link org.activiti.spring.components.registry.ActivitiStateHandlerRegistry} or null
	 */
	public ActivitiStateHandlerRegistration findRegistrationForProcessAndState(String processName, String stateName) {

		ActivitiStateHandlerRegistration r = null;

		String key = registrationKey(processName, stateName);

		Collection<ActivitiStateHandlerRegistration> rs = this.findRegistrationsForProcessAndState(
				processName, stateName);

		for (ActivitiStateHandlerRegistration sr : rs) {
			String kName = registrationKey(sr.getProcessName(), sr.getStateName());
			if (key.equalsIgnoreCase(kName)) {
				r = sr;
				break;
			}
		}

		for (ActivitiStateHandlerRegistration sr : rs) {
			String kName = registrationKey(null, sr.getStateName());
			if (key.equalsIgnoreCase(kName)) {
				r = sr;
				break;
			}
		}

		if ((r == null) && (rs.size() > 0)) {
			r = rs.iterator().next();
		}

		return r;
	}

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	public void setBeanName(String name) {
		this.beanName = name;
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(this.processEngine, "the 'processEngine' can't be null");
		logger.info( "this bean contains a processEngine reference. "+ this.processEngine);
		logger.info("starting " + getClass().getName());
	}
}
