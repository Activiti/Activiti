package org.activiti.engine.test.api.event;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;

public class TestHistoricProcessInstanceEventListener implements ActivitiEventListener {

	private List<ActivitiEvent> eventsReceived;
	private Class<?> entityClass;
	
	public TestHistoricProcessInstanceEventListener(Class<?> entityClass) {
		this.entityClass = entityClass;
		
		eventsReceived = new ArrayList<ActivitiEvent>();
	}

	public List<ActivitiEvent> getEventsReceived() {
		return eventsReceived;
	}
		
	public void clearEventsReceived() {
		eventsReceived.clear();
	}

	@Override
	public void onEvent(ActivitiEvent event) {
		if(event instanceof ActivitiEntityEvent && entityClass.isAssignableFrom(((ActivitiEntityEvent) event).getEntity().getClass())) {
			eventsReceived.add(event);
		}
	}

	@Override
	public boolean isFailOnException() {
		return false;
	}

}
