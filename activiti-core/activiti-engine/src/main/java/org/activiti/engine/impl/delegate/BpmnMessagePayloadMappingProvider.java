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
package org.activiti.engine.impl.delegate;

import static java.util.Collections.unmodifiableMap;

import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.Expression;
import org.activiti.engine.impl.bpmn.parser.FieldDeclaration;

public class BpmnMessagePayloadMappingProvider implements MessagePayloadMappingProvider {

    private final List<FieldDeclaration> fieldDeclarations;

    public BpmnMessagePayloadMappingProvider(List<FieldDeclaration> fieldDeclarations) {
        super();
        this.fieldDeclarations = fieldDeclarations;
    }

    @Override
    public Optional<Map<String, Object>> getMessagePayload(DelegateExecution execution) {
        Map<String, Object> payload = new LinkedHashMap<>();

        fieldDeclarations.stream()
                         .map(field -> applyFieldDeclaration(execution,
                                                             field))
                         .forEach(entry -> payload.put(entry.getKey(), entry.getValue()));

        return Optional.of(payload)
                       .filter(map -> !map.isEmpty())
                       .map(map -> unmodifiableMap(map));
    }

    protected Map.Entry<String, Object> applyFieldDeclaration(DelegateExecution execution, FieldDeclaration field) {
        return Optional.of(field)
                       .map(f -> {
                           Object value = Optional.ofNullable(f.getValue())
                                                  .map(v -> (Expression.class.isInstance(v))
                                                               ? Expression.class.cast(v).getValue(execution)
                                                               : v)
                                                  .orElse(null);

                           return new AbstractMap.SimpleImmutableEntry<>(field.getName(), value);
                        })
                       .get();
    }
}
