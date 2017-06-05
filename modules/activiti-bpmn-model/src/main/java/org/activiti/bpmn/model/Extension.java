package org.activiti.bpmn.model;

/**
 * Created by francisco on 30/04/17.
 */
public interface Extension {

    String getName();

    String getNamespacePrefix();

    String getNamespace();

    <T extends Extension> T clone();
}
