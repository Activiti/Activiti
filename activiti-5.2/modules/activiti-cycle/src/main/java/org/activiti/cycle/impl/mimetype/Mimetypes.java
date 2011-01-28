package org.activiti.cycle.impl.mimetype;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;

/**
 * {@link CycleContextType#APPLICATION} scoped cycle component holding a list of
 * all availabe {@link MimeType}s
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class Mimetypes {

  private Set<MimeType> cachedSet;

  private HashMap<String, MimeType> extensionsMap;

  private void initSet() {
    Set<MimeType> set = new HashSet<MimeType>();
    Set<Class<MimeType>> implClasses = CycleComponentFactory.getAllImplementations(MimeType.class);
    for (Class<MimeType> class1 : implClasses) {
      set.add(CycleComponentFactory.getCycleComponentInstance(class1, MimeType.class));
    }
    cachedSet = Collections.unmodifiableSet(set);
  }

  public Set<MimeType> getAvailableMimetypes() {
    if (cachedSet == null)
      synchronized (this) {
        if (cachedSet == null) {
          initSet();
        }
      }
    return cachedSet;
  }

  /**
   * @return a {@link MimeType} corresponding to the provided filename. Returns
   *         {@link UnknownMimeType} if no {@link MimeType} can be found for the
   *         provided extension.
   */
  public MimeType getTypeForFilename(String filename) {
    buildExtensionsMap();
    MimeType type = extensionsMap.get(filename);
    if (type == null) {
      return new UnknownMimeType();
    }
    return type;
  }

  private void buildExtensionsMap() {
    if (extensionsMap == null) {
      synchronized (this) {
        if (extensionsMap != null) {
          return;
        }
        extensionsMap = new HashMap<String, MimeType>();
        for (MimeType type : getAvailableMimetypes()) {
          for (String extension : type.getCommonFileExtensions()) {
            extensionsMap.put(extension, type);
          }
        }
      }
    }
  }

}
