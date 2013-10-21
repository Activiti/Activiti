package org.activiti.rest.editor.application;

import org.activiti.rest.editor.main.EditorRestResource;
import org.activiti.rest.editor.main.PluginRestResource;
import org.activiti.rest.editor.main.StencilsetRestResource;
import org.activiti.rest.editor.model.ModelEditorJsonRestResource;
import org.activiti.rest.editor.model.ModelSaveRestResource;
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
