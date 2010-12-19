package org.activiti.cycle.impl.action;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.action.Action;
import org.activiti.cycle.action.CreateUrlAction;
import org.activiti.cycle.action.ParameterizedAction;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;

/**
 * Cycle Component for managing {@link Action}s as provided by the plugins
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class Actions {

  boolean initialized = false;

  private Set<ParameterizedAction> globalParameterizedActions = new HashSet<ParameterizedAction>();
  private Set<CreateUrlAction> globalCreateUrlActions = new HashSet<CreateUrlAction>();
  private Map<RepositoryArtifactType, Set<ParameterizedAction>> parameterizedActionsMap = new HashMap<RepositoryArtifactType, Set<ParameterizedAction>>();
  private Map<RepositoryArtifactType, Set<CreateUrlAction>> createUrlActionsMap = new HashMap<RepositoryArtifactType, Set<CreateUrlAction>>();

  public Set<ParameterizedAction> getParameterizedActions(RepositoryArtifactType forType) {
    ensureMapInitialized();
    Set<ParameterizedAction> resultSet = new HashSet<ParameterizedAction>(globalParameterizedActions);
    Set<ParameterizedAction> actionsForThisType = parameterizedActionsMap.get(forType);
    if (actionsForThisType != null) {
      resultSet.addAll(actionsForThisType);
    }
    return resultSet;
  }

  public Set<CreateUrlAction> getCreateUrlActions(RepositoryArtifactType forType) {
    ensureMapInitialized();
    Set<CreateUrlAction> resultSet = new HashSet<CreateUrlAction>(globalCreateUrlActions);
    Set<CreateUrlAction> actionsForThisType = createUrlActionsMap.get(forType);
    if (actionsForThisType != null) {
      resultSet.addAll(actionsForThisType);
    }
    return resultSet;
  }

  private void ensureMapInitialized() {
    if (!initialized) {
      synchronized (this) {
        if (initialized) {
          return;
        }
        parameterizedActionsMap = new HashMap<RepositoryArtifactType, Set<ParameterizedAction>>();
        createUrlActionsMap = new HashMap<RepositoryArtifactType, Set<CreateUrlAction>>();
        loadMap(parameterizedActionsMap, globalParameterizedActions, ParameterizedAction.class);
        loadMap(createUrlActionsMap, globalCreateUrlActions, CreateUrlAction.class);
        initialized = true;
      }
    }
  }

  private <T extends Action> void loadMap(Map<RepositoryArtifactType, Set<T>> map, Set<T> globalActionsSet, Class<T> clazz) {
    Set<Class<T>> actionClasses = CycleComponentFactory.getAllImplementations(clazz);
    for (Class<T> class1 : actionClasses) {
      T actionObject = CycleApplicationContext.get(class1);
      Set<RepositoryArtifactType> artifactTypes = actionObject.getArtifactTypes();
      if (artifactTypes == null || artifactTypes.size() == 0) {
        globalActionsSet.add(actionObject);
        continue;
      }
      for (RepositoryArtifactType repositoryArtifactType : artifactTypes) {
        Set<T> currentSetforThisType = map.get(repositoryArtifactType);
        if (currentSetforThisType == null) {
          currentSetforThisType = new HashSet<T>();
          map.put(repositoryArtifactType, currentSetforThisType);
        }
        currentSetforThisType.add(actionObject);
      }
    }
  }

}
