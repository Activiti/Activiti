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

import java.io.Serializable;

import org.activiti.engine.impl.javax.el.ELException;

public interface TypeConverter extends Serializable {
	/**
	 * Default conversions as from JSR245.
	 */
	public static final TypeConverter DEFAULT = new TypeConverterImpl();
	
	/**
	 * Convert the given input value to the specified target type.
	 * @param value input value
	 * @param type target type
	 * @return conversion result
	 */
	public <T> T convert(Object value, Class<T> type) throws ELException;
}
