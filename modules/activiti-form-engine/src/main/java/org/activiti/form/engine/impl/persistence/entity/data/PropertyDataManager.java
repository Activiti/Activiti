package org.activiti.form.engine.impl.persistence.entity.data;

import org.activiti.form.engine.impl.persistence.entity.PropertyEntity;

import java.util.List;

public interface PropertyDataManager extends DataManager<PropertyEntity> {
  
  List<PropertyEntity> findAll();

}