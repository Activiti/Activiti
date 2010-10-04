/*
 * Based on JUEL 2.2.1 code, 2006-2009 Odysseus Software GmbH
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
package org.activiti.el.juel.tree.impl.ast;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for de.odysseus.el.tree");
		//$JUnit-BEGIN$
		suite.addTestSuite(AstBinaryTest.class);
		suite.addTestSuite(AstBooleanTest.class);
		suite.addTestSuite(AstBracketTest.class);
		suite.addTestSuite(AstChoiceTest.class);
		suite.addTestSuite(AstCompositeTest.class);
		suite.addTestSuite(AstDotTest.class);
		suite.addTestSuite(AstEvalTest.class);
		suite.addTestSuite(AstFunctionTest.class);
		suite.addTestSuite(AstIdentifierTest.class);
		suite.addTestSuite(AstMethodTest.class);
		suite.addTestSuite(AstNestedTest.class);
		suite.addTestSuite(AstNullTest.class);
		suite.addTestSuite(AstNumberTest.class);
		suite.addTestSuite(AstStringTest.class);
		suite.addTestSuite(AstTextTest.class);
		suite.addTestSuite(AstUnaryTest.class);
		//$JUnit-END$
		return suite;
	}

}
