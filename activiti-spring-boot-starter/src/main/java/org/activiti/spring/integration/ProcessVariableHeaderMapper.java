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
package org.activiti.spring.integration;

import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**

 */
public class ProcessVariableHeaderMapper implements HeaderMapper<Map<String, Object>> {

    private final Set<String> keysToPreserve = new ConcurrentSkipListSet<String>();

    public ProcessVariableHeaderMapper(Set<String> sync) {
        this.keysToPreserve.addAll(sync);
    }

    @Override
    public void fromHeaders(MessageHeaders headers, Map<String, Object> target) {
        // inbound SI msg. take the headers and convert it to
        sync(this.keysToPreserve, headers, target);
    }

    @Override
    public Map<String, Object> toHeaders(Map<String, Object> source) {
        Map<String, Object> matches = sync(
                this.keysToPreserve,
                source,
                new HashMap<String, Object>());
        return matches;
    }

    private static Map<String, Object> sync(
            Set<String> keys,
            Map<String, Object> in,
            Map<String, Object> out) {
        for (String k : keys)
            if (in.containsKey(k))
                out.put(k, in.get(k));
        return out;
    }
}
