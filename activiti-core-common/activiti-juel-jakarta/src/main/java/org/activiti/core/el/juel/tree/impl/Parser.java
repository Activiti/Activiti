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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.activiti.core.el.juel.misc.LocalMessages;
import org.activiti.core.el.juel.tree.FunctionNode;
import org.activiti.core.el.juel.tree.IdentifierNode;
import org.activiti.core.el.juel.tree.Tree;
import org.activiti.core.el.juel.tree.impl.ast.AstBinary;
import org.activiti.core.el.juel.tree.impl.ast.AstBoolean;
import org.activiti.core.el.juel.tree.impl.ast.AstBracket;
import org.activiti.core.el.juel.tree.impl.ast.AstChoice;
import org.activiti.core.el.juel.tree.impl.ast.AstComposite;
import org.activiti.core.el.juel.tree.impl.ast.AstDot;
import org.activiti.core.el.juel.tree.impl.ast.AstEval;
import org.activiti.core.el.juel.tree.impl.ast.AstFunction;
import org.activiti.core.el.juel.tree.impl.ast.AstIdentifier;
import org.activiti.core.el.juel.tree.impl.ast.AstMethod;
import org.activiti.core.el.juel.tree.impl.ast.AstNested;
import org.activiti.core.el.juel.tree.impl.ast.AstNode;
import org.activiti.core.el.juel.tree.impl.ast.AstNull;
import org.activiti.core.el.juel.tree.impl.ast.AstNumber;
import org.activiti.core.el.juel.tree.impl.ast.AstParameters;
import org.activiti.core.el.juel.tree.impl.ast.AstProperty;
import org.activiti.core.el.juel.tree.impl.ast.AstString;
import org.activiti.core.el.juel.tree.impl.ast.AstText;
import org.activiti.core.el.juel.tree.impl.ast.AstUnary;

/**
 * Handcrafted top-down parser.
 *
 * @author Christoph Beck
 */
public class Parser {

    /**
     * Parse exception type
     */
    @SuppressWarnings("serial")
    public static class ParseException extends Exception {

        final int position;
        final String encountered;
        final String expected;

        public ParseException(
            int position,
            String encountered,
            String expected
        ) {
            super(
                LocalMessages.get(
                    "error.parse",
                    position,
                    encountered,
                    expected
                )
            );
            this.position = position;
            this.encountered = encountered;
            this.expected = expected;
        }
    }

    /**
     * Token type (used to store lookahead)
     */
    private static final class LookaheadToken {

        final Scanner.Token token;
        final int position;

        LookaheadToken(Scanner.Token token, int position) {
            this.token = token;
            this.position = position;
        }
    }

    public enum ExtensionPoint {
        OR,
        AND,
        EQ,
        CMP,
        ADD,
        MUL,
        UNARY,
        LITERAL,
    }

    /**
     * Provide limited support for syntax extensions.
     */
    public abstract static class ExtensionHandler {

        private final ExtensionPoint point;

        public ExtensionHandler(ExtensionPoint point) {
            this.point = point;
        }

        /**
         * @return the extension point specifying where this syntax extension is active
         */
        public ExtensionPoint getExtensionPoint() {
            return point;
        }

        /**
         * Called by the parser if it handles a extended token associated with this handler
         * at the appropriate extension point.
         * @param children
         * @return abstract syntax tree node
         */
        public abstract AstNode createAstNode(AstNode... children);
    }

    private static final String EXPR_FIRST =
        Scanner.Symbol.IDENTIFIER +
        "|" +
        Scanner.Symbol.STRING +
        "|" +
        Scanner.Symbol.FLOAT +
        "|" +
        Scanner.Symbol.INTEGER +
        "|" +
        Scanner.Symbol.TRUE +
        "|" +
        Scanner.Symbol.FALSE +
        "|" +
        Scanner.Symbol.NULL +
        "|" +
        Scanner.Symbol.MINUS +
        "|" +
        Scanner.Symbol.NOT +
        "|" +
        Scanner.Symbol.EMPTY +
        "|" +
        Scanner.Symbol.LPAREN;

    protected final Builder context;
    protected final Scanner scanner;

