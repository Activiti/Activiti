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

package org.activiti.camel;

import org.activiti.engine.impl.bpmn.BpmnActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityBehavior;
import org.activiti.engine.impl.pvm.delegate.ActivityExecution;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;

import java.util.Collection;
import java.util.Map;

public class CamelBehaviour extends BpmnActivityBehavior implements ActivityBehavior {

    private Collection<ContextProvider> contextProviders;

    public CamelBehaviour(Collection<ContextProvider> camelContext) {
        this.contextProviders =  camelContext;
    }

    public void execute(ActivityExecution execution) throws Exception {
        ActivitiEndpoint ae = createEndpoint(execution);
        ae.process(createExchange(execution));
        performDefaultOutgoingBehavior(execution);

    }


    private ActivitiEndpoint createEndpoint(ActivityExecution execution) {
        String uri = "activiti:"+getProcessName(execution)+":"+execution.getActivity().getId();
        ActivitiEndpoint ae = (ActivitiEndpoint) getContext(execution).getEndpoint(uri);
        if (ae == null) {
            throw new RuntimeException("Activiti endpoint not defined for "+uri);
        }
        return ae;
    }

    private CamelContext getContext(ActivityExecution execution) {
        String processName = getProcessName(execution);
        String names = "";
        for (ContextProvider provider : contextProviders) {
            CamelContext ctx = provider.getContext(processName);
            if (ctx != null) {
                return ctx;
            }
        }
        throw new RuntimeException("Could not find camel context for "+processName+" names are "+names);
    }



    private Exchange createExchange(ActivityExecution activityExecution) {
        Exchange ex = new DefaultExchange(getContext(activityExecution));
        for (Map.Entry<String, Object> var : activityExecution.getVariables().entrySet()) {
            ex.setProperty(var.getKey(), var.getValue());
        }
        return ex;
    }

    private String getProcessName(ActivityExecution execution) {
        String id = execution.getActivity().getProcessDefinition().getId();
        return id.substring(0, id.indexOf(":"));

    }

}
