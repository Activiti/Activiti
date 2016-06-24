/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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