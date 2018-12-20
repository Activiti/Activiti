package org.activiti.spring.process.variable.types;

import java.util.List;

import org.activiti.engine.ActivitiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JavaObjectExtensionVariableType extends ExtensionVariableType {

    private static final Logger logger = LoggerFactory.getLogger(JavaObjectExtensionVariableType.class);

    public Class clazz;

    public JavaObjectExtensionVariableType(Class clazz) {
        this.clazz = clazz;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    @Override
    public void validate(Object var,List<ActivitiException> errors) {


        if (!(var).getClass().isAssignableFrom(clazz)){
            String message = var.getClass()+" is not assignable from "+clazz;
            errors.add(new ActivitiException(message));
            logger.error(message);
        }
    }
}
