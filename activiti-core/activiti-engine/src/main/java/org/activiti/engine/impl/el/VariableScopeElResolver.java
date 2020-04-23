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

package org.activiti.engine.impl.el;

import java.beans.FeatureDescriptor;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.el.ELContext;
import javax.el.ELResolver;
import org.activiti.engine.delegate.VariableScope;
import org.activiti.engine.impl.context.Context;
import org.activiti.engine.impl.el.variable.AuthenticatedUserELResolver;
import org.activiti.engine.impl.el.variable.ExecutionElResolver;
import org.activiti.engine.impl.el.variable.ProcessInitiatorELResolver;
import org.activiti.engine.impl.el.variable.TaskElResolver;
import org.activiti.engine.impl.el.variable.VariableElResolver;
import org.activiti.engine.impl.el.variable.VariableScopeItemELResolver;

/**
 * Implementation of an {@link ELResolver} that resolves expressions with the
 * process variables of a given {@link VariableScope} as context. <br>
 * Also exposes the currently logged in username to be used in expressions (if
 * any)
 *
 *
 *
 */
public class VariableScopeElResolver extends ELResolver {

    protected VariableScope variableScope;
    private List<VariableScopeItemELResolver> variableScopeItemELResolvers;

    public VariableScopeElResolver(VariableScope variableScope) {
        this.variableScope = variableScope;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {

        if (base == null) {
            String variable = (String) property; // according to javadoc, can
            // only be a String

            for (VariableScopeItemELResolver variableScopeItemELResolver : getVariableScopeItemELResolvers()) {
                if (variableScopeItemELResolver.canResolve(variable, variableScope)) {
                    // if not set, the next elResolver in the CompositeElResolver
                    // will be called
                    context.setPropertyResolved(true);

                    return variableScopeItemELResolver.resolve(variable, variableScope);
                }
            }
        }

        // property resolution (eg. bean.value) will be done by the
        // BeanElResolver (part of the CompositeElResolver)
        // It will use the bean resolved in this resolver as base.

        return null;
    }

    protected List<VariableScopeItemELResolver> getVariableScopeItemELResolvers() {
        if (variableScopeItemELResolvers == null) {
            variableScopeItemELResolvers = Arrays.asList(
                new ExecutionElResolver(),
                new TaskElResolver(),
                new AuthenticatedUserELResolver(),
                new ProcessInitiatorELResolver(),
                new VariableElResolver(Context.getProcessEngineConfiguration().getObjectMapper()));
        }
        return variableScopeItemELResolvers;
    }

	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		if (base == null) {
			String variable = (String) property;
			return !variableScope.hasVariable(variable);
		}
		return true;
	}

	@Override
	public void setValue(ELContext context, Object base, Object property, Object value) {
		if (base == null) {
			String variable = (String) property;
			if (variableScope.hasVariable(variable)) {
				variableScope.setVariable(variable, value);
			}
		}
	}

	@Override
	public Class<?> getCommonPropertyType(ELContext arg0, Object arg1) {
		return Object.class;
	}

	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext arg0, Object arg1) {
		return null;
	}

	@Override
	public Class<?> getType(ELContext arg0, Object arg1, Object arg2) {
		return Object.class;
	}

}