    private List<IdentifierNode> identifiers = Collections.emptyList();
    private List<FunctionNode> functions = Collections.emptyList();
    private List<LookaheadToken> lookahead = Collections.emptyList();

    private Scanner.Token token; // current token
    private int position; // current token's position

    protected Map<Scanner.ExtensionToken, ExtensionHandler> extensions = Collections.emptyMap();

    public Parser(Builder context, String input) {
        this.context = context;
        this.scanner = createScanner(input);
    }

    protected Scanner createScanner(String expression) {
        return new Scanner(expression);
    }

    public void putExtensionHandler(
        Scanner.ExtensionToken token,
        ExtensionHandler extension
    ) {
        if (extensions.isEmpty()) {
            extensions =
                new HashMap<Scanner.ExtensionToken, ExtensionHandler>(16);
        }
        extensions.put(token, extension);
    }

    protected ExtensionHandler getExtensionHandler(Scanner.Token token) {
        return extensions.get(token);
    }

    /**
     * Parse an integer literal.
     * @param string string to parse
     * @return <code>Long.valueOf(string)</code>
     */
    protected Number parseInteger(String string) throws ParseException {
        try {
            return Long.valueOf(string);
        } catch (NumberFormatException e) {
            fail(Scanner.Symbol.INTEGER);
            return null;
        }
    }

    /**
     * Parse a floating point literal.
     * @param string string to parse
     * @return <code>Double.valueOf(string)</code>
     */
    protected Number parseFloat(String string) throws ParseException {
        try {
            return Double.valueOf(string);
        } catch (NumberFormatException e) {
            fail(Scanner.Symbol.FLOAT);
            return null;
        }
    }

    protected AstBinary createAstBinary(
        AstNode left,
        AstNode right,
        AstBinary.Operator operator
    ) {
        return new AstBinary(left, right, operator);
    }

    protected AstBracket createAstBracket(
        AstNode base,
        AstNode property,
        boolean lvalue,
        boolean strict
    ) {
        return new AstBracket(
            base,
            property,
            lvalue,
            strict,
            context.isEnabled(Builder.Feature.IGNORE_RETURN_TYPE)
        );
    }

    protected AstChoice createAstChoice(
        AstNode question,
        AstNode yes,
        AstNode no
    ) {
        return new AstChoice(question, yes, no);
    }

    protected AstComposite createAstComposite(List<AstNode> nodes) {
        return new AstComposite(nodes);
    }

    protected AstDot createAstDot(
        AstNode base,
        String property,
        boolean lvalue
    ) {
        return new AstDot(
            base,
            property,
            lvalue,
            context.isEnabled(Builder.Feature.IGNORE_RETURN_TYPE)
        );
    }

    protected AstFunction createAstFunction(
        String name,
        int index,
        AstParameters params
    ) {
        return new AstFunction(
            name,
            index,
            params,
            context.isEnabled(Builder.Feature.VARARGS)
        );
    }

    protected AstIdentifier createAstIdentifier(String name, int index) {
        return new AstIdentifier(
            name,
            index,
            context.isEnabled(Builder.Feature.IGNORE_RETURN_TYPE)
        );
    }

    protected AstMethod createAstMethod(
        AstProperty property,
        AstParameters params
    ) {
        return new AstMethod(property, params);
    }

    protected AstUnary createAstUnary(
        AstNode child,
        AstUnary.Operator operator
    ) {
        return new AstUnary(child, operator);
    }

    protected final List<FunctionNode> getFunctions() {
        return functions;
    }

    protected final List<IdentifierNode> getIdentifiers() {
        return identifiers;
    }

    protected final Scanner.Token getToken() {
        return token;
    }

    /**
     * throw exception
     */
    protected void fail(String expected) throws ParseException {
        throw new ParseException(
            position,
            "'" + token.getImage() + "'",
            expected
        );
    }

    /**
     * throw exception
     */
    protected void fail(Scanner.Symbol expected) throws ParseException {
        fail(expected.toString());
    }

