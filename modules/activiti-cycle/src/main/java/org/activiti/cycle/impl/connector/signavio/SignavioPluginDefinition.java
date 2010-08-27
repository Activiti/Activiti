package org.activiti.cycle.impl.connector.signavio;

import java.util.List;

import org.activiti.cycle.ArtifactAction;
import org.activiti.cycle.ArtifactType;
import org.activiti.cycle.ContentRepresentationProvider;
import org.activiti.cycle.impl.connector.signavio.action.CreateTechnicalBpmnXmlAction;
import org.activiti.cycle.impl.connector.signavio.action.OpenModelerAction;
import org.activiti.cycle.impl.connector.signavio.action.ValidateActivitiDeployment;
import org.activiti.cycle.impl.connector.signavio.provider.ActivitiCompliantBpmn20Provider;
import org.activiti.cycle.impl.connector.signavio.provider.Bpmn20Provider;
import org.activiti.cycle.impl.connector.signavio.provider.Jpdl4Provider;
import org.activiti.cycle.impl.connector.signavio.provider.JsonProvider;
import org.activiti.cycle.impl.connector.signavio.provider.PngProvider;
import org.activiti.cycle.impl.plugin.ActivitiCyclePlugin;
import org.activiti.cycle.impl.plugin.ActivitiCyclePluginDefinition;
import org.activiti.cycle.impl.plugin.DefinitionEntry;

@ActivitiCyclePlugin
public class SignavioPluginDefinition implements ActivitiCyclePluginDefinition {

  public void addDefinedArtifactTypeToList(List<ArtifactType> list) {
    list.add(new ArtifactType("Activiti Modeler BPMN 2.0", SignavioConnector.ORYX_TYPE_ATTRIBUTE_FOR_BPMN_20));
    list.add(new ArtifactType("Signavio BPMN 2.0", SignavioConnector.SIGNAVIO_NAMESPACE_FOR_BPMN_2_0));
    list.add(new ArtifactType("Signavio BPMN for jBPM 4", SignavioConnector.SIGNAVIO_NAMESPACE_FOR_BPMN_JBPM4));
  }

  public void addContentRepresentationProviderToMap(List<DefinitionEntry<Class< ? extends ContentRepresentationProvider>>> contentProviderMap) {
    contentProviderMap.add(new DefinitionEntry<Class< ? extends ContentRepresentationProvider>>(SignavioConnector.SIGNAVIO_NAMESPACE_FOR_BPMN_2_0,
            JsonProvider.class));
    contentProviderMap.add(new DefinitionEntry<Class< ? extends ContentRepresentationProvider>>(SignavioConnector.SIGNAVIO_NAMESPACE_FOR_BPMN_2_0,
            Bpmn20Provider.class));
    contentProviderMap.add(new DefinitionEntry<Class< ? extends ContentRepresentationProvider>>(SignavioConnector.SIGNAVIO_NAMESPACE_FOR_BPMN_2_0,
            ActivitiCompliantBpmn20Provider.class));
    contentProviderMap.add(new DefinitionEntry<Class< ? extends ContentRepresentationProvider>>(SignavioConnector.SIGNAVIO_NAMESPACE_FOR_BPMN_2_0, PngProvider.class));

    // TODO: Refactor, this is weird, we have the type name, type type, then we
    // register that for the type type?
    contentProviderMap.add(new DefinitionEntry<Class< ? extends ContentRepresentationProvider>>(SignavioConnector.ORYX_TYPE_ATTRIBUTE_FOR_BPMN_20,
            PngProvider.class));
    contentProviderMap.add(new DefinitionEntry<Class< ? extends ContentRepresentationProvider>>(SignavioConnector.ORYX_TYPE_ATTRIBUTE_FOR_BPMN_20,
            ActivitiCompliantBpmn20Provider.class));
    contentProviderMap.add(new DefinitionEntry<Class< ? extends ContentRepresentationProvider>>(SignavioConnector.ORYX_TYPE_ATTRIBUTE_FOR_BPMN_20,
            Bpmn20Provider.class));
    contentProviderMap.add(new DefinitionEntry<Class< ? extends ContentRepresentationProvider>>(SignavioConnector.ORYX_TYPE_ATTRIBUTE_FOR_BPMN_20,
            JsonProvider.class));

    
    // Embedebale Model still has problems in the OSS version (usable there at
    // all?)
    // contentProviderMap.add(new DefinitionEntry<Class< ? extends
    // ContentRepresentationProvider>>(SignavioConnector.SIGNAVIO_BPMN_2_0,
    // EmbeddableModelProvider.class));

    contentProviderMap.add(new DefinitionEntry<Class< ? extends ContentRepresentationProvider>>(SignavioConnector.SIGNAVIO_NAMESPACE_FOR_BPMN_JBPM4, PngProvider.class));
    contentProviderMap.add(new DefinitionEntry<Class< ? extends ContentRepresentationProvider>>(SignavioConnector.SIGNAVIO_NAMESPACE_FOR_BPMN_JBPM4, Jpdl4Provider.class));
    contentProviderMap.add(new DefinitionEntry<Class< ? extends ContentRepresentationProvider>>(SignavioConnector.SIGNAVIO_NAMESPACE_FOR_BPMN_JBPM4, JsonProvider.class));
    // contentProviderMap.add(new DefinitionEntry<Class< ? extends
    // ContentRepresentationProvider>>(SignavioConnector.SIGNAVIO_BPMN_JBPM4,
    // EmbeddableModelProvider.class));
  }

  public void addArtifactActionToMap(List<DefinitionEntry<Class< ? extends ArtifactAction>>> actionMap) {
    actionMap.add(new DefinitionEntry<Class< ? extends ArtifactAction>>(SignavioConnector.SIGNAVIO_NAMESPACE_FOR_BPMN_2_0, OpenModelerAction.class));
    actionMap.add(new DefinitionEntry<Class< ? extends ArtifactAction>>(SignavioConnector.SIGNAVIO_NAMESPACE_FOR_BPMN_2_0, CreateTechnicalBpmnXmlAction.class));
    actionMap.add(new DefinitionEntry<Class< ? extends ArtifactAction>>(SignavioConnector.SIGNAVIO_NAMESPACE_FOR_BPMN_2_0, ValidateActivitiDeployment.class));

    actionMap.add(new DefinitionEntry<Class< ? extends ArtifactAction>>(SignavioConnector.ORYX_TYPE_ATTRIBUTE_FOR_BPMN_20, OpenModelerAction.class));
    actionMap.add(new DefinitionEntry<Class< ? extends ArtifactAction>>(SignavioConnector.ORYX_TYPE_ATTRIBUTE_FOR_BPMN_20, CreateTechnicalBpmnXmlAction.class));
    actionMap.add(new DefinitionEntry<Class< ? extends ArtifactAction>>(SignavioConnector.ORYX_TYPE_ATTRIBUTE_FOR_BPMN_20, ValidateActivitiDeployment.class));

    actionMap.add(new DefinitionEntry<Class< ? extends ArtifactAction>>(SignavioConnector.SIGNAVIO_NAMESPACE_FOR_BPMN_JBPM4, OpenModelerAction.class));

    // TODO: Retrieve model through modellink (without /info) and dynamically
    // initialize RepositoryRegistry with supported formats?
  }
}
