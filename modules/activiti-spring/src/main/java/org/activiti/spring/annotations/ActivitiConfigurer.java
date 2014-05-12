package org.activiti.spring.annotations;

import org.activiti.spring.SpringProcessEngineConfiguration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;
import java.util.List;

public interface ActivitiConfigurer {
	
	void processDefinitionResources(List<Resource> resourceList);

	void postProcessSpringProcessEngineConfiguration(SpringProcessEngineConfiguration springProcessEngineConfiguration);

	DataSource dataSource();
}