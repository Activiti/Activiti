/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package com.activiti.service.runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.activiti.domain.runtime.RelatedContent;

public class SubmittedFormVariables {

    private Map<String, List<RelatedContent>> variableContent;
    private Map<String, Object> variables;

    public Map<String, List<RelatedContent>> getVariableContent() {
        return variableContent;
    }
    public void setVariableContent(Map<String, List<RelatedContent>> variableContent) {
        this.variableContent = variableContent;
    }
    public Map<String, Object> getVariables() {
        return variables;
    }
    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }

    public void addContent(String variableName, RelatedContent content) {
        if (variableContent == null) {
            variableContent = new HashMap<String, List<RelatedContent>>();
        }
        List<RelatedContent> contentList = variableContent.get(variableName);
        if (contentList == null) {
            contentList = new ArrayList<RelatedContent>();
            variableContent.put(variableName, contentList);
        }

        // allow forms to have empty content lists.
        if (content != null) {
            contentList.add(content);
        }

    }

    public boolean hasContent() {
        if (variableContent != null) {
            return !variableContent.isEmpty();
        }
        return false;
    }
}
