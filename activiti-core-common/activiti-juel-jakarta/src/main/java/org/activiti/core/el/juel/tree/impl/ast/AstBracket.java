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

package org.activiti.core.el.juel.tree.impl.ast;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import org.activiti.core.el.juel.tree.Bindings;

public class AstBracket extends AstProperty {

    protected final AstNode property;

    public AstBracket(
        AstNode base,
        AstNode property,
        boolean lvalue,
        boolean strict
    ) {
        this(base, property, lvalue, strict, false);
    }

    public AstBracket(
        AstNode base,
        AstNode property,
        boolean lvalue,
        boolean strict,
        boolean ignoreReturnType
    ) {
        super(base, lvalue, strict, ignoreReturnType);
        this.property = property;
    }

    @Override
    protected Object getProperty(Bindings bindings, ELContext context)
        throws ELException {
        return property.eval(bindings, context);
    }

    @Override
    public String toString() {
        return "[...]";
    }

    @Override
    public void appendStructure(StringBuilder b, Bindings bindings) {
        getChild(0).appendStructure(b, bindings);
        b.append("[");
        getChild(1).appendStructure(b, bindings);
        b.append("]");
    }

    public int getCardinality() {
        return 2;
    }

    @Override
    public AstNode getChild(int i) {
        return i == 1 ? property : super.getChild(i);
    }
}
