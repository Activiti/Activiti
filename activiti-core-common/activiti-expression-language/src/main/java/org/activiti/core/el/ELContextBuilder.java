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
package org.activiti.core.el;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;

/**
 * Builder of {@link javax.el.ELContext} instances.
 */
public class ELContextBuilder {

    private List<ELResolver> resolvers;
    private Map<String, Object> variables;

    public ELContextBuilder withResolvers(ELResolver... resolvers) {
        this.resolvers = List.of(resolvers);
        return this;
    }

    public ELContextBuilder withVariables(Map<String, Object> variables) {
        this.variables = variables;
        return this;
    }

    public ELContext build() {
        CompositeELResolver elResolver = new CompositeELResolver();
        elResolver.add(new ReadOnlyMapELResolver(new HashMap<>(variables)));
        addResolvers(elResolver);
        return new ActivitiElContext(elResolver);
    }

    private void addResolvers(CompositeELResolver compositeResolver) {
        Stream.ofNullable(resolvers)
            .flatMap(Collection::stream)
            .forEach(compositeResolver::add);
    }
}
