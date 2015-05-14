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

import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.activiti.engine.impl.javax.el.ELException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Type Conversions as described in EL 2.1 specification (section 1.17).
 */
public class TypeConverterImpl implements TypeConverter {
	private static final long serialVersionUID = 1L;

	protected Boolean coerceToBoolean(Object value) {
		if (value == null || "".equals(value)) {
			return Boolean.FALSE;
		}
		if (value instanceof Boolean) {
			return (Boolean)value;
		}
		if (value instanceof String) {
			return Boolean.valueOf((String)value);
		}
		throw new ELException(LocalMessages.get("error.coerce.type", value.getClass(), Boolean.class));
	}

	protected Character coerceToCharacter(Object value) {
		if (value == null || "".equals(value)) {
			return Character.valueOf((char)0);
		}
		if (value instanceof Character) {
			return (Character)value;
		}
		if (value instanceof Number) {
			return Character.valueOf((char)((Number)value).shortValue());
		}
		if (value instanceof String) {
			return Character.valueOf(((String)value).charAt(0));
		}
		throw new ELException(LocalMessages.get("error.coerce.type", value.getClass(), Character.class));
	}

	protected BigDecimal coerceToBigDecimal(Object value) {
		if (value == null || "".equals(value)) {
			return BigDecimal.valueOf(0l);
		}
		if (value instanceof BigDecimal) {
			return (BigDecimal)value;
		}
		if (value instanceof BigInteger) {
			return new BigDecimal((BigInteger)value);
		}
		if (value instanceof Number) {
			return new BigDecimal(((Number)value).doubleValue());
		}
		if (value instanceof String) {
			try {
				return new BigDecimal((String)value);
			} catch (NumberFormatException e) {
				throw new ELException(LocalMessages.get("error.coerce.value", value, BigDecimal.class));
			}
		}
		if (value instanceof Character) {
			return new BigDecimal((short)((Character)value).charValue());
		}
		throw new ELException(LocalMessages.get("error.coerce.type", value.getClass(), BigDecimal.class));
	}

	protected BigInteger coerceToBigInteger(Object value) {
		if (value == null || "".equals(value)) {
			return BigInteger.valueOf(0l);
		}
		if (value instanceof BigInteger) {
			return (BigInteger)value;
		}
		if (value instanceof BigDecimal) {
			return ((BigDecimal)value).toBigInteger();
		}
		if (value instanceof Number) {
			return BigInteger.valueOf(((Number)value).longValue());
		}
		if (value instanceof String) {
			try {
				return new BigInteger((String)value);
			} catch (NumberFormatException e) {
				throw new ELException(LocalMessages.get("error.coerce.value", value, BigInteger.class));
			}
		}
		if (value instanceof Character) {
			return BigInteger.valueOf((short)((Character)value).charValue());
		}
		throw new ELException(LocalMessages.get("error.coerce.type", value.getClass(), BigInteger.class));
	}

	protected Double coerceToDouble(Object value) {
		if (value == null || "".equals(value)) {
			return Double.valueOf(0);
		}
		if (value instanceof Double) {
			return (Double)value;
		}
		if (value instanceof Number) {
			return Double.valueOf(((Number)value).doubleValue());
		}
		if (value instanceof String) {
			try {
				return Double.valueOf((String)value);
			} catch (NumberFormatException e) {
				throw new ELException(LocalMessages.get("error.coerce.value", value, Double.class));
			}
		}
		if (value instanceof Character) {
			return Double.valueOf((short)((Character)value).charValue());
		}
		throw new ELException(LocalMessages.get("error.coerce.type", value.getClass(), Double.class));
	}

	protected Float coerceToFloat(Object value) {
		if (value == null || "".equals(value)) {
			return Float.valueOf(0);
		}
		if (value instanceof Float) {
			return (Float)value;
		}
		if (value instanceof Number) {
			return Float.valueOf(((Number)value).floatValue());
		}
		if (value instanceof String) {
			try {
				return Float.valueOf((String)value);
			} catch (NumberFormatException e) {
				throw new ELException(LocalMessages.get("error.coerce.value", value, Float.class));
			}
		}
		if (value instanceof Character) {
			return Float.valueOf((short)((Character)value).charValue());
		}
		throw new ELException(LocalMessages.get("error.coerce.type", value.getClass(), Float.class));
	}

	protected Long coerceToLong(Object value) {
		if (value == null || "".equals(value)) {
			return Long.valueOf(0l);
		}
		if (value instanceof Long) {
			return (Long)value;
		}
		if (value instanceof Number) {
			return Long.valueOf(((Number)value).longValue());
		}
		if (value instanceof String) {
			try {
				return Long.valueOf((String)value);
			} catch (NumberFormatException e) {
				throw new ELException(LocalMessages.get("error.coerce.value", value, Long.class));
			}
		}
		if (value instanceof Character) {
			return Long.valueOf((short)((Character)value).charValue());
		}
		throw new ELException(LocalMessages.get("error.coerce.type", value.getClass(), Long.class));
	}

