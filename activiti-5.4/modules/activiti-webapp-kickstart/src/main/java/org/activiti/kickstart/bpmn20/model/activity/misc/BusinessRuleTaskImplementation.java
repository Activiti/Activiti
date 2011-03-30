/**
 * Copyright (c) 2009
 * Philipp Giese, Sven Wagner-Boysen
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.activiti.kickstart.bpmn20.model.activity.misc;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;


/**
 * <p>Java class for tUserTaskImplementation.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="tUserTaskImplementation">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="unspecified"/>
 *     &lt;enumeration value="other"/>
 *     &lt;enumeration value="webService"/>
 *     &lt;enumeration value="humanTaskWebService"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlEnum
public enum BusinessRuleTaskImplementation {

    @XmlEnumValue("unspecified")
    UNSPECIFIED("unspecified"),
    @XmlEnumValue("other")
    OTHER("other"),
    @XmlEnumValue("webService")
    WEB_SERVICE("webService"),
    @XmlEnumValue("businessRuleTaskWebService")
    BUSINESS_RULE_TASK_WEB_SERVICE("businessRuleTaskWebService");
    private final String value;

    BusinessRuleTaskImplementation(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BusinessRuleTaskImplementation fromValue(String v) {
        for (BusinessRuleTaskImplementation c: BusinessRuleTaskImplementation.values()) {
            if (c.value.equalsIgnoreCase(v)) {
                return c;
            } else {
            	/* Return default value otherwise */
            	return OTHER;
            }
            	
        }
        throw new IllegalArgumentException(v);
    }

}


