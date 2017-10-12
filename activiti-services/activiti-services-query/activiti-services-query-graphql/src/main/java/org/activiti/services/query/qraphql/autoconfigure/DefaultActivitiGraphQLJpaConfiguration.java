package org.activiti.services.query.qraphql.autoconfigure;

import javax.persistence.EntityManager;

import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.GraphQLSchemaBuilder;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaSchemaBuilder;
import org.activiti.services.query.model.ProcessInstance;
import org.activiti.services.query.qraphql.web.ActivitiGraphQLController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;

/**
 * Provides default configuration of Activiti GraphQL JPA Query Components 
 *
 */
@Configuration
@Import(ActivitiGraphQLController.class)
@EntityScan(basePackageClasses = ProcessInstance.class)
@EnableConfigurationProperties(ActivitiGraphQLSchemaProperties.class) 
public class DefaultActivitiGraphQLJpaConfiguration implements ImportAware {

    @Autowired
    private ActivitiGraphQLSchemaProperties properties;

    @Bean
    @ConditionalOnMissingBean(GraphQLExecutor.class)
    public GraphQLExecutor graphQLExecutor(final GraphQLSchemaBuilder graphQLSchemaBuilder) {
        return new GraphQLJpaExecutor(graphQLSchemaBuilder.build());
    }

    @Bean
    @ConditionalOnMissingBean(GraphQLSchemaBuilder.class)
    public GraphQLSchemaBuilder graphQLSchemaBuilder(final EntityManager entityManager) {
        Assert.notNull(properties.getName(), "GraphQL schema name cannot be null.");
        Assert.notNull(properties.getDescription(), "GraphQL schema description cannot be null.");

        return new GraphQLJpaSchemaBuilder(entityManager)
             .name(properties.getName())
             .description(properties.getDescription());
    }

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        this.properties.setEnabled(true);
    }

}
