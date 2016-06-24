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
package com.activiti.rest.runtime.example;

import javax.inject.Inject;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

@RestController
@RequestMapping("/rest/temporary/example-options")
public class ExampleFormOptionResource {
    
    @Inject
    protected ObjectMapper objectMapper;
    
    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public ArrayNode getOptions() {
        ArrayNode optionsNode = objectMapper.createArrayNode();
        for (int i = 0; i < 100; i++) {
            addOption(i, optionsNode);
        }
        return optionsNode;
    }
    
    protected void addOption(int number, ArrayNode optionsNode) {
        ObjectNode optionNode = objectMapper.createObjectNode();
        optionNode.put("someId", number);
        optionNode.put("someLabel", "Label " + number);
        optionsNode.add(optionNode);
    }
}