package org.activiti.cycle.impl.representation;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.cycle.Content;
import org.activiti.cycle.ContentRepresentation;
import org.activiti.cycle.MimeType;
import org.activiti.cycle.RenderInfo;
import org.activiti.cycle.RepositoryArtifact;
import org.activiti.cycle.annotations.CycleComponent;
import org.activiti.cycle.context.CycleApplicationContext;
import org.activiti.cycle.context.CycleContextType;
import org.activiti.cycle.impl.mimetype.TextMimeType;
import org.activiti.cycle.impl.mimetype.XmlMimeType;
import org.activiti.cycle.service.CycleServiceFactory;

/**
 * Default {@link ContentRepresentation} for {@link XmlMimeType}
 * 
 * @author daniel.meyer@camunda.com
 */
@CycleComponent(context = CycleContextType.APPLICATION)
public class DefaultXmlContentRepresentation extends AbstractBasicArtifactTypeContentRepresentation {

  private final static Logger log = Logger.getLogger(DefaultXmlContentRepresentation.class.getCanonicalName());

  private static final long serialVersionUID = 1L;

  public RenderInfo getRenderInfo() {
    return RenderInfo.CODE;
  }

  protected Class< ? extends MimeType> getMimeType() {
    return XmlMimeType.class;
  }

  public org.activiti.cycle.Content getContent(RepositoryArtifact artifact) {
    Content content = super.getContent(artifact);
    try {
      // try to transform the content using an XML content transformation
      content = CycleServiceFactory.getContentService().transformContent(content, CycleApplicationContext.get(TextMimeType.class),
              CycleApplicationContext.get(XmlMimeType.class));
    } catch (Exception e) {
      log.log(Level.WARNING, "error while applying XML-Content transformation: " + e.getMessage(), e);
    }
    return content;

  }
}