    /**
     * get lookahead symbol.
     */
    protected final Scanner.Token lookahead(int index)
        throws Scanner.ScanException, ParseException {
        if (lookahead.isEmpty()) {
            lookahead = new LinkedList<LookaheadToken>();
        }
        while (index >= lookahead.size()) {
            lookahead.add(
                new LookaheadToken(scanner.next(), scanner.getPosition())
            );
        }
        return lookahead.get(index).token;
    }

    /**
     * consume current token (get next token).
     * @return the consumed token (which was the current token when calling this method)
     */
    protected final Scanner.Token consumeToken()
        throws Scanner.ScanException, ParseException {
        Scanner.Token result = token;
        if (lookahead.isEmpty()) {
            token = scanner.next();
            position = scanner.getPosition();
        } else {
            LookaheadToken next = lookahead.remove(0);
            token = next.token;
            position = next.position;
        }
        return result;
    }

    /**
     * consume current token (get next token); throw exception if the current token doesn't
     * match the expected symbol.
     */
    protected final Scanner.Token consumeToken(Scanner.Symbol expected)
        throws Scanner.ScanException, ParseException {
        if (token.getSymbol() != expected) {
            fail(expected);
        }
        return consumeToken();
    }

    /**
     * tree := text? ((dynamic text?)+ | (deferred text?)+)?
     */
    public Tree tree() throws Scanner.ScanException, ParseException {
        consumeToken();
        AstNode t = text();
        if (token.getSymbol() == Scanner.Symbol.EOF) {
            if (t == null) {
                t = new AstText("");
            }
            return new Tree(t, functions, identifiers, false);
        }
        AstEval e = eval();
        if (token.getSymbol() == Scanner.Symbol.EOF && t == null) {
            return new Tree(e, functions, identifiers, e.isDeferred());
        }
        ArrayList<AstNode> list = new ArrayList<AstNode>();
        if (t != null) {
            list.add(t);
        }
        list.add(e);
        t = text();
        if (t != null) {
            list.add(t);
        }
        while (token.getSymbol() != Scanner.Symbol.EOF) {
            if (e.isDeferred()) {
                list.add(eval(true, true));
            } else {
                list.add(eval(true, false));
            }
            t = text();
            if (t != null) {
                list.add(t);
            }
        }
        return new Tree(
            createAstComposite(list),
            functions,
            identifiers,
            e.isDeferred()
        );
    }

    /**
     * text := &lt;TEXT&gt;
     */
    protected AstNode text() throws Scanner.ScanException, ParseException {
        AstNode v = null;
        if (token.getSymbol() == Scanner.Symbol.TEXT) {
            v = new AstText(token.getImage());
            consumeToken();
        }
        return v;
    }

    /**
     * eval := dynamic | deferred
     */
    protected AstEval eval() throws Scanner.ScanException, ParseException {
        AstEval e = eval(false, false);
        if (e == null) {
            e = eval(false, true);
            if (e == null) {
                fail(
                    Scanner.Symbol.START_EVAL_DEFERRED +
                    "|" +
                    Scanner.Symbol.START_EVAL_DYNAMIC
                );
            }
        }
        return e;
    }

    /**
     * dynmamic := &lt;START_EVAL_DYNAMIC&gt; expr &lt;END_EVAL&gt;
     * deferred := &lt;START_EVAL_DEFERRED&gt; expr &lt;END_EVAL&gt;
     */
    protected AstEval eval(boolean required, boolean deferred)
        throws Scanner.ScanException, ParseException {
        AstEval v = null;
        Scanner.Symbol start_eval = deferred
            ? Scanner.Symbol.START_EVAL_DEFERRED
            : Scanner.Symbol.START_EVAL_DYNAMIC;
        if (token.getSymbol() == start_eval) {
            consumeToken();
            v = new AstEval(expr(true), deferred);
            consumeToken(Scanner.Symbol.END_EVAL);
        } else if (required) {
            fail(start_eval);
        }
        return v;
    }

