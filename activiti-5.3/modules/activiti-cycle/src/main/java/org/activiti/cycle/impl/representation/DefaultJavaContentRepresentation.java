package org.activiti.cycle.impl.representation;

import org.activiti.cycle.MimeType;
import org.activiti.cycle.RenderInfo;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.mimetype.JavaMimeType;

/**
 * Default {@link ContentRepresentation} for {@link JavaMimeType}
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class DefaultJavaContentRepresentation extends AbstractBasicArtifactTypeContentRepresentation {

  public RenderInfo getRenderInfo() {
    return RenderInfo.TEXT_PLAIN;
  }

  @Override
  protected Class< ? extends MimeType> getMimeType() {
    return JavaMimeType.class;
  }

}
