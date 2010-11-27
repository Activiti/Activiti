package org.activiti.cycle.impl.plugin;


public class DefinitionEntry<T> {
  
  private String key;

  private T value;
  
  public DefinitionEntry(String key, T entryClass) {
    this.key = key;
    this.value = entryClass;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public T getValue() {
    return value;
  }

  public void setValue(T value) {
    this.value = value;
  }

}
