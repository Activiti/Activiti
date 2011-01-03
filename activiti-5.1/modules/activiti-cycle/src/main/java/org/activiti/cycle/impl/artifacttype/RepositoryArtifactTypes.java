package org.activiti.cycle.impl.artifacttype;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * {@link CycleContextType#APPLICATION} scoped cycle component holding a list of
 * all availabe {@link RepositoryArtifactType}s
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class RepositoryArtifactTypes {

  private Set<RepositoryArtifactType> cachedSet;

  private void initSet() {
    Set<RepositoryArtifactType> set = new HashSet<RepositoryArtifactType>();
    Set<Class<RepositoryArtifactType>> implClasses = CycleComponentFactory.getAllImplementations(RepositoryArtifactType.class);
    for (Class<RepositoryArtifactType> class1 : implClasses) {
      set.add(CycleComponentFactory.getCycleComponentInstance(class1, RepositoryArtifactType.class));
    }
    cachedSet = Collections.unmodifiableSet(set);
  }

  public Set<RepositoryArtifactType> getAvailableRepositoryArtifactTypes() {
    if (cachedSet == null)
      synchronized (this) {
        if (cachedSet == null) {
          initSet();
        }
      }
    return cachedSet;
  }

}
