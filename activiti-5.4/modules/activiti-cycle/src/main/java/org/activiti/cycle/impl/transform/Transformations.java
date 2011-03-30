package org.activiti.cycle.impl.transform;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.activiti.cycle.Content;
import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.transform.ContentArtifactTypeTransformation;
import org.activiti.cycle.transform.ContentMimeTypeTransformation;
import org.activiti.cycle.transform.ContentTransformationException;

/**
 * {@link CycleContextType#APPLICATION}-scoped component managing
 * {@link ContentMimeTypeTransformation}s and
 * {@link ContentArtifactTypeTransformation}s.
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class Transformations {

  private Map<RepositoryArtifactType, Set<ContentArtifactTypeTransformation>> fromArtifactTypeTransformations;

  private Map<RepositoryArtifactType, Set<ContentArtifactTypeTransformation>> toArtifactTypeTransformations;

  private Map<MimeType, Set<ContentMimeTypeTransformation>> fromMimeTypeTransformations;

  private Map<MimeType, Set<ContentMimeTypeTransformation>> toMimeTypeTransformations;

  private void initMimeTypeTransformations() {
    if (fromMimeTypeTransformations == null || toMimeTypeTransformations == null) {
      synchronized (this) {
        if (fromMimeTypeTransformations != null && toMimeTypeTransformations != null) {
          return;
        }
        fromMimeTypeTransformations = new HashMap<MimeType, Set<ContentMimeTypeTransformation>>();
        toMimeTypeTransformations = new HashMap<MimeType, Set<ContentMimeTypeTransformation>>();

        Set<Class<ContentMimeTypeTransformation>> contentMimeTypeTransformations = CycleComponentFactory
                .getAllImplementations(ContentMimeTypeTransformation.class);

        for (Class<ContentMimeTypeTransformation> transformationClass : contentMimeTypeTransformations) {
          ContentMimeTypeTransformation transformation = CycleApplicationContext.get(transformationClass);
          MimeType sourceType = transformation.getSourceType();
          MimeType targetType = transformation.getTargetType();
          Set<ContentMimeTypeTransformation> sourceSet = fromMimeTypeTransformations.get(sourceType);
          if (sourceSet == null) {
            sourceSet = new HashSet<ContentMimeTypeTransformation>();
            fromMimeTypeTransformations.put(sourceType, sourceSet);
          }
          sourceSet.add(transformation);
          Set<ContentMimeTypeTransformation> targetSet = toMimeTypeTransformations.get(targetType);
          if (targetSet == null) {
            targetSet = new HashSet<ContentMimeTypeTransformation>();
            toMimeTypeTransformations.put(targetType, targetSet);
          }
          targetSet.add(transformation);
        }
      }
    }
  }

  private void initArtifactTypeTransformations() {
    if (fromArtifactTypeTransformations == null || toArtifactTypeTransformations == null) {
      synchronized (this) {
        if (fromMimeTypeTransformations != null && toArtifactTypeTransformations != null) {
          return;
        }
        fromArtifactTypeTransformations = new HashMap<RepositoryArtifactType, Set<ContentArtifactTypeTransformation>>();
        toArtifactTypeTransformations = new HashMap<RepositoryArtifactType, Set<ContentArtifactTypeTransformation>>();

        Set<Class<ContentArtifactTypeTransformation>> contentMimeTypeTransformations = CycleComponentFactory
                .getAllImplementations(ContentArtifactTypeTransformation.class);

        for (Class<ContentArtifactTypeTransformation> transformationClass : contentMimeTypeTransformations) {
          ContentArtifactTypeTransformation transformation = CycleApplicationContext.get(transformationClass);
          RepositoryArtifactType sourceType = transformation.getSourceType();
          RepositoryArtifactType targetType = transformation.getTargetType();
          Set<ContentArtifactTypeTransformation> sourceSet = fromArtifactTypeTransformations.get(sourceType);
          if (sourceSet == null) {
            sourceSet = new HashSet<ContentArtifactTypeTransformation>();
            fromArtifactTypeTransformations.put(sourceType, sourceSet);
          }
          sourceSet.add(transformation);
          Set<ContentArtifactTypeTransformation> targetSet = toArtifactTypeTransformations.get(targetType);
          if (targetSet == null) {
            targetSet = new HashSet<ContentArtifactTypeTransformation>();
            toArtifactTypeTransformations.put(targetType, targetSet);
          }
          targetSet.add(transformation);
        }
      }
    }
  }

  public Map<MimeType, Set<ContentMimeTypeTransformation>> getFromMimeTypeTransformations() {
    initMimeTypeTransformations();
    return fromMimeTypeTransformations;
  }

  public Map<MimeType, Set<ContentMimeTypeTransformation>> getToMimeTypeTransformations() {
    initMimeTypeTransformations();
    return toMimeTypeTransformations;
  }

  public Map<RepositoryArtifactType, Set<ContentArtifactTypeTransformation>> getFromArtifactTypeTransformations() {
    initArtifactTypeTransformations();
    return fromArtifactTypeTransformations;
  }

  public Map<RepositoryArtifactType, Set<ContentArtifactTypeTransformation>> getToArtifactTypeTransformations() {
    initArtifactTypeTransformations();
    return toArtifactTypeTransformations;
  }

  public Set<RepositoryArtifactType> getAvailableTransformations(RepositoryArtifactType type) {
    Set<RepositoryArtifactType> result = new HashSet<RepositoryArtifactType>();
    for (ContentArtifactTypeTransformation transformation : getFromArtifactTypeTransformations().get(type)) {
      result.add(transformation.getTargetType());
    }
    return result;
  }

  public Set<MimeType> getAvailableTransformations(MimeType mimeType) {
    Set<MimeType> result = new HashSet<MimeType>();
    for (ContentMimeTypeTransformation transformation : getFromMimeTypeTransformations().get(mimeType)) {
      result.add(transformation.getTargetType());
    }
    return result;
  }

  public Content transformContent(Content content, RepositoryArtifactType fromType, RepositoryArtifactType toType) throws ContentTransformationException {
    // select transformation:
    Set<ContentArtifactTypeTransformation> fromTransformations = getFromArtifactTypeTransformations().get(fromType);
    if (fromTransformations == null) {
      throw new ContentTransformationException("Cannot transform content of type '" + fromType + "' to type '" + toType
              + "', no appropriate transformation avaiable.");
    }
    ContentArtifactTypeTransformation transformation = null;
    for (ContentArtifactTypeTransformation contentArtifactTypeTransformation : fromTransformations) {
      if (contentArtifactTypeTransformation.getTargetType().equals(toType)) {
        transformation = contentArtifactTypeTransformation;
        break;
      }
    }
    if (transformation == null) {
      throw new ContentTransformationException("Cannot transform content of type '" + fromType + "' to type '" + toType
              + "', no appropriate transformation avaiable.");
    }

    return transformation.transformContent(content);
  }

  public Content transformContent(Content content, MimeType fromType, MimeType toType) throws ContentTransformationException {
    // select transformation:
    Set<ContentMimeTypeTransformation> fromTransformations = getFromMimeTypeTransformations().get(fromType);
    if (fromTransformations == null) {
      throw new ContentTransformationException("Cannot transform content of type '" + fromType + "' to type '" + toType
              + "', no appropriate transformation avaiable.");
    }
    ContentMimeTypeTransformation transformation = null;
    for (ContentMimeTypeTransformation contentMimeTypeTransformation : fromTransformations) {
      if (contentMimeTypeTransformation.getTargetType().equals(toType)) {
        transformation = contentMimeTypeTransformation;
        break;
      }
    }
    if (transformation == null) {
      throw new ContentTransformationException("Cannot transform content of type '" + fromType + "' to type '" + toType
              + "', no appropriate transformation avaiable.");
    }

    return transformation.transformContent(content);
  }

}
