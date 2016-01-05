package org.activiti.engine.runtime;

public class VariableInstance {
  protected String name;
  protected String description;
  protected String localizedName;
  protected String localizedDescription;
  protected Object value;
  
  public VariableInstance(String name, String description, String localizedName, String localizedDescription, Object value) {
    this.name = name;
    this.description = description;
    this.localizedName = localizedName;
    this.localizedDescription = localizedDescription;
    this.value = value;
  }
  
  /**
   * Name of the variable.
   */
  public String getName() {
    return name;
  }
  
  /**
   * Localized variable name.
   */
  public String getLocalizedName() {
    return localizedName;
  }
  
  /** 
   * Localized variable description.
   */
  public String getLocalizedDescription() {
    return localizedDescription;
  }
  
  /**
   * Description of the variable.
   */
  public String getDescription() {
    return description;
  }
  
  /**
   * Value of the variable
   */
  public Object getValue() {
    return value;
  }
}
