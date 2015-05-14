package org.activiti.osgi.blueprint;

import java.beans.FeatureDescriptor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.activiti.engine.delegate.JavaDelegate;
import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELResolver;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @see org.activiti.spring.ApplicationContextElResolver
 */
public class BlueprintELResolver extends ELResolver {
	
  private static final Logger LOGGER = LoggerFactory.getLogger(BlueprintELResolver.class);
	private Map<String, JavaDelegate> delegateMap = new HashMap<String, JavaDelegate>();
  private Map<String, ActivityBehavior> activityBehaviourMap = new HashMap<String, ActivityBehavior>();

  public Object getValue(ELContext context, Object base, Object property) {
    if (base == null) {
      // according to javadoc, can only be a String
      String key = (String) property;
      for (String name : delegateMap.keySet()) {
        if (name.equalsIgnoreCase(key)) {
          context.setPropertyResolved(true);
          return delegateMap.get(name);
        }
      }
      for (String name : activityBehaviourMap.keySet()) {
        if (name.equalsIgnoreCase(key)) {
          context.setPropertyResolved(true);
          return activityBehaviourMap.get(name);
        }
      }
    }

    return null;
	}
	
	@SuppressWarnings("rawtypes")
	public void bindService(JavaDelegate delegate, Map props) {
    String name = (String) props.get("osgi.service.blueprint.compname");
    delegateMap.put(name, delegate);
    LOGGER.info("added Activiti service to delegate cache {}", name);
	}

	@SuppressWarnings("rawtypes")
  public void unbindService(JavaDelegate delegate, Map props) {
		String name = (String) props.get("osgi.service.blueprint.compname");
    if(delegateMap.containsKey(name)) {
    	delegateMap.remove(name);
    }
    LOGGER.info("removed Activiti service from delegate cache {}", name);
	}

	@SuppressWarnings("rawtypes")
	public void bindActivityBehaviourService(ActivityBehavior delegate, Map props) {
    String name = (String) props.get("osgi.service.blueprint.compname");
    activityBehaviourMap.put(name, delegate);
    LOGGER.info("added Activiti service to activity behaviour cache {}", name);
  }

  @SuppressWarnings("rawtypes")
  public void unbindActivityBehaviourService(ActivityBehavior delegate, Map props) {
    String name = (String) props.get("osgi.service.blueprint.compname");
    if (activityBehaviourMap.containsKey(name)) {
      activityBehaviourMap.remove(name);
    }
    LOGGER.info("removed Activiti service from activity behaviour cache {}", name);
  }

	public boolean isReadOnly(ELContext context, Object base, Object property) {
		return true;
	}

	public void setValue(ELContext context, Object base, Object property, Object value) {
	}

	public Class<?> getCommonPropertyType(ELContext context, Object arg) {
		return Object.class;
	}

	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object arg) {
		return null;
	}

	public Class<?> getType(ELContext context, Object arg1, Object arg2) {
		return Object.class;
	}
}
