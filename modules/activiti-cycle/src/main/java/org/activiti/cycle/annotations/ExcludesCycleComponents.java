package org.activiti.cycle.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.action.Action;
import org.activiti.cycle.action.DownloadContentAction;
import org.activiti.cycle.action.ParameterizedAction;

/**
 * {@link CycleComponent}s can be annotated with the {@link ExcludesCycleComponents}
 * -Annotation. The {@link ExcludesCycleComponents}-Annotaion allows to disable other
 * components.
 * 
 * <p/>
 * <strong>EXAMPLE:</strong> An {@link Action} for a special
 * {@link RepositoryArtifactType} excludes another {@link Action} which works on
 * a more general {@link RepositoryArtifactType}.
 * <p />
 * 
 * <strong>NOTE:</strong> at the moment, this annotation is only applicable to
 * some component types:
 * <ul>
 * <li>{@link Action} (which subsumes {@link ParameterizedAction},
 * {@link DownloadContentAction} etc...)</li>
 * <li> {@link ContentRepresentation}</li>
 * </ul>
 * On other component types (i.e. connectors) it is ignored.
 * 
 * @author daniel.meyer@camunda.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface ExcludesCycleComponents {

  /**
   * The name of the component to exclude.
   */
  String[] value();

}
