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

import java.math.BigDecimal;
import java.math.BigInteger;

import org.activiti.engine.impl.javax.el.ELException;

/**
 * Arithmetic Operations as specified in chapter 1.7.
 * 
 * @author Christoph Beck
 */
public class NumberOperations {
	private final static Long LONG_ZERO = Long.valueOf(0L);

	private final static boolean isDotEe(String value) {
		int length = value.length();
		for (int i = 0; i < length; i++) {
			switch (value.charAt(i)) {
				case '.':
				case 'E':
				case 'e': return true;
			}
		}
		return false;
	}

	private final static boolean isDotEe(Object value) {
		return value instanceof String && isDotEe((String)value);
	}

	private static final boolean isFloatOrDouble(Object value) {
		return value instanceof Float || value instanceof Double;
	}

	private static final boolean isFloatOrDoubleOrDotEe(Object value) {
		return isFloatOrDouble(value) || isDotEe(value);
	}

	private static final boolean isBigDecimalOrBigInteger(Object value) {
		return value instanceof BigDecimal || value instanceof BigInteger;
	}

	private static final boolean isBigDecimalOrFloatOrDoubleOrDotEe(Object value) {
		return value instanceof BigDecimal || isFloatOrDoubleOrDotEe(value);
	}

	public static final Number add(TypeConverter converter, Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return LONG_ZERO;
		}
		if (o1 instanceof BigDecimal || o2 instanceof BigDecimal) {
			return converter.convert(o1, BigDecimal.class).add(converter.convert(o2, BigDecimal.class));
		}
		if (isFloatOrDoubleOrDotEe(o1) || isFloatOrDoubleOrDotEe(o2)) {
			if (o1 instanceof BigInteger || o2 instanceof BigInteger) {
				return converter.convert(o1, BigDecimal.class).add(converter.convert(o2, BigDecimal.class));
			}
			return converter.convert(o1, Double.class) + converter.convert(o2, Double.class);
		}
		if (o1 instanceof BigInteger || o2 instanceof BigInteger) {
			return converter.convert(o1, BigInteger.class).add(converter.convert(o2, BigInteger.class));
		}
		return converter.convert(o1, Long.class) + converter.convert(o2, Long.class);
	}

	public static final Number sub(TypeConverter converter, Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return LONG_ZERO;
		}
		if (o1 instanceof BigDecimal || o2 instanceof BigDecimal) {
			return converter.convert(o1, BigDecimal.class).subtract(converter.convert(o2, BigDecimal.class));
		}
		if (isFloatOrDoubleOrDotEe(o1) || isFloatOrDoubleOrDotEe(o2)) {
			if (o1 instanceof BigInteger || o2 instanceof BigInteger) {
				return converter.convert(o1, BigDecimal.class).subtract(converter.convert(o2, BigDecimal.class));
			}
			return converter.convert(o1, Double.class) - converter.convert(o2, Double.class);
		}
		if (o1 instanceof BigInteger || o2 instanceof BigInteger) {
			return converter.convert(o1, BigInteger.class).subtract(converter.convert(o2, BigInteger.class));
		}
		return converter.convert(o1, Long.class) - converter.convert(o2, Long.class);
	}

	public static final Number mul(TypeConverter converter, Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return LONG_ZERO;
		}
		if (o1 instanceof BigDecimal || o2 instanceof BigDecimal) {
			return converter.convert(o1, BigDecimal.class).multiply(converter.convert(o2, BigDecimal.class));
		}
		if (isFloatOrDoubleOrDotEe(o1) || isFloatOrDoubleOrDotEe(o2)) {
			if (o1 instanceof BigInteger || o2 instanceof BigInteger) {
				return converter.convert(o1, BigDecimal.class).multiply(converter.convert(o2, BigDecimal.class));
			}
			return converter.convert(o1, Double.class) * converter.convert(o2, Double.class);
		}
		if (o1 instanceof BigInteger || o2 instanceof BigInteger) {
			return converter.convert(o1, BigInteger.class).multiply(converter.convert(o2, BigInteger.class));
		}
		return converter.convert(o1, Long.class) * converter.convert(o2, Long.class);
	}

	public static final Number div(TypeConverter converter, Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return LONG_ZERO;
		}
		if (isBigDecimalOrBigInteger(o1) || isBigDecimalOrBigInteger(o2)) {
			return converter.convert(o1, BigDecimal.class).divide(converter.convert(o2, BigDecimal.class), BigDecimal.ROUND_HALF_UP);
		}
		return converter.convert(o1, Double.class) / converter.convert(o2, Double.class);
	}

	public static final Number mod(TypeConverter converter, Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return LONG_ZERO;
		}
		if (isBigDecimalOrFloatOrDoubleOrDotEe(o1) || isBigDecimalOrFloatOrDoubleOrDotEe(o2)) {
			return converter.convert(o1, Double.class) % converter.convert(o2, Double.class);
		}
		if (o1 instanceof BigInteger || o2 instanceof BigInteger) {
			return converter.convert(o1, BigInteger.class).remainder(converter.convert(o2, BigInteger.class));
		}
		return converter.convert(o1, Long.class) % converter.convert(o2, Long.class);
	}

	public static final Number neg(TypeConverter converter, Object value) {
		if (value == null) {
			return LONG_ZERO;
		}
		if (value instanceof BigDecimal) {
			return ((BigDecimal)value).negate();
		}
		if (value instanceof BigInteger) {
			return ((BigInteger)value).negate();
		}
		if (value instanceof Double) {
			return Double.valueOf(-((Double)value).doubleValue());
		}
		if (value instanceof Float) {
			return Float.valueOf(-((Float)value).floatValue());
		}
		if (value instanceof String) {
			if (isDotEe((String)value)) {
				return Double.valueOf(-converter.convert(value, Double.class).doubleValue());
			}
			return Long.valueOf(-converter.convert(value, Long.class).longValue());
		}
		if (value instanceof Long) {
			return Long.valueOf(-((Long)value).longValue());
		}
		if (value instanceof Integer) {
			return Integer.valueOf(-((Integer)value).intValue());
		}
		if (value instanceof Short) {
			return Short.valueOf((short)-((Short)value).shortValue());
		}
		if (value instanceof Byte) {
			return Byte.valueOf((byte)-((Byte)value).byteValue());
		}
		throw new ELException(LocalMessages.get("error.negate", value.getClass()));
	}
}
