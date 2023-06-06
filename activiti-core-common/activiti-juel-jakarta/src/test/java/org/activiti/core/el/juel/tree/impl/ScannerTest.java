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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Arrays;
import org.activiti.core.el.juel.test.TestCase;
import org.junit.jupiter.api.Test;

public class ScannerTest extends TestCase {

    void assertEquals(Object[] a1, Object[] a2) {
        assertTrue(Arrays.equals(a1, a2));
    }

    Scanner.Symbol[] symbols(String expression) throws Scanner.ScanException {
        ArrayList<Scanner.Symbol> list = new ArrayList<Scanner.Symbol>();
        Scanner scanner = new Scanner(expression);
        Scanner.Token token = scanner.next();
        while (token.getSymbol() != Scanner.Symbol.EOF) {
            list.add(token.getSymbol());
            token = scanner.next();
        }
        return list.toArray(new Scanner.Symbol[list.size()]);
    }

    @Test
    public void testInteger() throws Scanner.ScanException {
        Scanner.Symbol[] expected = {
            Scanner.Symbol.START_EVAL_DYNAMIC,
            Scanner.Symbol.INTEGER,
            Scanner.Symbol.END_EVAL,
        };

        assertEquals(expected, symbols("${0}"));
        assertEquals(expected, symbols("${1}"));
        assertEquals(expected, symbols("${01234567890}"));
    }

    @Test
    public void testFloat() throws Scanner.ScanException {
        Scanner.Symbol[] expected = {
            Scanner.Symbol.START_EVAL_DYNAMIC,
            Scanner.Symbol.FLOAT,
            Scanner.Symbol.END_EVAL,
        };

        assertEquals(expected, symbols("${0.}"));
        assertEquals(expected, symbols("${023456789.}"));
        assertEquals(expected, symbols("${.0}"));
        assertEquals(expected, symbols("${.023456789}"));
        assertEquals(expected, symbols("${0.0}"));

        assertEquals(expected, symbols("${0e0}"));
        assertEquals(expected, symbols("${0E0}"));
        assertEquals(expected, symbols("${0e+0}"));
        assertEquals(expected, symbols("${0E+0}"));
        assertEquals(expected, symbols("${0e+0}"));
        assertEquals(expected, symbols("${0E+0}"));

        assertEquals(expected, symbols("${.0e0}"));
        assertEquals(expected, symbols("${.0E0}"));
        assertEquals(expected, symbols("${.0e+0}"));
        assertEquals(expected, symbols("${.0E+0}"));
        assertEquals(expected, symbols("${.0e-0}"));
        assertEquals(expected, symbols("${.0E-0}"));

        assertEquals(expected, symbols("${0.e0}"));
        assertEquals(expected, symbols("${0.E0}"));
        assertEquals(expected, symbols("${0.e+0}"));
        assertEquals(expected, symbols("${0.E+0}"));
        assertEquals(expected, symbols("${0.e-0}"));
        assertEquals(expected, symbols("${0.E-0}"));

        assertEquals(expected, symbols("${0.0e0}"));
        assertEquals(expected, symbols("${0.0E0}"));
        assertEquals(expected, symbols("${0.0e+0}"));
        assertEquals(expected, symbols("${0.0E+0}"));
        assertEquals(expected, symbols("${0.0e-0}"));
        assertEquals(expected, symbols("${0.0E-0}"));
    }

    @Test
    public void testString() throws Scanner.ScanException {
        Scanner.Symbol[] expected = {
            Scanner.Symbol.START_EVAL_DYNAMIC,
            Scanner.Symbol.STRING,
            Scanner.Symbol.END_EVAL,
        };

        assertEquals(expected, symbols("${'foo'}"));
        assertEquals(expected, symbols("${'f\"o'}"));
        assertEquals(expected, symbols("${'f\\'o'}"));
        try {
            symbols("${'f\\oo'}");
            fail();
        } catch (Scanner.ScanException e) {}
        try {
            symbols("${'f\\\"o'}");
            fail();
        } catch (Scanner.ScanException e) {}
        try {
            symbols("${'foo");
            fail();
        } catch (Scanner.ScanException e) {}

        assertEquals(expected, symbols("${\"foo\"}"));
        assertEquals(expected, symbols("${\"f\\\"o\"}"));
        assertEquals(expected, symbols("${\"f'o\"}"));
        try {
            symbols("${\"f\\oo\"}");
            fail();
        } catch (Scanner.ScanException e) {}
        try {
            symbols("${\"f\\'o\"}");
            fail();
        } catch (Scanner.ScanException e) {}
        try {
            symbols("${\"foo");
            fail();
        } catch (Scanner.ScanException e) {}
    }

