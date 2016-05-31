package org.activiti.engine.runtime;

/*
 * Represents a modeled DataObject.
 */
public interface DataObject {
  
  /**
   * Name of the DataObject.
   */
  String getName();
  
  /**
   * Localized Name of the DataObject.
   */
  String getLocalizedName();
  
  /**
   * Description of the DataObject.
   */
  String getDescription();

  /**
   * Value of the DataObject.
   */
  Object getValue();

  /**
   * Type of the DataObject.
   */
  String getType();  
}
