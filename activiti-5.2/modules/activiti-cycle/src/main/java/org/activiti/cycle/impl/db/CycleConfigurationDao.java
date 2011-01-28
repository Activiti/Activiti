package org.activiti.cycle.impl.db;

import java.util.List;

import org.activiti.cycle.impl.db.entity.CycleConfigEntity;

/**
 * Dao for managing {@link CycleConfigEntity}-Objects
 * 
 * @author daniel.meyer@camunda.com
 */
public interface CycleConfigurationDao {

  public void saveCycleConfig(CycleConfigEntity entity);

  public CycleConfigEntity selectCycleConfigByGroupAndKey(String group, String key);

  public List<CycleConfigEntity> selectCycleConfigByGroup(String group);

  public List<CycleConfigEntity> selectCycleConfigById(String id);

}
