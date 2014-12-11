package org.activiti.engine.impl.form;

import org.activiti.bpmn.model.FormProperty;
import org.activiti.engine.form.FormType;

public interface FormTypeSupport<T extends FormType> {

  String CONSTRUCTOR_WITH_FORM_PROPERTY = "newInstance";

  T newInstance(FormProperty formProperty);

}