    @Test
    public void testKeywords() throws Scanner.ScanException {
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.NULL,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${null}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.TRUE,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${true}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.FALSE,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${false}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.EMPTY,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${empty}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.DIV,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${div}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.MOD,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${mod}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.NOT,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${not}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.AND,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${and}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.OR,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${or}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.LE,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${le}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.LT,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${lt}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.EQ,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${eq}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.NE,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${ne}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.GE,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${ge}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.GT,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${gt}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.INSTANCEOF,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${instanceof}")
        );

        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.IDENTIFIER,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${xnull}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.IDENTIFIER,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${nullx}")
        );
    }

    @Test
    public void testOperators() throws Scanner.ScanException {
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.MUL,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${*}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.DIV,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${/}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.MOD,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${%}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.PLUS,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${+}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.MINUS,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${-}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.QUESTION,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${?}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.COLON,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${:}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.LBRACK,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${[}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.RBRACK,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${]}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.LPAREN,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${(}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.RPAREN,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${)}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.COMMA,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${,}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.DOT,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${.}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.AND,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${&&}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.OR,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${||}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.NOT,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${!}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.LE,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${<=}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.LT,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${<}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.EQ,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${==}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.NE,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${!=}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.GE,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${>=}")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.GT,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${>}")
        );

        try {
            symbols("${&)");
            fail();
        } catch (Scanner.ScanException e) {}
        try {
            symbols("${|)");
            fail();
        } catch (Scanner.ScanException e) {}
        try {
            symbols("${=)");
            fail();
        } catch (Scanner.ScanException e) {}
    }

    @Test
    public void testWhitespace() throws Scanner.ScanException {
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.END_EVAL,
            },
            symbols("${\t\n\r }")
        );
    }

    @Test
    public void testIdentifier() throws Scanner.ScanException {
        Scanner.Symbol[] expected = {
            Scanner.Symbol.START_EVAL_DYNAMIC,
            Scanner.Symbol.IDENTIFIER,
            Scanner.Symbol.END_EVAL,
        };

        assertEquals(expected, symbols("${foo}"));
        assertEquals(expected, symbols("${foo_1}"));
    }

    @Test
    public void testText() throws Scanner.ScanException {
        Scanner.Symbol[] expected = { Scanner.Symbol.TEXT };

        assertEquals(expected, symbols("foo"));
        assertEquals(expected, symbols("foo\\"));
        assertEquals(expected, symbols("foo\\$"));
        assertEquals(expected, symbols("foo\\#"));
        assertEquals(expected, symbols("foo\\${"));
        assertEquals(expected, symbols("foo\\#{"));
        assertEquals(expected, symbols("\\${foo}"));
        assertEquals(expected, symbols("\\${foo}"));
    }

    @Test
    public void testMixed() throws Scanner.ScanException {
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.TEXT,
                Scanner.Symbol.START_EVAL_DYNAMIC,
            },
            symbols("foo${")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.IDENTIFIER,
            },
            symbols("${bar")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.END_EVAL,
                Scanner.Symbol.TEXT,
            },
            symbols("${}bar")
        );
        assertEquals(
            new Scanner.Symbol[] {
                Scanner.Symbol.TEXT,
                Scanner.Symbol.START_EVAL_DYNAMIC,
                Scanner.Symbol.END_EVAL,
                Scanner.Symbol.TEXT,
            },
            symbols("foo${}bar")
        );
    }

    @Test
    public void testDeferred() throws Scanner.ScanException {
        assertEquals(
            new Scanner.Symbol[] { Scanner.Symbol.START_EVAL_DEFERRED },
            symbols("#{")
        );
    }
}
