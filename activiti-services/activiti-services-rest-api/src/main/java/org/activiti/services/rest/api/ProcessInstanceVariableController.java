package org.activiti.services.rest.api;

import java.util.Map;

import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@RequestMapping(value = "/v1/process-instances/{processInstanceId}/variables", produces = MediaTypes.HAL_JSON_VALUE)
public interface ProcessInstanceVariableController {

    @RequestMapping(method = RequestMethod.GET)
    Resource<Map<String, Object>> getVariables(@PathVariable String processInstanceId);
}
