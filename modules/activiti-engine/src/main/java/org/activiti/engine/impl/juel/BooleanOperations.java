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
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.activiti.engine.impl.javax.el.ELException;

public class BooleanOperations {
	private final static Set<Class<? extends Number>> SIMPLE_INTEGER_TYPES = new HashSet<Class<? extends Number>>();
	private final static Set<Class<? extends Number>> SIMPLE_FLOAT_TYPES = new HashSet<Class<? extends Number>>();
	
	static {
		SIMPLE_INTEGER_TYPES.add(Byte.class);
		SIMPLE_INTEGER_TYPES.add(Short.class);
		SIMPLE_INTEGER_TYPES.add(Integer.class);
		SIMPLE_INTEGER_TYPES.add(Long.class);
		SIMPLE_FLOAT_TYPES.add(Float.class);
		SIMPLE_FLOAT_TYPES.add(Double.class);
	}

	@SuppressWarnings("unchecked")
	private static final boolean lt0(TypeConverter converter, Object o1, Object o2) {
		Class<?> t1 = o1.getClass();
		Class<?> t2 = o2.getClass();
		if (BigDecimal.class.isAssignableFrom(t1) || BigDecimal.class.isAssignableFrom(t2)) {
			return converter.convert(o1, BigDecimal.class).compareTo(converter.convert(o2, BigDecimal.class)) < 0;
		}
		if (SIMPLE_FLOAT_TYPES.contains(t1) || SIMPLE_FLOAT_TYPES.contains(t2)) {
			return converter.convert(o1, Double.class) < converter.convert(o2, Double.class);
		}
		if (BigInteger.class.isAssignableFrom(t1) || BigInteger.class.isAssignableFrom(t2)) {
			return converter.convert(o1, BigInteger.class).compareTo(converter.convert(o2, BigInteger.class)) < 0;
		}
		if (SIMPLE_INTEGER_TYPES.contains(t1) || SIMPLE_INTEGER_TYPES.contains(t2)) {
			return converter.convert(o1, Long.class) < converter.convert(o2, Long.class);
		}
		if (t1 == String.class || t2 == String.class) {
			return converter.convert(o1, String.class).compareTo(converter.convert(o2, String.class)) < 0;
		}
		if (o1 instanceof Comparable) {
			return ((Comparable)o1).compareTo(o2) < 0;
		}
		if (o2 instanceof Comparable) {
			return ((Comparable)o2).compareTo(o1) > 0;
		}
		throw new ELException(LocalMessages.get("error.compare.types", o1.getClass(), o2.getClass()));
	}

	@SuppressWarnings("unchecked")
	private static final boolean gt0(TypeConverter converter, Object o1, Object o2) {		
		Class<?> t1 = o1.getClass();
		Class<?> t2 = o2.getClass();
		if (BigDecimal.class.isAssignableFrom(t1) || BigDecimal.class.isAssignableFrom(t2)) {
			return converter.convert(o1, BigDecimal.class).compareTo(converter.convert(o2, BigDecimal.class)) > 0;
		}
		if (SIMPLE_FLOAT_TYPES.contains(t1) || SIMPLE_FLOAT_TYPES.contains(t2)) {
			return converter.convert(o1, Double.class) > converter.convert(o2, Double.class);
		}
		if (BigInteger.class.isAssignableFrom(t1) || BigInteger.class.isAssignableFrom(t2)) {
			return converter.convert(o1, BigInteger.class).compareTo(converter.convert(o2, BigInteger.class)) > 0;
		}
		if (SIMPLE_INTEGER_TYPES.contains(t1) || SIMPLE_INTEGER_TYPES.contains(t2)) {
			return converter.convert(o1, Long.class) > converter.convert(o2, Long.class);
		}
		if (t1 == String.class || t2 == String.class) {
			return converter.convert(o1, String.class).compareTo(converter.convert(o2, String.class)) > 0;
		}
		if (o1 instanceof Comparable) {
			return ((Comparable)o1).compareTo(o2) > 0;
		}
		if (o2 instanceof Comparable) {
			return ((Comparable)o2).compareTo(o1) < 0;
		}
		throw new ELException(LocalMessages.get("error.compare.types", o1.getClass(), o2.getClass()));
	}

	public static final boolean lt(TypeConverter converter, Object o1, Object o2) {
		if (o1 == o2) {
			return false;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		return lt0(converter, o1, o2);
	}

	public static final boolean gt(TypeConverter converter, Object o1, Object o2) {
		if (o1 == o2) {
			return false;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		return gt0(converter, o1, o2);
	}

	public static final boolean ge(TypeConverter converter, Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		return !lt0(converter, o1, o2);
	}

	public static final boolean le(TypeConverter converter, Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		return !gt0(converter, o1, o2);
	}

	public static final boolean eq(TypeConverter converter, Object o1, Object o2) {
		if (o1 == o2) {
			return true;
		}
		if (o1 == null || o2 == null) {
			return false;
		}
		Class<?> t1 = o1.getClass();
		Class<?> t2 = o2.getClass();
		if (BigDecimal.class.isAssignableFrom(t1) || BigDecimal.class.isAssignableFrom(t2)) {
			return converter.convert(o1, BigDecimal.class).equals(converter.convert(o2, BigDecimal.class));
		}
		if (SIMPLE_FLOAT_TYPES.contains(t1) || SIMPLE_FLOAT_TYPES.contains(t2)) {
			return converter.convert(o1, Double.class).equals(converter.convert(o2, Double.class));
		}
		if (BigInteger.class.isAssignableFrom(t1) || BigInteger.class.isAssignableFrom(t2)) {
			return converter.convert(o1, BigInteger.class).equals(converter.convert(o2, BigInteger.class));
		}
		if (SIMPLE_INTEGER_TYPES.contains(t1) || SIMPLE_INTEGER_TYPES.contains(t2)) {
			return converter.convert(o1, Long.class).equals(converter.convert(o2, Long.class));
		}
		if (t1 == Boolean.class || t2 == Boolean.class) {
			return converter.convert(o1, Boolean.class).equals(converter.convert(o2, Boolean.class));
		}
		if (o1 instanceof Enum<?>) {
			return o1 == converter.convert(o2, o1.getClass());
		}
		if (o2 instanceof Enum<?>) {
			return converter.convert(o1, o2.getClass()) == o2;
		}
		if (t1 == String.class || t2 == String.class) {
			return converter.convert(o1, String.class).equals(converter.convert(o2, String.class));
		}
		return o1.equals(o2);
	}

	public static final boolean ne(TypeConverter converter, Object o1, Object o2) {
		return !eq(converter, o1, o2);
	}

	public static final boolean empty(TypeConverter converter, Object o) {
		if (o == null || "".equals(o)) {
			return true;
		}
		if (o instanceof Object[]) {
			return ((Object[])o).length == 0;
		}
		if (o instanceof Map<?,?>) {
			return ((Map<?,?>)o).isEmpty();
		}
		if (o instanceof Collection<?>) {
			return ((Collection<?>)o).isEmpty();
		}
		return false;
	}
}
