package org.activiti.cycle.impl.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.impl.CycleComponentComparator;
import org.activiti.cycle.impl.artifacttype.RepositoryArtifactTypes;
import org.activiti.cycle.impl.mimetype.Mimetypes;
import org.activiti.cycle.impl.representation.ContentRepresentations;
import org.activiti.cycle.impl.transform.Transformations;
import org.activiti.cycle.service.CycleContentService;
import org.activiti.cycle.transform.ContentTransformationException;

/**
 * Default {@link CycleContentService} implementation
 * 
 * @author daniel.meyer@camunda.com
 */
@SuppressWarnings("unchecked")
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
    Collections.sort(sortedList, new CycleComponentComparator());
  }

  private void removeExcludedContentRepresentations(Set<?> represenations) {
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
