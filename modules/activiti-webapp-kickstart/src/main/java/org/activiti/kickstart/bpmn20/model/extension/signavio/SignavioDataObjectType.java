/**
 * Copyright (c) 2010
 * Signavio, Sven Wagner-Boysen
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

package org.activiti.kickstart.bpmn20.model.extension.signavio;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;

/**
 * Indicates whether a DataObject is of type IT-System or ProcessParticipant
 * 
 * @author Sven Wagner-Boysen
 *
 */
@XmlEnum
public enum SignavioDataObjectType {
	@XmlEnumValue("Default")
	DEFAULT("default"),
	@XmlEnumValue("IT-System")
	ITSYSTEM("it-system"),
	@XmlEnumValue("ProcessParticipant")
	PROCESSPARTICIPANT("processparticipant");
	
	private final String value;
	
	SignavioDataObjectType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }
    
    public static SignavioDataObjectType fromValue(String v) {
        for (SignavioDataObjectType c: SignavioDataObjectType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }
}