    /**
     * expr := or (&lt;QUESTION&gt; expr &lt;COLON&gt; expr)?
     */
    protected AstNode expr(boolean required)
        throws Scanner.ScanException, ParseException {
        AstNode v = or(required);
        if (v == null) {
            return null;
        }
        if (token.getSymbol() == Scanner.Symbol.QUESTION) {
            consumeToken();
            AstNode a = expr(true);
            consumeToken(Scanner.Symbol.COLON);
            AstNode b = expr(true);
            v = createAstChoice(v, a, b);
        }
        return v;
    }

    /**
     * or := and (&lt;OR&gt; and)*
     */
    protected AstNode or(boolean required)
        throws Scanner.ScanException, ParseException {
        AstNode v = and(required);
        if (v == null) {
            return null;
        }
        while (true) {
            switch (token.getSymbol()) {
                case OR:
                    consumeToken();
                    v = createAstBinary(v, and(true), AstBinary.OR);
                    break;
                case EXTENSION:
                    if (
                        getExtensionHandler(token).getExtensionPoint() ==
                        ExtensionPoint.OR
                    ) {
                        v =
                            getExtensionHandler(consumeToken())
                                .createAstNode(v, and(true));
                        break;
                    }
                default:
                    return v;
            }
        }
    }

    /**
     * and := eq (&lt;AND&gt; eq)*
     */
    protected AstNode and(boolean required)
        throws Scanner.ScanException, ParseException {
        AstNode v = eq(required);
        if (v == null) {
            return null;
        }
        while (true) {
            switch (token.getSymbol()) {
                case AND:
                    consumeToken();
                    v = createAstBinary(v, eq(true), AstBinary.AND);
                    break;
                case EXTENSION:
                    if (
                        getExtensionHandler(token).getExtensionPoint() ==
                        ExtensionPoint.AND
                    ) {
                        v =
                            getExtensionHandler(consumeToken())
                                .createAstNode(v, eq(true));
                        break;
                    }
                default:
                    return v;
            }
        }
    }

    /**
     * eq := cmp (&lt;EQ&gt; cmp | &lt;NE&gt; cmp)*
     */
    protected AstNode eq(boolean required)
        throws Scanner.ScanException, ParseException {
        AstNode v = cmp(required);
        if (v == null) {
            return null;
        }
        while (true) {
            switch (token.getSymbol()) {
                case EQ:
                    consumeToken();
                    v = createAstBinary(v, cmp(true), AstBinary.EQ);
                    break;
                case NE:
                    consumeToken();
                    v = createAstBinary(v, cmp(true), AstBinary.NE);
                    break;
                case EXTENSION:
                    if (
                        getExtensionHandler(token).getExtensionPoint() ==
                        ExtensionPoint.EQ
                    ) {
                        v =
                            getExtensionHandler(consumeToken())
                                .createAstNode(v, cmp(true));
                        break;
                    }
                default:
                    return v;
            }
        }
    }

    /**
     * cmp := add (&lt;LT&gt; add | &lt;LE&gt; add | &lt;GE&gt; add | &lt;GT&gt; add)*
     */
    protected AstNode cmp(boolean required)
        throws Scanner.ScanException, ParseException {
        AstNode v = add(required);
        if (v == null) {
            return null;
        }
        while (true) {
            switch (token.getSymbol()) {
                case LT:
                    consumeToken();
                    v = createAstBinary(v, add(true), AstBinary.LT);
                    break;
                case LE:
                    consumeToken();
                    v = createAstBinary(v, add(true), AstBinary.LE);
                    break;
                case GE:
                    consumeToken();
                    v = createAstBinary(v, add(true), AstBinary.GE);
                    break;
                case GT:
                    consumeToken();
                    v = createAstBinary(v, add(true), AstBinary.GT);
                    break;
                case EXTENSION:
                    if (
                        getExtensionHandler(token).getExtensionPoint() ==
                        ExtensionPoint.CMP
                    ) {
                        v =
                            getExtensionHandler(consumeToken())
                                .createAstNode(v, add(true));
                        break;
                    }
                default:
                    return v;
            }
        }
    }

