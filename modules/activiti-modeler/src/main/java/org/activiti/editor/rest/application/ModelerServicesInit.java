package org.activiti.editor.rest.application;

import org.activiti.editor.rest.main.EditorRestResource;
import org.activiti.editor.rest.main.PluginRestResource;
import org.activiti.editor.rest.main.StencilsetRestResource;
import org.activiti.editor.rest.model.ModelEditorJsonRestResource;
import org.activiti.editor.rest.model.ModelSaveRestResource;
import org.restlet.routing.Router;

public class ModelerServicesInit {

  public static void attachResources(Router router) {
    router.attach("/model/{modelId}/json", ModelEditorJsonRestResource.class);
    router.attach("/model/{modelId}/save", ModelSaveRestResource.class);
    
    router.attach("/editor", EditorRestResource.class);
    router.attach("/editor/plugins", PluginRestResource.class);
    router.attach("/editor/stencilset", StencilsetRestResource.class);
  }
}
