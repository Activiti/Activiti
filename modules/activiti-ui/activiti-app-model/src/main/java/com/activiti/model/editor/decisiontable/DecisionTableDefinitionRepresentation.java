/**
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
package com.activiti.model.editor.decisiontable;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

/**
 * Created by Yvo Swillens
 */
@JsonInclude(Include.NON_NULL)
public class DecisionTableDefinitionRepresentation {

    protected Long id;
    protected String name;
    protected String key;
    protected String description;
    protected String hitIndicator;
    protected String completenessIndicator;
    protected List<DecisionTableExpressionRepresentation> inputExpressions;
    protected List<DecisionTableExpressionRepresentation> outputExpressions;
    protected List<Map<String,Object>> rules;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }

    public String getHitIndicator() {
        return hitIndicator;
    }

    public void setHitIndicator(String hitIndicator) {
        this.hitIndicator = hitIndicator;
    }

    public String getCompletenessIndicator() {
        return completenessIndicator;
    }

    public void setCompletenessIndicator(String completenessIndicator) {
        this.completenessIndicator = completenessIndicator;
    }

    public List<DecisionTableExpressionRepresentation> getInputExpressions() {
        return inputExpressions;
    }

    public void setInputExpressions(List<DecisionTableExpressionRepresentation> inputExpressions) {
        this.inputExpressions = inputExpressions;
    }

    public List<DecisionTableExpressionRepresentation> getOutputExpressions() {
        return outputExpressions;
    }

    public void setOutputExpressions(List<DecisionTableExpressionRepresentation> outputExpressions) {
        this.outputExpressions = outputExpressions;
    }

    public List<Map<String, Object>> getRules() {
        return rules;
    }

    public void setRules(List<Map<String, Object>> rules) {
        this.rules = rules;
    }

}
