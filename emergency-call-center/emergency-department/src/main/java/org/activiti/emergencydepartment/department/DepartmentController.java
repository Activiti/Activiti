package org.activiti.emergencydepartment.department;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping(value = "/emergency/department/")
public class DepartmentController {

	@RequestMapping(value = "request/{activitiProcessInstance}", method = RequestMethod.GET)
    @ResponseBody
    public ResourceRequest requestResourcesToHospital(@PathVariable String activitiProcessInstance) {

		// TODO: This assignement should be replaced with a business rule.
		Boolean resourcesAvailable = true;
		String notes = "The resources will be there in 5 minutes.";

		return new ResourceRequest(activitiProcessInstance, notes, resourcesAvailable);

	}
}
