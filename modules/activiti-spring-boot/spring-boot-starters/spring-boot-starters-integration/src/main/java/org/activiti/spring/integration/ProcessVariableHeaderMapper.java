package org.activiti.spring.integration;

import org.activiti.engine.ProcessEngine;
import org.springframework.integration.mapping.HeaderMapper;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author Josh Long
 */
public class ProcessVariableHeaderMapper implements HeaderMapper<Map<String, Object>> {

    private final ProcessEngine processEngine;
    private final Set<String> keysToPreserve = new ConcurrentSkipListSet<String>();

    public ProcessVariableHeaderMapper(ProcessEngine processEngine, Set<String> sync) {
        this.processEngine = processEngine;
        keysToPreserve.addAll(sync);
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
}