    /**
     * add := add (&lt;PLUS&gt; mul | &lt;MINUS&gt; mul)*
     */
    protected AstNode add(boolean required)
        throws Scanner.ScanException, ParseException {
        AstNode v = mul(required);
        if (v == null) {
            return null;
        }
        while (true) {
            switch (token.getSymbol()) {
                case PLUS:
                    consumeToken();
                    v = createAstBinary(v, mul(true), AstBinary.ADD);
                    break;
                case MINUS:
                    consumeToken();
                    v = createAstBinary(v, mul(true), AstBinary.SUB);
                    break;
                case EXTENSION:
                    if (
                        getExtensionHandler(token).getExtensionPoint() ==
                        ExtensionPoint.ADD
                    ) {
                        v =
                            getExtensionHandler(consumeToken())
                                .createAstNode(v, mul(true));
                        break;
                    }
                default:
                    return v;
            }
        }
    }

    /**
     * mul := unary (&lt;MUL&gt; unary | &lt;DIV&gt; unary | &lt;MOD&gt; unary)*
     */
    protected AstNode mul(boolean required)
        throws Scanner.ScanException, ParseException {
        AstNode v = unary(required);
        if (v == null) {
            return null;
        }
        while (true) {
            switch (token.getSymbol()) {
                case MUL:
                    consumeToken();
                    v = createAstBinary(v, unary(true), AstBinary.MUL);
                    break;
                case DIV:
                    consumeToken();
                    v = createAstBinary(v, unary(true), AstBinary.DIV);
                    break;
                case MOD:
                    consumeToken();
                    v = createAstBinary(v, unary(true), AstBinary.MOD);
                    break;
                case EXTENSION:
                    if (
                        getExtensionHandler(token).getExtensionPoint() ==
                        ExtensionPoint.MUL
                    ) {
                        v =
                            getExtensionHandler(consumeToken())
                                .createAstNode(v, unary(true));
                        break;
                    }
                default:
                    return v;
            }
        }
    }

    /**
     * unary := &lt;NOT&gt; unary | &lt;MINUS&gt; unary | &lt;EMPTY&gt; unary | value
     */
    protected AstNode unary(boolean required)
        throws Scanner.ScanException, ParseException {
        AstNode v = null;
        switch (token.getSymbol()) {
            case NOT:
                consumeToken();
                v = createAstUnary(unary(true), AstUnary.NOT);
                break;
            case MINUS:
                consumeToken();
                v = createAstUnary(unary(true), AstUnary.NEG);
                break;
            case EMPTY:
                consumeToken();
                v = createAstUnary(unary(true), AstUnary.EMPTY);
                break;
            case EXTENSION:
                if (
                    getExtensionHandler(token).getExtensionPoint() ==
                    ExtensionPoint.UNARY
                ) {
                    v =
                        getExtensionHandler(consumeToken())
                            .createAstNode(unary(true));
                    break;
                }
            default:
                v = value();
        }
        if (v == null && required) {
            fail(EXPR_FIRST);
        }
        return v;
    }

    /**
     * value := (nonliteral | literal) (&lt;DOT&gt; &lt;IDENTIFIER&gt; | &lt;LBRACK&gt; expr &lt;RBRACK&gt;)*
     */
    protected AstNode value() throws Scanner.ScanException, ParseException {
        boolean lvalue = true;
        AstNode v = nonliteral();
        if (v == null) {
            v = literal();
            if (v == null) {
                return null;
            }
            lvalue = false;
        }
        while (true) {
            switch (token.getSymbol()) {
                case DOT:
                    consumeToken();
                    String name = consumeToken(Scanner.Symbol.IDENTIFIER)
                        .getImage();
                    AstDot dot = createAstDot(v, name, lvalue);
                    if (
                        token.getSymbol() == Scanner.Symbol.LPAREN &&
                        context.isEnabled(Builder.Feature.METHOD_INVOCATIONS)
                    ) {
                        v = createAstMethod(dot, params());
                    } else {
                        v = dot;
                    }
                    break;
                case LBRACK:
                    consumeToken();
                    AstNode property = expr(true);
                    boolean strict = !context.isEnabled(
                        Builder.Feature.NULL_PROPERTIES
                    );
                    consumeToken(Scanner.Symbol.RBRACK);
                    AstBracket bracket = createAstBracket(
                        v,
                        property,
                        lvalue,
                        strict
                    );
                    if (
                        token.getSymbol() == Scanner.Symbol.LPAREN &&
                        context.isEnabled(Builder.Feature.METHOD_INVOCATIONS)
                    ) {
                        v = createAstMethod(bracket, params());
                    } else {
                        v = bracket;
                    }
                    break;
                default:
                    return v;
            }
        }
    }

