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

package org.activiti.cycle.impl.connector.signavio.transform.signavio;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Removes Signavio glossary links
 * 
 * Example: glossary://fe916161287140b09dae4203537a7dea/Anschlussadresse\nextrahieren;;
 * 
 * @author Falko Menge <falko.menge@camunda.com>
 */
public class GlossaryLinkRemoval extends OryxShapeNameTransformation {

  public static final Pattern GLOSSARY_LINK_PATTERN = Pattern.compile("glossary://[a-z0-9]+/([^;]+);;");

  public String transformName(String name) {
    String newName = name;
    Matcher matcher = GLOSSARY_LINK_PATTERN.matcher(name);
    if (matcher.matches()) {
      newName = matcher.group(1);
    }
    return newName;
  }

}
