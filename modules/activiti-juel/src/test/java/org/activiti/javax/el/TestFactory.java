package org.activiti.javax.el;

import java.util.Properties;

import org.activiti.javax.el.ELContext;
import org.activiti.javax.el.ELException;
import org.activiti.javax.el.ExpressionFactory;
import org.activiti.javax.el.MethodExpression;
import org.activiti.javax.el.ValueExpression;

public class TestFactory extends ExpressionFactory {
	final Properties properties;
	public TestFactory() {
		this.properties = null;
	}
	public TestFactory(Properties properties) {
		this.properties = properties;
	}
	@Override
	public Object coerceToType(Object obj, Class<?> targetType) {
		if (targetType.isPrimitive()) {
			if (targetType == boolean.class) {
				if (obj == null) {
					return false;
				}
				targetType = Boolean.class;
			}
			if (targetType == int.class) {
				if (obj == null) {
					return 0;
				}
				targetType = Integer.class;
			}
			if (targetType == long.class) {
				if (obj == null) {
					return 0;
				}
				targetType = Long.class;
			}
		}
		if (targetType.isInstance(obj)) {
			return obj;
		}
		if (targetType == String.class) {
			return obj == null ? "" : obj.toString();
		}
		if (obj.getClass() == String.class) {
			if (targetType == Boolean.class) {
				return Boolean.valueOf(obj.toString());
			}
			if (targetType == Integer.class) {
				return Integer.valueOf(obj.toString());
			}
		}
		if (obj instanceof Number) {
			if (targetType == Integer.class) {
				return ((Number)obj).intValue();
			}
			if (targetType == Long.class) {
				return ((Number)obj).longValue();
			}
			if (targetType == Double.class) {
				return ((Number)obj).doubleValue();
			}
		}
		throw new ELException("Test conversion failed: " + obj.getClass() + " --> " + targetType);
	}
	@Override
	public MethodExpression createMethodExpression(ELContext context, String expression,
			Class<?> expectedReturnType, Class<?>[] expectedParamTypes) {
		throw new UnsupportedOperationException();
	};
	@Override
	public ValueExpression createValueExpression(ELContext context, String expression, Class<?> expectedType) {
		throw new UnsupportedOperationException();
	}
	@Override
	public ValueExpression createValueExpression(Object instance, Class<?> expectedType) {
		throw new UnsupportedOperationException();
	}
}
