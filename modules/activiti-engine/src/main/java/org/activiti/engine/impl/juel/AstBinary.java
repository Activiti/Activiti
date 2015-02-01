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


public class AstBinary extends AstRightValue {
	public interface Operator {
		public Object eval(Bindings bindings, ELContext context, AstNode left, AstNode right);		
	}
	public static abstract class SimpleOperator implements Operator {
		public Object eval(Bindings bindings, ELContext context, AstNode left, AstNode right) {
			return apply(bindings, left.eval(bindings, context), right.eval(bindings, context));
		}

		protected abstract Object apply(TypeConverter converter, Object o1, Object o2);
	}
	public static final Operator ADD = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return NumberOperations.add(converter, o1, o2); }
		@Override public String toString() { return "+"; }
	};
	public static final Operator AND = new Operator() {
		public Object eval(Bindings bindings, ELContext context, AstNode left, AstNode right) {
			Boolean l = bindings.convert(left.eval(bindings, context), Boolean.class);
			return Boolean.TRUE.equals(l) ? bindings.convert(right.eval(bindings, context), Boolean.class) : Boolean.FALSE;
		}
		@Override public String toString() { return "&&"; }
	};
	public static final Operator DIV = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return NumberOperations.div(converter, o1, o2); }
		@Override public String toString() { return "/"; }
	};
	public static final Operator EQ = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return BooleanOperations.eq(converter, o1, o2); }
		@Override public String toString() { return "=="; }
	};
	public static final Operator GE = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return BooleanOperations.ge(converter, o1, o2); }
		@Override public String toString() { return ">="; }
	};
	public static final Operator GT = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return BooleanOperations.gt(converter, o1, o2); }
		@Override public String toString() { return ">"; }
	};
	public static final Operator LE = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return BooleanOperations.le(converter, o1, o2); }
		@Override public String toString() { return "<="; }
	};
	public static final Operator LT = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return BooleanOperations.lt(converter, o1, o2); }
		@Override public String toString() { return "<"; }
	};
	public static final Operator MOD = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return NumberOperations.mod(converter, o1, o2); }
		@Override public String toString() { return "%"; }
	};
	public static final Operator MUL = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return NumberOperations.mul(converter, o1, o2); }
		@Override public String toString() { return "*"; }
	};
	public static final Operator NE = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return BooleanOperations.ne(converter, o1, o2); }
		@Override public String toString() { return "!="; }
	};
	public static final Operator OR = new Operator() {
		public Object eval(Bindings bindings, ELContext context, AstNode left, AstNode right) {
			Boolean l = bindings.convert(left.eval(bindings, context), Boolean.class);
			return Boolean.TRUE.equals(l) ? Boolean.TRUE : bindings.convert(right.eval(bindings, context), Boolean.class);
		}
		@Override public String toString() { return "||"; }
	};
	public static final Operator SUB = new SimpleOperator() {
		@Override public Object apply(TypeConverter converter, Object o1, Object o2) { return NumberOperations.sub(converter, o1, o2); }
		@Override public String toString() { return "-"; }
	};

	private final Operator operator;
	private final AstNode left, right;

	public AstBinary(AstNode left, AstNode right, Operator operator) {
		this.left = left;
		this.right = right;
		this.operator = operator;
	}

	public Operator getOperator() {
		return operator;
	}

	@Override 
	public Object eval(Bindings bindings, ELContext context) {
		return operator.eval(bindings, context, left, right);
	}

	@Override
	public String toString() {
		return "'" + operator.toString() + "'";
	}	

	@Override 
	public void appendStructure(StringBuilder b, Bindings bindings) {
		left.appendStructure(b, bindings);
		b.append(' ');
		b.append(operator);
		b.append(' ');
		right.appendStructure(b, bindings);
	}

	public int getCardinality() {
		return 2;
	}

	public AstNode getChild(int i) {
		return i == 0 ? left : i == 1 ? right : null;
	}
}
