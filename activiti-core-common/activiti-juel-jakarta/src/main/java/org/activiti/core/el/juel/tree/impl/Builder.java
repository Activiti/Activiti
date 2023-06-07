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

package org.activiti.core.el.juel.tree.impl;

import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.FunctionMapper;
import jakarta.el.VariableMapper;
import java.io.PrintWriter;
import java.util.EnumSet;
import org.activiti.core.el.juel.tree.Bindings;
import org.activiti.core.el.juel.tree.NodePrinter;
import org.activiti.core.el.juel.tree.Tree;
import org.activiti.core.el.juel.tree.TreeBuilder;
import org.activiti.core.el.juel.tree.TreeBuilderException;
import org.activiti.core.el.juel.tree.impl.Parser.ParseException;

/**
 * Tree builder.
 *
 * @author Christoph Beck
 */
public class Builder implements TreeBuilder {

    private static final long serialVersionUID = 1L;

    /**
     * Feature enumeration type.
     */
    public static enum Feature {
        /**
         * Method invocations as in <code>${foo.bar(1)}</code> as specified in JSR 245,
         * maintenance release 2.
         * The method to be invoked is resolved at evaluation time by calling
         * {@link ELResolver#invoke(ELContext, Object, Object, Class[], Object[])}.
         */
        METHOD_INVOCATIONS,
        /**
         * For some reason we don't understand, the specification does not allow to resolve
         * <code>null</code> property values. E.g. <code>${map[key]}</code> will always
         * return <code>null</code> if <code>key</code> evaluates to <code>null</code>.
         * Enabling this feature will allow <em>JUEL</em> to pass <code>null</code> to
         * the property resolvers just like any other property value.
         */
        NULL_PROPERTIES,
        /**
         * Allow for use of Java 5 varargs in function calls.
         */
        VARARGS,
        /**
         * Do not verify that a method's return type matches the expected return type passed to
         * {@link ExpressionFactory#createMethodExpression(ELContext, String, Class, Class[])}.
         */
        IGNORE_RETURN_TYPE,
    }

    protected final EnumSet<Feature> features;

    public Builder() {
        this.features = EnumSet.noneOf(Feature.class);
    }

    public Builder(Feature... features) {
        if (features == null || features.length == 0) {
            this.features = EnumSet.noneOf(Feature.class);
        } else if (features.length == 1) {
            this.features = EnumSet.of(features[0]);
        } else {
            Feature[] rest = new Feature[features.length - 1];
            for (int i = 1; i < features.length; i++) {
                rest[i - 1] = features[i];
            }
            this.features = EnumSet.of(features[0], rest);
        }
    }

    /**
     * @return <code>true</code> iff the specified feature is supported.
     */
    public boolean isEnabled(Feature feature) {
        return features.contains(feature);
    }

    /**
     * Parse expression.
     */
    public Tree build(String expression) throws TreeBuilderException {
        try {
            return createParser(expression).tree();
        } catch (Scanner.ScanException e) {
            throw new TreeBuilderException(
                expression,
                e.position,
                e.encountered,
                e.expected,
                e.getMessage()
            );
        } catch (ParseException e) {
            throw new TreeBuilderException(
                expression,
                e.position,
                e.encountered,
                e.expected,
                e.getMessage()
            );
        }
    }

    protected Parser createParser(String expression) {
        return new Parser(this, expression);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        return features.equals(((Builder) obj).features);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    /**
     * Dump out abstract syntax tree for a given expression
     *
     * @param args array with one element, containing the expression string
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println(
                "usage: java " +
                Builder.class.getName() +
                " <expression string>"
            );
            System.exit(1);
        }
        PrintWriter out = new PrintWriter(System.out);
        Tree tree = null;
        try {
            tree = new Builder(Feature.METHOD_INVOCATIONS).build(args[0]);
        } catch (TreeBuilderException e) {
            System.out.println(e.getMessage());
            System.exit(0);
        }
        NodePrinter.dump(out, tree.getRoot());
        if (
            !tree.getFunctionNodes().iterator().hasNext() &&
            !tree.getIdentifierNodes().iterator().hasNext()
        ) {
            ELContext context = new ELContext() {
                @Override
                public VariableMapper getVariableMapper() {
                    return null;
                }

                @Override
                public FunctionMapper getFunctionMapper() {
                    return null;
                }

                @Override
                public ELResolver getELResolver() {
                    return null;
                }
            };
            out.print(">> ");
            try {
                out.println(
                    tree
                        .getRoot()
                        .getValue(new Bindings(null, null), context, null)
                );
            } catch (ELException e) {
                out.println(e.getMessage());
            }
        }
        out.flush();
    }
}
