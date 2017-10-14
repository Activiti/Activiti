package org.activiti.services.audit.mongo.config;

import org.activiti.services.audit.mongo.events.ActivityCompletedEventDocument;
import org.activiti.services.audit.mongo.events.ActivityStartedEventDocument;
import org.activiti.services.audit.mongo.events.ProcessCompletedEventDocument;
import org.activiti.services.audit.mongo.events.ProcessStartedEventDocument;
import org.activiti.services.audit.mongo.events.SequenceFlowTakenEventDocument;
import org.activiti.services.audit.mongo.events.TaskAssignedEventDocument;
import org.activiti.services.audit.mongo.events.TaskCompletedEventDocument;
import org.activiti.services.audit.mongo.events.TaskCreatedEventDocument;
import org.activiti.services.audit.mongo.events.VariableCreatedEventDocument;
import org.activiti.services.audit.mongo.events.VariableDeletedEventDocument;
import org.activiti.services.audit.mongo.events.VariableUpdatedEventDocument;
import org.activiti.services.audit.mongo.repository.EventsRepository;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.core.mapping.RepositoryDetectionStrategy.RepositoryDetectionStrategies;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@Configuration
@EnableSpringDataWebSupport
@EnableMongoRepositories(basePackageClasses = EventsRepository.class)
public class RepositoryConfig extends RepositoryRestConfigurerAdapter {

    @Override
    public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
        config.setBasePath("/v1");
        
        // Expose only repositories annotated with @RepositoryRestResource
        config.setRepositoryDetectionStrategy(RepositoryDetectionStrategies.ANNOTATED);
        
        config.exposeIdsFor(ActivityCompletedEventDocument.class);
        config.exposeIdsFor(ActivityStartedEventDocument.class);
        config.exposeIdsFor(ProcessCompletedEventDocument.class);
        config.exposeIdsFor(ProcessStartedEventDocument.class);
        config.exposeIdsFor(SequenceFlowTakenEventDocument.class);
        config.exposeIdsFor(TaskAssignedEventDocument.class);
        config.exposeIdsFor(TaskCompletedEventDocument.class);
        config.exposeIdsFor(TaskCreatedEventDocument.class);
        config.exposeIdsFor(VariableCreatedEventDocument.class);
        config.exposeIdsFor(VariableDeletedEventDocument.class);
        config.exposeIdsFor(VariableUpdatedEventDocument.class);
    }
}
