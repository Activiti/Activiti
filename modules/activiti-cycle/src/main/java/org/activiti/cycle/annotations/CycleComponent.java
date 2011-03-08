package org.activiti.cycle.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.CycleComponentFactory;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RepositoryArtifactType;
import org.activiti.cycle.RepositoryConnector;
import org.activiti.cycle.action.Action;
import org.activiti.cycle.action.CreateUrlAction;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.context.CycleRequestContext;
import org.activiti.cycle.context.CycleSessionContext;
import org.activiti.cycle.event.CycleEventListener;
import org.activiti.cycle.transform.ContentArtifactTypeTransformation;
import org.activiti.cycle.transform.ContentMimeTypeTransformation;

/**
 * Annotation for qualifying cycle components. Cycle components are ordinary
 * java classes annotated with {@link CycleComponent}. Cycle components are
 * discovered and managed by the {@link CycleComponentFactory}.
 * <p>
 * <strong>Component Name</strong> <br>
 * A component has a name, which is added as a parameter to this annotation, see
 * {@link #name()}. If no name is provided, the class name of the component is
 * used. Needs to be unique.
 * </p>
 * <p>
 * <strong>Component Type</strong> <br>
 * While cycle components are ordinary java classes some components play a
 * special role in cycle. An example of such a component is a
 * {@link RepositoryConnector}. If such an interface is implemented, we call the
 * implemented interface the <em>type</em> of a cycle component. The following
 * is a list of supported component types:
 * <ul>
 * <li> {@link RepositoryConnector}</li>
 * <li> {@link Action} ( {@link CreateUrlAction})</li>
 * <li> {@link ContentRepresentation}</li>
 * <li> {@link ContentArtifactTypeTransformation}</li>
 * <li> {@link ContentMimeTypeTransformation}</li>
 * <li> {@link MimeType}</li>
 * <li> {@link RepositoryArtifactType}</li>
 * <li> {@link CycleEventListener}</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Component Context</strong> <br>
 * Cycle uses a simple contextual component model. Instances of components are
 * stored in a {@link CycleContext}. Three different cycle contexts are
 * supported:
 * <ul>
 * <li> {@link CycleContextType#REQUEST} ant the corresponding
 * {@link CycleRequestContext}: a request-scoped context</li>
 * <li> {@link CycleContextType#SESSION} and the corresponding
 * {@link CycleSessionContext}: the session context is a usersession-scoped
 * context of which one instance per usersession is managed. Components residing
 * within this context are instantiated once per user session, i.e. each
 * usersession has its own instances of such components.</li>
 * <li> {@link CycleContextType#APPLICATION} and the corresponding
 * {@link CycleApplicationContext}: the application context is a globally
 * visible context, used as a singleton-space.</li>
 * </ul>
 * The context of a cycle component is specified using the {@link #context()}
 * field of this annotation.
 * </p>
 * 
 * 
 * @see CycleComponentFactory
 * @see CycleContextType
 * @see Interceptors
 * 
 * @author daniel.meyer@camunda.com
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })
public @interface CycleComponent {

  /**
   * @see #name()
   */
  String value() default "";

  /**
   * The name of a component. If left blank, the classname of the annotated
   * class is used. Needs to be unique.
   */
  String name() default "";

  /**
   * Optional parameter: used to set the context of the annotated component.
   */
  CycleContextType context() default CycleContextType.NONE;

}
