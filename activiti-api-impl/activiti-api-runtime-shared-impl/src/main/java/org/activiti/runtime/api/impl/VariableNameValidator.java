package org.activiti.runtime.api.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public class VariableNameValidator {
    
    public static String regexPattern = "(?i)[a-z][a-z0-9_]*";
    
    public boolean validate(String name) { 
        
        if (StringUtils.hasLength(name)) {           
            if (Pattern.compile(regexPattern).matcher(name).matches()) {
                return true;
            }  
        }
        return false;
        
    }
    
    public Set<String> validateVariables(Map<String, Object> variables) {
        Set<String> mismatchedVars = new HashSet<>();
        if (variables != null && !variables.isEmpty()) {
            for (Map.Entry<String, Object> variable : variables.entrySet()) {
                if (!validate(variable.getKey())) {
                    mismatchedVars.add(variable.getKey());
                }
            }   
        }
        
        return mismatchedVars;
    }

}
