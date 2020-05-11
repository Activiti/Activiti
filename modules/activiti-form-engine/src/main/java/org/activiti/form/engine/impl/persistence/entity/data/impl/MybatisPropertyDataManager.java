package org.activiti.form.engine.impl.persistence.entity.data.impl;

import org.activiti.form.engine.FormEngineConfiguration;
import org.activiti.form.engine.impl.persistence.entity.PropertyEntity;
import org.activiti.form.engine.impl.persistence.entity.PropertyEntityImpl;
import org.activiti.form.engine.impl.persistence.entity.data.AbstractDataManager;
import org.activiti.form.engine.impl.persistence.entity.data.PropertyDataManager;

import java.util.List;

public class MybatisPropertyDataManager extends AbstractDataManager<PropertyEntity> implements PropertyDataManager {
  
  public MybatisPropertyDataManager(FormEngineConfiguration formEngineConfiguration) {
    super(formEngineConfiguration);
  }

  @Override
  public Class<? extends PropertyEntity> getManagedEntityClass() {
    return PropertyEntityImpl.class;
  }
  
  @Override
  public PropertyEntity create() {
    return new PropertyEntityImpl();
  }
  
  @Override
  @SuppressWarnings("unchecked")
  public List<PropertyEntity> findAll() {
    return getDbSqlSession().selectList("selectProperties");
  }
  
}
