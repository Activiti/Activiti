/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.activiti.app.util;

import javax.xml.stream.XMLInputFactory;

/**
 * @author Joram Barrez
 */
public class XmlUtil {

	/**
	 * 'safe' is here reflecting:
	 * http://activiti.org/userguide/index.html#advanced.safe.bpmn.xml
	 */
	public static XMLInputFactory createSafeXmlInputFactory() {
		XMLInputFactory xif = XMLInputFactory.newInstance();
		if (xif.isPropertySupported(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES)) {
			xif.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
			        false);
		}

		if (xif.isPropertySupported(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES)) {
			xif.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
			        false);
		}

		if (xif.isPropertySupported(XMLInputFactory.SUPPORT_DTD)) {
			xif.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		}
		return xif;
	}

}
