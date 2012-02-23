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

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class LocalMessages {
	private static final String BUNDLE_NAME = "org.activiti.engine.impl.juel.misc.LocalStrings";
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	public static String get(String key, Object... args) {
		String template = null;
		try {
			template = RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			StringBuilder b = new StringBuilder();
			try {
				b.append(RESOURCE_BUNDLE.getString("message.unknown"));
				b.append(": ");
			} catch (MissingResourceException e2) {}
			b.append(key);
			if (args != null && args.length > 0) {
				b.append("(");
				b.append(args[0]);
				for (int i = 1; i < args.length; i++) {
					b.append(", ");
					b.append(args[i]);
				}
				b.append(")");
			}
			return b.toString();
		}
		return MessageFormat.format(template, args);
	}
}
