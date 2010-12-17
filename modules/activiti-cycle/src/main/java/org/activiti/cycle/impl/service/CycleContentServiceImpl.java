package org.activiti.cycle.impl.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.impl.connector.signavio.provider.AbstractPngProvider;
import org.activiti.cycle.impl.mimetype.Mimetypes;
import org.activiti.cycle.impl.repositoryartifacttype.RepositoryArtifactTypes;
import org.activiti.cycle.impl.representation.ContentRepresentations;
import org.activiti.cycle.impl.transform.Transformations;
import org.activiti.cycle.service.CycleContentService;
import org.activiti.cycle.transform.ContentTransformationException;

/**
 * Default {@link CycleContentService} implementation
 * 
 * @author daniel.meyer@camunda.com
 */
public class CycleContentServiceImpl implements CycleContentService {

  public Set<MimeType> getAvailableMimeTypes() {
    return CycleApplicationContext.get(Mimetypes.class).getAvailableMimetypes();
  }

  public Set<RepositoryArtifactType> getAvailableArtifactTypes() {
    return CycleApplicationContext.get(RepositoryArtifactTypes.class).getAvailableRepositoryArtifactTypes();
  }

  public Set<RepositoryArtifactType> getAvailableTransformations(RepositoryArtifactType type) {
    return CycleApplicationContext.get(Transformations.class).getAvailableTransformations(type);
  }

  public Set<MimeType> getAvailableTransformations(MimeType mimeType) {
    return CycleApplicationContext.get(Transformations.class).getAvailableTransformations(mimeType);
  }

  public Content transformContent(Content content, RepositoryArtifactType fromType, RepositoryArtifactType toType) throws ContentTransformationException {
    return CycleApplicationContext.get(Transformations.class).transformContent(content, fromType, toType);
  }

  public Content transformContent(Content content, MimeType fromType, MimeType toType) throws ContentTransformationException {
    return CycleApplicationContext.get(Transformations.class).transformContent(content, fromType, toType);
  }

  public List<ContentRepresentation> getcontentRepresentations(RepositoryArtifact artifact) {
    RepositoryArtifactType type = artifact.getArtifactType();
    Set<ContentRepresentation> representations = CycleApplicationContext.get(ContentRepresentations.class).getContentRepresentations(type);
    removeExcludedContentRepresentations(representations);
    List<ContentRepresentation> sortedList = new ArrayList<ContentRepresentation>(representations);
    sortContentReprsentations(sortedList);
    return sortedList;
  }

  private void sortContentReprsentations(List<ContentRepresentation> sortedList) {
    // TODO: sort contentRepresentations according to name AND annotations
    // TODO: move to better suited place
    // for the moment: sort alphabetically and make sure that "PNG" is the first
    // tab:
    Collections.sort(sortedList, new Comparator<ContentRepresentation>() {
      public int compare(ContentRepresentation o1, ContentRepresentation o2) {
        if (o1.equals(o2)) {
          return 0;
        }
        if (AbstractPngProvider.class.isAssignableFrom(o1.getClass())) {
          return -1;
        }
        if (AbstractPngProvider.class.isAssignableFrom(o2.getClass())) {
          return 1;
        }
        return o1.getId().compareTo(o2.getId());
      }
    });
  }

  private void removeExcludedContentRepresentations(Set represenations) {
    CycleComponentFactory.removeExcludedComponents(represenations);
  }

  public ContentRepresentation getContentRepresentation(RepositoryArtifact artifact, String contentRepresentationId) {
    List<ContentRepresentation> representations = getcontentRepresentations(artifact);
    for (ContentRepresentation contentRepresentation : representations) {
      if (!contentRepresentation.getId().equals(contentRepresentationId)) {
        continue;
      }
      return contentRepresentation;
    }
    return null;
  }

}
