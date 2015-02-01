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

import java.util.EventObject;

/**
 * An event which indicates that an {@link ELContext} has been created. The source object is the
 * ELContext that was created.
 */
public class ELContextEvent extends EventObject {
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an ELContextEvent object to indicate that an ELContext has been created.
	 * 
	 * @param source
	 *            the ELContext that was created.
	 */
	public ELContextEvent(ELContext source) {
		super(source);
	}

	/**
	 * Returns the ELContext that was created. This is a type-safe equivalent of the
	 * java.util.EventObject.getSource() method.
	 * 
	 * @return the ELContext that was created.
	 */
	public ELContext getELContext() {
		return (ELContext) getSource();
	}
}
