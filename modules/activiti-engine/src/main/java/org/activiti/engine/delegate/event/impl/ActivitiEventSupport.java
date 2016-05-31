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
package org.activiti.engine.delegate.event.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.ActivitiIllegalArgumentException;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that allows adding and removing event listeners and dispatching events
 * to the appropriate listeners.
 * 
 * @author Frederik Heremans
 */
public class ActivitiEventSupport {

	private static final Logger LOG = LoggerFactory.getLogger(ActivitiEventSupport.class);

	protected List<ActivitiEventListener> eventListeners;
	protected Map<ActivitiEventType, List<ActivitiEventListener>> typedListeners;

	public ActivitiEventSupport() {
		eventListeners = new CopyOnWriteArrayList<ActivitiEventListener>();
		typedListeners = new HashMap<ActivitiEventType, List<ActivitiEventListener>>();
	}

	public synchronized void addEventListener(ActivitiEventListener listenerToAdd) {
		if (listenerToAdd == null) {
			throw new ActivitiIllegalArgumentException("Listener cannot be null.");
		}
		if (!eventListeners.contains(listenerToAdd)) {
			eventListeners.add(listenerToAdd);
		}
	}

	public synchronized void addEventListener(ActivitiEventListener listenerToAdd, ActivitiEventType... types) {
		if (listenerToAdd == null) {
			throw new ActivitiIllegalArgumentException("Listener cannot be null.");
		}

		if (types == null || types.length == 0) {
			addEventListener(listenerToAdd);
		
		} else {
  		for (ActivitiEventType type : types) {
  			addTypedEventListener(listenerToAdd, type);
  		}
		}
	}

	public void removeEventListener(ActivitiEventListener listenerToRemove) {
		eventListeners.remove(listenerToRemove);

		for (List<ActivitiEventListener> listeners : typedListeners.values()) {
			listeners.remove(listenerToRemove);
		}
	}

	public void dispatchEvent(ActivitiEvent event) {
		if (event == null) {
			throw new ActivitiIllegalArgumentException("Event cannot be null.");
		}

		if (event.getType() == null) {
			throw new ActivitiIllegalArgumentException("Event type cannot be null.");
		}

		// Call global listeners
		if (!eventListeners.isEmpty()) {
			for (ActivitiEventListener listener : eventListeners) {
				dispatchEvent(event, listener);
			}
		}

		// Call typed listeners, if any
		List<ActivitiEventListener> typed = typedListeners.get(event.getType());
		if (typed != null && !typed.isEmpty()) {
			for (ActivitiEventListener listener : typed) {
				dispatchEvent(event, listener);
			}
		}
	}

	protected void dispatchEvent(ActivitiEvent event, ActivitiEventListener listener) {
		try {
			listener.onEvent(event);
		} catch (Throwable t) {
			if (listener.isFailOnException()) {
				throw new ActivitiException("Exception while executing event-listener", t);
			} else {
				// Ignore the exception and continue notifying remaining listeners. The
				// listener
				// explicitly states that the exception should not bubble up
				LOG.warn("Exception while executing event-listener, which was ignored", t);
			}
		}
	}

	protected synchronized void addTypedEventListener(ActivitiEventListener listener, ActivitiEventType type) {
		List<ActivitiEventListener> listeners = typedListeners.get(type);
		if (listeners == null) {
			// Add an empty list of listeners for this type
			listeners = new CopyOnWriteArrayList<ActivitiEventListener>();
			typedListeners.put(type, listeners);
		}

		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
}