	protected Integer coerceToInteger(Object value) {
		if (value == null || "".equals(value)) {
			return Integer.valueOf(0);
		}
		if (value instanceof Integer) {
			return (Integer)value;
		}
		if (value instanceof Number) {
			return Integer.valueOf(((Number)value).intValue());
		}
		if (value instanceof String) {
			try {
				return Integer.valueOf((String)value);
			} catch (NumberFormatException e) {
				throw new ELException(LocalMessages.get("error.coerce.value", value, Integer.class));
			}
		}
		if (value instanceof Character) {
			return Integer.valueOf((short)((Character)value).charValue());
		}
		throw new ELException(LocalMessages.get("error.coerce.type", value.getClass(), Integer.class));
	}

	protected Short coerceToShort(Object value) {
		if (value == null || "".equals(value)) {
			return Short.valueOf((short)0);
		}
		if (value instanceof Short) {
			return (Short)value;
		}
		if (value instanceof Number) {
			return Short.valueOf(((Number)value).shortValue());
		}
		if (value instanceof String) {
			try {
				return Short.valueOf((String)value);
			} catch (NumberFormatException e) {
				throw new ELException(LocalMessages.get("error.coerce.value", value, Short.class));
			}
		}
		if (value instanceof Character) {
			return Short.valueOf((short)((Character)value).charValue());
		}
		throw new ELException(LocalMessages.get("error.coerce.type", value.getClass(), Short.class));
	}

	protected Byte coerceToByte(Object value) {
		if (value == null || "".equals(value)) {
			return Byte.valueOf((byte)0);
		}
		if (value instanceof Byte) {
			return (Byte)value;
		}
		if (value instanceof Number) {
			return Byte.valueOf(((Number)value).byteValue());
		}
		if (value instanceof String) {
			try {
				return Byte.valueOf((String)value);
			} catch (NumberFormatException e) {
				throw new ELException(LocalMessages.get("error.coerce.value", value, Byte.class));
			}
		}
		if (value instanceof Character) {
			return Byte.valueOf(Short.valueOf((short)((Character)value).charValue()).byteValue());
		}
		throw new ELException(LocalMessages.get("error.coerce.type", value.getClass(), Byte.class));
	}

	protected String coerceToString(Object value) {
		if (value == null) {
			return "";
		}
		if (value instanceof String) {
			return (String)value;
		}
		if (value instanceof Enum<?>) {
			return ((Enum<?>)value).name();
		}
    if (value instanceof Date) {
      DateTimeFormatter fmt = ISODateTimeFormat.dateTime();
      DateTime dt = new DateTime(value);
      return fmt.print(dt);
    }
		return value.toString();
	}

	@SuppressWarnings("unchecked")
	protected <T extends Enum<T>> T coerceToEnum(Object value, Class<T> type) {
		if (value == null || "".equals(value)) {
			return null;
		}
		if (type.isInstance(value)) {
			return (T)value;
		}
		if (value instanceof String) {
			try {
				return Enum.valueOf(type, (String)value);
			} catch (IllegalArgumentException e) {
				throw new ELException(LocalMessages.get("error.coerce.value", value, type));
			}
		}
		throw new ELException(LocalMessages.get("error.coerce.type", value.getClass(), type));
	}

	protected Object coerceStringToType(String value, Class<?> type) {
		PropertyEditor editor = PropertyEditorManager.findEditor(type);
		if (editor == null) {
			if ("".equals(value)) {
				return null;
			}
			throw new ELException(LocalMessages.get("error.coerce.type", String.class, type));
		} else {
			if ("".equals(value)) {
				try {
					editor.setAsText(value);
				} catch (IllegalArgumentException e) {
					return null;
				}
			} else {
				try {
					editor.setAsText(value);
				} catch (IllegalArgumentException e) {
					throw new ELException(LocalMessages.get("error.coerce.value", value, type));
				}
			}
			return editor.getValue();
		}
	}

	@SuppressWarnings("unchecked")
	protected Object coerceToType(Object value, Class<?> type) {
		if (type == String.class) {
			return coerceToString(value);
		}
		if (type == Long.class || type == long.class) {
			return coerceToLong(value);
		}
		if (type == Double.class || type == double.class) {
			return coerceToDouble(value);
		}
		if (type == Boolean.class || type == boolean.class) {
			return coerceToBoolean(value);
		}
		if (type == Integer.class || type == int.class) {
			return coerceToInteger(value);
		}
		if (type == Float.class || type == float.class) {
			return coerceToFloat(value);
		}
		if (type == Short.class || type == short.class) {
			return coerceToShort(value);
		}
		if (type == Byte.class || type == byte.class) {
			return coerceToByte(value);
		}
		if (type == Character.class || type == char.class) {
			return coerceToCharacter(value);
		}
		if (type == BigDecimal.class) {
			return coerceToBigDecimal(value);
		}
		if (type == BigInteger.class) {
			return coerceToBigInteger(value);
		}
		if (type.getSuperclass() == Enum.class) {
			return coerceToEnum(value, (Class<? extends Enum>)type);
		}
		if (value == null || value.getClass() == type || type.isInstance(value)) {
			return value;
		}
		if (value instanceof String) {
			return coerceStringToType((String)value, type);
		}
		throw new ELException(LocalMessages.get("error.coerce.type", value.getClass(), type));
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass().equals(getClass());
	}

	@Override
	public int hashCode() {
		return getClass().hashCode();
	}

	@SuppressWarnings("unchecked")
	public <T> T convert(Object value, Class<T> type) throws ELException {
		return (T)coerceToType(value, type);
	}
}
