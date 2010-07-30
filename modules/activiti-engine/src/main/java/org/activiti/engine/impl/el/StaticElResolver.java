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

package org.activiti.engine.impl.el;

import java.beans.FeatureDescriptor;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import javax.el.ELContext;
import javax.el.ELResolver;

/**
 * Implementation of an {@link ELResolver} that resolves expressions with the a
 * constant map as context.
 * 
 * @author Dave Syer
 */
public class StaticElResolver extends ELResolver {

  private String prefix = "static:";

  private Map<String, Object> map = Collections.emptyMap();
  
  public void setMap(Map<String, Object> map) {
    this.map = map;
  }

  public void setPrefix(String prefix) {
    if (!prefix.endsWith(":")) {
      prefix = prefix + ":";
    }
    this.prefix = prefix;
  }

  public Object getValue(ELContext context, Object base, Object property) {

    if (base == null) {
      // according to javadoc, can only be a String
      String variable = (String) property;
      if (variable.startsWith(prefix)) {
        variable = variable.substring(prefix.length());
      }
      if (map.containsKey(variable)) {
        context.setPropertyResolved(true);
        return map.get(variable);
      }
    }

    return null;
  }

  public boolean isReadOnly(ELContext context, Object base, Object property) {
    return true;
  }

  public void setValue(ELContext context, Object base, Object property, Object value) {
  }

  public Class< ? > getCommonPropertyType(ELContext context, Object arg) {
    return Object.class;
  }

  public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object arg) {
    return null;
  }

  public Class< ? > getType(ELContext context, Object arg1, Object arg2) {
    return Object.class;
  }

}
