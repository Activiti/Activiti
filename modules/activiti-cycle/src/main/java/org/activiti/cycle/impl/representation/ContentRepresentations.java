package org.activiti.cycle.impl.representation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;

/**
 * {@link CycleComponent} for managing content representations.
 * 
 * @author meyerd
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class ContentRepresentations {

  private Map<RepositoryArtifactType, Set<ContentRepresentation>> contentRepMap;

  private void ensureMapLoaded() {
    if (contentRepMap != null) {
      return;
    }
    synchronized (this) {
      if (contentRepMap != null) {
        return;
      }
      contentRepMap = new HashMap<RepositoryArtifactType, Set<ContentRepresentation>>();
      Set<Class<ContentRepresentation>> contentRepresentationClasses = CycleComponentFactory.getAllImplementations(ContentRepresentation.class);
      for (Class<ContentRepresentation> clazz : contentRepresentationClasses) {
        ContentRepresentation representation = CycleApplicationContext.get(clazz);
        RepositoryArtifactType type = representation.getRepositoryArtifactType();
        Set<ContentRepresentation> representationsForThisType = contentRepMap.get(type);
        if (representationsForThisType == null) {
          representationsForThisType = new HashSet<ContentRepresentation>();
          contentRepMap.put(type, representationsForThisType);
        }
        representationsForThisType.add(representation);
      }
    }
  }

  public Set<ContentRepresentation> getContentRepresentations(RepositoryArtifactType type) {
    ensureMapLoaded();
    Set<ContentRepresentation> representations = new HashSet<ContentRepresentation>();
    Set<ContentRepresentation> availableRepresenatioRepresentations = contentRepMap.get(type);
    if (availableRepresenatioRepresentations != null) {
      representations.addAll(availableRepresenatioRepresentations);
    }
    return representations;
  }
}
