package org.activiti.cycle.impl.repositoryartifacttype;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.mimetype.Mimetypes;

/**
 * {@link CycleContextType#APPLICATION} scoped cycle component holding a list of
 * all availabe {@link RepositoryArtifactType}s
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class RepositoryArtifactTypes {

  private Set<RepositoryArtifactType> cachedSet;

  private Map<String, RepositoryArtifactType> extensionsMap;

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

  /**
   * Method for retrieving the best match of a {@link RepositoryArtifactType} to
   * a provided filename. Returns a {@link BasicRepositoryArtifactType} if no
   * "special" {@link RepositoryArtifactType} exists
   */
  public RepositoryArtifactType getTypeForFilename(String filename) {
    buildExtensionsMap();
    RepositoryArtifactType type = extensionsMap.get(filename);
    if (type != null) {
      return type;
    }
    // if we find no RepositoryArtifactType, look for a MimeType:
    Mimetypes mimetypes = CycleApplicationContext.get(Mimetypes.class);
    MimeType mimeType = mimetypes.getTypeForFilename(filename);
    return new BasicRepositoryArtifactType(mimeType);
  }

  private void buildExtensionsMap() {
    if (extensionsMap == null) {
      synchronized (this) {
        if (extensionsMap != null) {
          return;
        }
        extensionsMap = new HashMap<String, RepositoryArtifactType>();
        for (RepositoryArtifactType type : getAvailableRepositoryArtifactTypes()) {
          String[] fileExtensions = type.getCommonFileExtensions();
          if(fileExtensions == null){
            continue;
          }
          for (String extension : type.getCommonFileExtensions()) {
            extensionsMap.put(extension, type);
          }
        }
      }
    }
  }

}
