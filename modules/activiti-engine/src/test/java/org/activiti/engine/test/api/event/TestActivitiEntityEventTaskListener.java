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
package org.activiti.engine.test.api.event;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.task.Task;

import java.util.ArrayList;
import java.util.List;

/**
 * Records a copy of the tasks involved in the events
 */
public class TestActivitiEntityEventTaskListener extends TestActivitiEntityEventListener {

	private List<Task> tasks;

	public TestActivitiEntityEventTaskListener(Class<?> entityClass) {
		super(entityClass);
		tasks = new ArrayList<Task>();
  	}

	@Override
	public void clearEventsReceived() {
		super.clearEventsReceived();
		tasks.clear();
	}
	
	@Override
	public void onEvent(ActivitiEvent event) {
		super.onEvent(event);
		if(event instanceof ActivitiEntityEvent && Task.class.isAssignableFrom(((ActivitiEntityEvent) event).getEntity().getClass())) {
			tasks.add(copy((Task) ((ActivitiEntityEvent) event).getEntity()));
		}
	}

	private Task copy(Task aTask)
	{
		TaskEntity ent = TaskEntity.create(aTask.getCreateTime());
		ent.setId(aTask.getId());
		ent.setName(aTask.getName());
		ent.setDescription(aTask.getDescription());
		ent.setOwner(aTask.getOwner());
		ent.setDueDateWithoutCascade(aTask.getDueDate());
		ent.setAssignee(aTask.getAssignee());
		ent.setPriority(aTask.getPriority());
		return ent;
	}

	public List<Task> getTasks()
	{
		return tasks;
	}
}
