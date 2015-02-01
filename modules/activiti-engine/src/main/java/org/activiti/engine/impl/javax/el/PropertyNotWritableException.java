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
package org.activiti.engine.impl.javax.el;

/**
 * Thrown when a property could not be written to while setting the value on a
 * {@link ValueExpression}. For example, this could be triggered by trying to set a map value on an
 * unmodifiable map.
 */
public class PropertyNotWritableException extends ELException {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a PropertyNotWritableException with no detail message.
	 */
	public PropertyNotWritableException() {
		super();
	}

	/**
	 * Creates a PropertyNotWritableException with the provided detail message.
	 * 
	 * @param message
	 *            the detail message
	 */
	public PropertyNotWritableException(String message) {
		super(message);
	}

	/**
	 * Creates a PropertyNotWritableException with the given root cause.
	 * 
	 * @param cause
	 *            the originating cause of this exception
	 */
	public PropertyNotWritableException(Throwable cause) {
		super(cause);
	}

	/**
	 * Creates a PropertyNotWritableException with the given detail message and root cause.
	 * 
	 * @param message
	 *            the detail message
	 * @param cause
	 *            the originating cause of this exception
	 */
	public PropertyNotWritableException(String message, Throwable cause) {
		super(message, cause);
	}
}
