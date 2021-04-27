/*
 * Copyright 2010-2020 Alfresco Software, Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.impl.el;

import org.activiti.engine.impl.el.variable.CallPattern;
import org.activiti.engine.impl.el.variable.CallWhitelistUtil;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.el.BeanELResolver;
import javax.el.ELContext;


/**
 * @author Vasile Dirla
 */
public class WhitelistBeanELResolver extends BeanELResolver {

    // whitelisted packages
    protected static final String RUNTIME_BEANS_PACKAGE_NAME = "com.activiti.runtime.activiti.bean.*";
    protected static final String EXTENSION_BEANS_PACKAGE_NAME = "com.activiti.extension.bean.*";


    protected final Set<CallPattern> whitelistedCalls = new LinkedHashSet<>();


    public WhitelistBeanELResolver(Set<String> whitelistedCalls) {

        this.whitelistedCalls.add(CallPattern.parse(RUNTIME_BEANS_PACKAGE_NAME));
        this.whitelistedCalls.add(CallPattern.parse(EXTENSION_BEANS_PACKAGE_NAME));

        if (whitelistedCalls != null) {
            for (String nextCallPattern : whitelistedCalls) {
                if (nextCallPattern != null) {
                    CallPattern callPattern = CallPattern.parse(nextCallPattern);
                    this.whitelistedCalls.add(callPattern);
                }
            }
        }
    }

    @Override
    public Object invoke(ELContext context, Object base, Object method, Class<?>[] paramTypes, Object[] params) {
        if (isResolvable(base, method, paramTypes, params.length)) {
            return super.invoke(context, base, method, paramTypes, params);
        }
        return null;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        if (isResolvable(base)) {
            super.setValue(context, base, property, value);
        }
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (isResolvable(base)) {
            return super.getValue(context, base, property);
        } else {
            return null;
        }
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        return super.isReadOnly(context, base, property);
    }


    private final boolean isResolvable(Object base) {
        if (base == null) {
            return true;
        }
        return CallWhitelistUtil.isWhitelisted(whitelistedCalls, base, null, null, 0);
    }

    private final boolean isResolvable(Object base, Object method, Class<?>[] paramTypes, int paramCount) {
        if (base == null) {
            return true;
        }
        return CallWhitelistUtil.isWhitelisted(whitelistedCalls, base, method, paramTypes, paramCount);
    }

}