    /**
     * nonliteral := &lt;IDENTIFIER&gt; | function | &lt;LPAREN&gt; expr &lt;RPAREN&gt;
     * function   := (&lt;IDENTIFIER&gt; &lt;COLON&gt;)? &lt;IDENTIFIER&gt; &lt;LPAREN&gt; list? &lt;RPAREN&gt;
     */
    protected AstNode nonliteral()
        throws Scanner.ScanException, ParseException {
        AstNode v = null;
        switch (token.getSymbol()) {
            case IDENTIFIER:
                String name = consumeToken().getImage();
                if (
                    token.getSymbol() == Scanner.Symbol.COLON &&
                    lookahead(0).getSymbol() == Scanner.Symbol.IDENTIFIER &&
                    lookahead(1).getSymbol() == Scanner.Symbol.LPAREN
                ) { // ns:f(...)
                    consumeToken();
                    name += ":" + token.getImage();
                    consumeToken();
                }
                if (token.getSymbol() == Scanner.Symbol.LPAREN) { // function
                    v = function(name, params());
                } else { // identifier
                    v = identifier(name);
                }
                break;
            case LPAREN:
                consumeToken();
                v = expr(true);
                consumeToken(Scanner.Symbol.RPAREN);
                v = new AstNested(v);
                break;
        }
        return v;
    }

    /**
     * params := &lt;LPAREN&gt; (expr (&lt;COMMA&gt; expr)*)? &lt;RPAREN&gt;
     */
    protected AstParameters params()
        throws Scanner.ScanException, ParseException {
        consumeToken(Scanner.Symbol.LPAREN);
        List<AstNode> l = Collections.emptyList();
        AstNode v = expr(false);
        if (v != null) {
            l = new ArrayList<AstNode>();
            l.add(v);
            while (token.getSymbol() == Scanner.Symbol.COMMA) {
                consumeToken();
                l.add(expr(true));
            }
        }
        consumeToken(Scanner.Symbol.RPAREN);
        return new AstParameters(l);
    }

    /**
     * literal := &lt;TRUE&gt; | &lt;FALSE&gt; | &lt;STRING&gt; | &lt;INTEGER&gt; | &lt;FLOAT&gt; | &lt;NULL&gt;
     */
    protected AstNode literal() throws Scanner.ScanException, ParseException {
        AstNode v = null;
        switch (token.getSymbol()) {
            case TRUE:
                v = new AstBoolean(true);
                consumeToken();
                break;
            case FALSE:
                v = new AstBoolean(false);
                consumeToken();
                break;
            case STRING:
                v = new AstString(token.getImage());
                consumeToken();
                break;
            case INTEGER:
                v = new AstNumber(parseInteger(token.getImage()));
                consumeToken();
                break;
            case FLOAT:
                v = new AstNumber(parseFloat(token.getImage()));
                consumeToken();
                break;
            case NULL:
                v = new AstNull();
                consumeToken();
                break;
            case EXTENSION:
                if (
                    getExtensionHandler(token).getExtensionPoint() ==
                    ExtensionPoint.LITERAL
                ) {
                    v = getExtensionHandler(consumeToken()).createAstNode();
                    break;
                }
        }
        return v;
    }

    protected final AstFunction function(String name, AstParameters params) {
        if (functions.isEmpty()) {
            functions = new ArrayList<FunctionNode>(4);
        }
        AstFunction function = createAstFunction(
            name,
            functions.size(),
            params
        );
        functions.add(function);
        return function;
    }

    protected final AstIdentifier identifier(String name) {
        if (identifiers.isEmpty()) {
            identifiers = new ArrayList<IdentifierNode>(4);
        }
        AstIdentifier identifier = createAstIdentifier(
            name,
            identifiers.size()
        );
        identifiers.add(identifier);
        return identifier;
    }
}
