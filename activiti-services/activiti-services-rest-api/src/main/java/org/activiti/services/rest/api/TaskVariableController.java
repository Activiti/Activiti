package org.activiti.services.rest.api;

import java.util.Map;

import org.activiti.services.core.model.commands.SetTaskVariablesCmd;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

public interface TaskVariableController {

    @RequestMapping(value = "/",
            method = RequestMethod.GET)
    Resource<Map<String, Object>> getVariables(@PathVariable String taskId);

    @RequestMapping(value = "/local",
            method = RequestMethod.GET)
    Resource<Map<String, Object>> getVariablesLocal(@PathVariable String taskId);

    @RequestMapping(value = "/",
            method = RequestMethod.POST)
    ResponseEntity<Void> setVariables(@PathVariable String taskId,
                                      @RequestBody(required = true) SetTaskVariablesCmd setTaskVariablesCmd);

    @RequestMapping(value = "/local",
            method = RequestMethod.POST)
    ResponseEntity<Void> setVariablesLocal(@PathVariable String taskId,
                                           @RequestBody(
                                                   required = true) SetTaskVariablesCmd setTaskVariablesCmd);
}
