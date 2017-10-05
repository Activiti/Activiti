/*
 * Copyright 2017 Alfresco, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.services.query.qraphql.web;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.introproventures.graphql.jpa.query.schema.GraphQLExecutor;
import com.introproventures.graphql.jpa.query.schema.impl.GraphQLJpaExecutor;
import graphql.ExecutionResult;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Activiti GraphQL Query Spring Rest Controller with HTTP mapping endpoints for GraphQLExecutor relay 
 * 
 * @see <a href="http://graphql.org/learn/serving-over-http/">Serving GraphQL over HTTP</a> 
 * 
 */
@RestController
@ConditionalOnWebApplication
@ConditionalOnClass(GraphQLExecutor.class)
public class ActivitiGraphQLController {
    
    private static final String PATH = "${spring.activiti.services.query.graphql.path:/graphql}";
    public static final String APPLICATION_GRAPHQL_VALUE = "application/graphql";
    
    private final GraphQLExecutor   graphQLExecutor;
    private final ObjectMapper  mapper;

    /**
     * Creates instance of Spring GraphQLController RestController
     * 
     * @param graphQLExecutor {@link GraphQLExecutor} instance
     * @param mapper {@link ObjectMapper} instance
     */
    public ActivitiGraphQLController(GraphQLExecutor graphQLExecutor, ObjectMapper mapper) {
        super();
        this.graphQLExecutor = graphQLExecutor;
        this.mapper = mapper;
    }
    
    /**
     * Handle standard GraphQL POST request that consumes 
     * "application/json" content type with a JSON-encoded body 
     * of the following format:
     * <pre>
     * {
     *   "query": "...",
     *   "variables": { "myVariable": "someValue", ... }
     * }
     * </pre>
     * @param queryRequest object
     * @return {@link ExecutionResult} response
     * @throws IOException
     */
    @PostMapping(value = PATH, 
            consumes = {MediaType.APPLICATION_JSON_VALUE}, 
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ExecutionResult postJson(@RequestBody @Valid final GraphQLQueryRequest queryRequest) throws IOException 
    {
        return graphQLExecutor.execute(queryRequest.getQuery(), queryRequest.getVariables());
    }

    /**
     * Handle HTTP GET request.
     * The GraphQL query should be specified in the "query" query string.
     * i.e. <pre> http://server/graphql?query={query{name}}</pre>
     * 
     * Query variables can be sent as a JSON-encoded string in an additional 
     * query parameter called variables.
     * 
     * @param query encoded JSON string
     * @param variables encoded JSON string
     * @return {@link ExecutionResult} response
     * @throws IOException
     */
    @GetMapping(value = PATH, 
            consumes = {APPLICATION_GRAPHQL_VALUE}, 
            produces=MediaType.APPLICATION_JSON_VALUE)
    public ExecutionResult getQuery( 
            @RequestParam(name="query") final String query, 
            @RequestParam(name="variables", required = false) final  String variables) throws IOException 
    {
        Map<String, Object> variablesMap = variablesStringToMap(variables);
        
        return graphQLExecutor.execute(query, variablesMap);
    }
    
    /**
     * Handle HTTP FORM POST request.
     * The GraphQL query should be specified in the "query" query parameter string.
     * 
     * Query variables can be sent as a JSON-encoded string in an additional 
     * query parameter called variables.

     * @param query encoded JSON string
     * @param variables encoded JSON string
     * @return {@link ExecutionResult} response
     * @throws IOException
     */
    @PostMapping(value = PATH, 
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, 
            produces=MediaType.APPLICATION_JSON_VALUE)
    public ExecutionResult postForm( 
            @RequestParam(name="query") final String query, 
            @RequestParam(name="variables", required = false) final String variables) throws IOException 
    {
        Map<String, Object> variablesMap = variablesStringToMap(variables);
        
        return graphQLExecutor.execute(query, variablesMap);
    }

    /**
     * Handle POST with the "application/graphql" Content-Type header.
     * Treat the HTTP POST body contents as the GraphQL query string.
     * 
     * 
     * @param queryRequest a valid {@link GraphQLQueryRequest} input argument
     * @return {@link ExecutionResult} response
     * @throws IOException
     */
    @PostMapping(value = PATH, 
            consumes = APPLICATION_GRAPHQL_VALUE, 
            produces=MediaType.APPLICATION_JSON_VALUE)
    public ExecutionResult postApplicationGraphQL( 
            @RequestBody final String query) throws IOException 
    {
        return graphQLExecutor.execute(query, null);
    }
    
    /**
     * Convert String argument to a Map as expected by {@link GraphQLJpaExecutor#execute(String, Map)}. GraphiQL posts both
     * query and variables as JSON encoded String, so Spring MVC mapping is useless here.
     * See: http://graphql.org/learn/serving-over-http/
     *
     * @param json JSON encoded string variables
     * @return a {@link HashMap} object of variable key-value pairs
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> variablesStringToMap(final String json) throws IOException {
        Map<String, Object> variables = null;
        
        if (json != null && !json.isEmpty())
            variables = mapper.readValue(json, Map.class);
        
        return variables;
    }

    /**
     * GraphQL JSON HTTP Request Wrapper Class  
     */
    @Validated
    public static class GraphQLQueryRequest {

        @NotNull
        private String query;

        private  Map<String, Object> variables;

        GraphQLQueryRequest() {}
        
        /**
         * @param query
         */
        public GraphQLQueryRequest(String query) {
            super();
            this.query = query;
        }

        /**
         * @return the query
         */
        public String getQuery() {
            return this.query;
        }

        /**
         * @param query the query to set
         */
        public void setQuery(String query) {
            this.query = query;
        }

        /**
         * @return the variables
         */
        public Map<String, Object> getVariables() {
            return this.variables;
        }

        /**
         * @param variables the variables to set
         */
        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }

    }
    
}
