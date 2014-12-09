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
package org.activiti.workflow.simple.alfresco.conversion.script;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionConstants;
import org.activiti.workflow.simple.alfresco.conversion.AlfrescoConversionUtil;
import org.activiti.workflow.simple.alfresco.model.M2Model;
import org.activiti.workflow.simple.alfresco.model.M2Namespace;
import org.activiti.workflow.simple.definition.form.FormPropertyDefinition;
import org.activiti.workflow.simple.exception.SimpleWorkflowException;

/**
 * A builder to create property-references that are resolved at conversion-time to reference
 * the correct fully-qualified property name, based on the "simple" name given in the {@link FormPropertyDefinition}.
 * 
 * @author Frederik Heremans
 */
public class PropertyReference {
  protected static final String REFERENCE_PREFIX = "{{";
  protected static final String REFERENCE_SUFFIX = "}}";
  protected static final String USERNAME_REFERENCE_EXPRESSION ="$'{'{0}.properties.userName'}'";
  protected static final String GROUPNAME_REFERENCE_EXPRESSION ="$'{'{0}.properties.authorityName'}'";
  protected static final String REFERENCE_EXPRESSION ="$'{'{0}'}'";
  protected static final Pattern REFERENCE_EXPRESSION_PATTERN = Pattern.compile("\\{\\{[^\\{](.*?)\\}\\}");
  
	protected String propertyName;
	protected String additionalProperties;
	protected boolean isPrefixed = false;
	
	public PropertyReference(String propertyName) {
		this(propertyName, null);
  }
	
	public PropertyReference(String propertyName, String additionalProperties) {
		this.propertyName = propertyName;
		this.additionalProperties = additionalProperties;
		if(additionalProperties != null && additionalProperties.isEmpty()) {
			this.additionalProperties = null;
		}
		
		isPrefixed = propertyName != null && propertyName.contains(":");
  }
	
	public String getPlaceholder() {
		return REFERENCE_PREFIX + propertyName + REFERENCE_SUFFIX;
	}
	
	public String getPropertyName() {
	  return propertyName;
  }
	
	public String getUsernameReferenceExpression(String namespacePrefix) {
		String qualifiedName = getQualifiedName(namespacePrefix);
    return MessageFormat.format(USERNAME_REFERENCE_EXPRESSION, getVariableName(qualifiedName));
	}
	
	public String getGroupReferenceExpression(String namespacePrefix) {
		String qualifiedName = getQualifiedName(namespacePrefix);
    return MessageFormat.format(GROUPNAME_REFERENCE_EXPRESSION, getVariableName(qualifiedName));
	}
	
	public String getPropertyReferenceExpression(String namespacePrefix) {
		String qualifiedName = getQualifiedName(namespacePrefix);
    return MessageFormat.format(REFERENCE_EXPRESSION, getVariableName(qualifiedName));
	}
	
	public String getVariableReference(String namespacePrefix) {
		String qualifiedName = getQualifiedName(namespacePrefix);
    return getVariableName(qualifiedName);
	}
	
	public void validate(M2Model model) {
		String namespacePrefix = model.getNamespaces().get(0).getPrefix();
		
		boolean valid = false;
		if(propertyName.contains(":")) {
			// Already prefixed. Check if we import that namespace...
			for(M2Namespace imported : model.getImports()) {
				if(propertyName.startsWith(imported.getPrefix())) {
					valid = true;
					break;
				}
			}
			
			if(!valid && !propertyName.startsWith(AlfrescoConversionConstants.WORKFLOW_NAMESPACE.getPrefix()) && 
				!propertyName.startsWith(AlfrescoConversionConstants.BPM_NAMESPACE.getPrefix())) {
				throw new SimpleWorkflowException("Property reference: " + this.getPlaceholder() + " references a property for a namespace that is not imported in the BPM-model");
			} else {
				valid = true;
			}
		}
		
		if(!valid && !model.isContainedInModel(getQualifiedName(namespacePrefix))) {
			throw new SimpleWorkflowException("Property reference: " + this.getPlaceholder() + " does not reference an existing property.");
		}
	}
	
	public static boolean isPropertyReference(String value) {
		return value != null && value.startsWith(REFERENCE_PREFIX)
				&& value.endsWith(REFERENCE_SUFFIX);
	}
	
	public static boolean containsPropertyReference(String value) {
		if(value == null) {
			return false;
		}
		return value != null && value.indexOf(REFERENCE_PREFIX) >= 0
				&& value.indexOf(REFERENCE_SUFFIX) >= 0;
	}
	
	public static String replaceAllPropertyReferencesInString(String refrence, String namespacePrefix, List<PropertyReference> foundReferences, boolean wrapAsExpression) {
	  String newValue = refrence;
	  
	  List<String> references = new ArrayList<String>();
	  Matcher regexMatcher = REFERENCE_EXPRESSION_PATTERN.matcher(refrence);
	  while (regexMatcher.find()) {
	  	references.add(regexMatcher.group());
	  } 
	  
	  if(!references.isEmpty()) {
	  	PropertyReference ref = null;
	  	for(String reference : references) {
	  		ref = PropertyReference.createReference(reference);
	  		if(ref != null) {
	  			foundReferences.add(ref);
	  			if(wrapAsExpression) {
	  				newValue = newValue.replace(reference, ref.getPropertyReferenceExpression(namespacePrefix));
	  			} else {
	  				newValue = newValue.replace(reference, ref.getVariableReference(namespacePrefix));
	  			}
	  		}
	  	}
	  }
	  return newValue;
	}
	
	/**
	 * @return a {@link PropertyReference} based on the given placeholder string. Returns null in case
	 * the given string is not a valid property reference.
	 */
	public static PropertyReference createReference(String referenceString) {
		PropertyReference ref = null;
		if(isPropertyReference(referenceString)) {
			String raw = referenceString.replace(REFERENCE_PREFIX, "").replace(REFERENCE_SUFFIX, "");
			if(raw.contains(".")) {
				String additional = null;
				String propertyName = raw;
				int indexOf = raw.indexOf(".");
				if(indexOf > 0) {
					propertyName = raw.substring(0, indexOf);
					additional = raw.substring(indexOf + 1);
				}
				ref = new PropertyReference(propertyName, additional);
			} else {
				ref = new PropertyReference(raw);
			}
		}
		return ref;
	}
	
	protected String getVariableName(String propertyName) {
		if(propertyName != null) {
			if(additionalProperties != null) {
				return propertyName.replace(':', '_') + "." + additionalProperties;
			} else {
				return propertyName.replace(':', '_');
			}
		}
		return null;
	}
	
	protected String getQualifiedName(String namespacePrefix) {
		String qualifiedName = null;
		if(isPrefixed) {
			qualifiedName = propertyName;
		} else {
			qualifiedName = AlfrescoConversionUtil.getQualifiedName(namespacePrefix, propertyName);
		}
		return qualifiedName;
	}
}
