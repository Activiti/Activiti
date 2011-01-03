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
package org.activiti.engine.impl.juel;

import org.activiti.engine.impl.javax.el.ELContext;
import org.activiti.engine.impl.javax.el.ELException;


public class AstChoice extends AstRightValue {
	private final AstNode question, yes, no;
	
	public AstChoice(AstNode question, AstNode yes, AstNode no) {
		this.question = question;
		this.yes = yes;
		this.no = no;
	}

	@Override 
	public Object eval(Bindings bindings, ELContext context) throws ELException {
		Boolean value = bindings.convert(question.eval(bindings, context), Boolean.class);
		return value.booleanValue() ? yes.eval(bindings, context) : no.eval(bindings, context);
	}

	@Override
	public String toString() {
		return "?";
	}	

	@Override 
	public void appendStructure(StringBuilder b, Bindings bindings) {
		question.appendStructure(b, bindings);
		b.append(" ? ");
		yes.appendStructure(b, bindings);
		b.append(" : ");
		no.appendStructure(b, bindings);
	}

	public int getCardinality() {
		return 3;
	}

	public AstNode getChild(int i) {
		return i == 0 ? question : i == 1 ? yes : i == 2 ? no : null;
	}
}
