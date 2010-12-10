package org.activiti.cycle;

import java.util.Map;

/**
 * Base class for actions requiring paremeters which must be displayed in an own
 * gui form element.
 * 
 * The form element must be returned as HTML and the paremeters posted are
 * handed in as {@link Map} from the GUI.
 * 
 * Normally best is to use the {@link ParametrizedFreemakerTemplateAction} which
 * has already built in support for generating HTML forms via freemarker
 * templates as it is done already in the Task-Form-GUI
 * 
 * @author ruecker
 */
public interface ParameterizedAction {
  
  public String getId();

  /**
   * returns html form for the action parameters as String. Returns null if no
   * form is needed
   * 
   * TODO: Think about Actions without parameters (Return null or create own
   * superclass)
   */
  public String getFormAsHtml();

  public void execute(RepositoryConnector connector, RepositoryArtifact artifact, Map<String, Object> parameters) throws Exception;
    
}
