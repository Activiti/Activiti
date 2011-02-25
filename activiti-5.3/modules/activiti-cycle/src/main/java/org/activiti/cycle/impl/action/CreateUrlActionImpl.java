package org.activiti.cycle.impl.action;

import java.net.URL;

import org.activiti.cycle.action.CreateUrlAction;
import org.activiti.cycle.impl.AbstractArtifactActionImpl;

/**
 * Base class for actions to open {@link URL}s in the GUI (e.g. download files,
 * open the modeler, ...)
 * 
 * @author ruecker
 */
public abstract class CreateUrlActionImpl extends AbstractArtifactActionImpl implements CreateUrlAction {

  private static final long serialVersionUID = 1L;

  // public CreateUrlActionImpl() {
  // }

  public CreateUrlActionImpl(String actionId) {
    super(actionId);
  }

}
