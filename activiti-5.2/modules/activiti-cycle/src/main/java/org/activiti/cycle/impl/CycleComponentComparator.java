package org.activiti.cycle.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.annotations.ListAfterComponents;
import org.activiti.cycle.annotations.ListBeforeComponents;

/**
 * Cycle component comparator used to determine a sorting on cycle components.
 * 
 * @author daniel.meyer@camunda.com
 */
@SuppressWarnings("rawtypes")
public class CycleComponentComparator implements Comparator {

  private Class< ? > getComponentClass(Object o) {
    // TODO: naive: this does not work for decorated components.
    return o.getClass();
  }

  private List<String> getBeforeComponentNames(Class< ? > o) {
    List<String> componentNames = new ArrayList<String>();
    ListBeforeComponents listBeforeComponents = o.getAnnotation(ListBeforeComponents.class);
    if (listBeforeComponents != null) {
      componentNames.addAll(Arrays.asList(listBeforeComponents.value()));
      componentNames.addAll(Arrays.asList(listBeforeComponents.names()));
      Class[] classes = listBeforeComponents.classes();
      for (Class clazz : classes) {
        componentNames.add(clazz.getCanonicalName());
      }
    }
    return componentNames;
  }
  private List<String> getAfterComponentNames(Class< ? > o) {
    List<String> componentNames = new ArrayList<String>();
    ListBeforeComponents listAfterComponents = o.getAnnotation(ListBeforeComponents.class);
    if (listAfterComponents != null) {
      componentNames.addAll(Arrays.asList(listAfterComponents.value()));
      componentNames.addAll(Arrays.asList(listAfterComponents.names()));
      Class[] classes = listAfterComponents.classes();
      for (Class clazz : classes) {
        componentNames.add(clazz.getCanonicalName());
      }
    }
    return componentNames;
  }

  private boolean isAfterAll(Class< ? > componentClass) {
    ListAfterComponents annotation = componentClass.getAnnotation(ListAfterComponents.class);
    if (annotation != null) {
      if (annotation.value().length != 0) {
        return false;
      }
      if (annotation.names().length != 0) {
        return false;
      }
      if (annotation.classes().length != 0) {
        return false;
      }
      return true;
    }
    return false;

  }

  private boolean isBeforeAll(Class< ? > componentClass) {
    ListBeforeComponents annotation = componentClass.getAnnotation(ListBeforeComponents.class);
    if (annotation != null) {
      if (annotation.value().length != 0) {
        return false;
      }
      if (annotation.names().length != 0) {
        return false;
      }
      if (annotation.classes().length != 0) {
        return false;
      }
      return true;
    }
    return false;

  }

  public int compare(Object o1, Object o2) {
    if (o1 == o2 || o1.equals(o2)) {
      return 0;
    }
    Class< ? > o1ComponentClass = getComponentClass(o1);

    if (isBeforeAll(o1ComponentClass)) {
      return -1;
    }
    if (isAfterAll(o1ComponentClass)) {
      return 1;
    }
    Class< ? > o2ComponentClass = getComponentClass(o2);
    if (isBeforeAll(o2ComponentClass)) {
      return 1;
    }
    if (isAfterAll(o2ComponentClass)) {
      return -1;
    }

    String o1ComponentName = CycleComponentFactory.getComponentName(o1ComponentClass);
    List<String> o1BeforeComponentNames = getBeforeComponentNames(o1ComponentClass);
    List<String> o1AfterComponentNames = getAfterComponentNames(o1ComponentClass);
    String o2ComponentName = CycleComponentFactory.getComponentName(o2ComponentClass);
    List<String> o2BeforeComponentNames = getBeforeComponentNames(o2ComponentClass);
    List<String> o2AfterComponentNames = getAfterComponentNames(o2ComponentClass);

    if (o1ComponentName.equals(o2ComponentName)) {
      // TODO: what do we do with two instances of the same component?
      return -1;
    }
    if (o1BeforeComponentNames.contains(o2ComponentName) || o2AfterComponentNames.contains(o1ComponentName)) {
      return -1;
    }
    if (o2BeforeComponentNames.contains(o1ComponentName) || o1AfterComponentNames.contains(o2ComponentName)) {
      return 1;
    }
    return o1.toString().compareTo(o2.toString());
  }

}
