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
package org.activiti.dmn.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yvo Swillens
 */
public class DmnDefinition extends NamedElement {

    public static final String DMN_1_1 = "http://www.omg.org/spec/DMN/20151130";
    protected String expressionLanguage;
    protected String typeLanguage;
    protected String namespace;
    protected List<ItemDefinition> itemDefinitions = new ArrayList<ItemDefinition>();
    protected List<Decision> decisions = new ArrayList<Decision>();

    public String getExpressionLanguage() {
        return expressionLanguage;
    }

    public void setExpressionLanguage(String expressionLanguage) {
        this.expressionLanguage = expressionLanguage;
    }

    public String getTypeLanguage() {
        return typeLanguage;
    }

    public void setTypeLanguage(String typeLanguage) {
        this.typeLanguage = typeLanguage;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<ItemDefinition> getItemDefinitions() {
        return itemDefinitions;
    }

    public void addItemDefinition(ItemDefinition itemDefinition) {
        this.itemDefinitions.add(itemDefinition);
    }

    public List<Decision> getDecisions() {
        return decisions;
    }

    public void addDecision(Decision decision) {
        this.decisions.add(decision);
    }
    
    public Decision getDecisionById(String id) {
        for (Decision decision : decisions) {
            if (id.equals(decision.getId())) {
                return decision;
            }
        }
        return null;
    }

}
