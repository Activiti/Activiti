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

package org.activiti.kickstart.bpmn20.model.activity.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.model.activity.Task;

/**
 * <p>
 * Java class for tScriptTask complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="tScriptTask">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tTask">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/bpmn20}script" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="scriptLanguage" type="{http://www.w3.org/2001/XMLSchema}anyURI" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tScriptTask", propOrder = { "script" })
public class ScriptTask extends Task {

  @XmlAttribute(name = "resultVariableName", namespace = "http://activiti.org/bpmn")
  protected String resultVariableName;

  public void setResultVariableName(String resultVariableName) {
    this.resultVariableName = resultVariableName;
  }

  public String getResultVariableName() {
    return resultVariableName;
  }

  /**
   * Default constructor
   */
  public ScriptTask() {

  }

  /**
   * Copy constructor
   * 
   * @param scriptTask
   *          The {@link ScriptTask} to copy.
   */
  public ScriptTask(ScriptTask scriptTask) {
    super(scriptTask);
    this.setScript(scriptTask.getScript());
    this.setScriptFormat(scriptTask.getScriptFormat());
  }

  @XmlElement
  protected String script;

  @XmlAttribute
  @XmlSchemaType(name = "anyURI")
  protected String scriptFormat;

  /**
   * Gets the value of the script property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getScript() {
    return script;
  }

  /**
   * Sets the value of the script property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setScript(String value) {
    this.script = value;
  }

  /**
   * Gets the value of the scriptLanguage property.
   * 
   * @return possible object is {@link String }
   * 
   */
  public String getScriptFormat() {
    return scriptFormat;
  }

  /**
   * Sets the value of the scriptLanguage property.
   * 
   * @param value
   *          allowed object is {@link String }
   * 
   */
  public void setScriptFormat(String value) {
    this.scriptFormat = value;
  }

}
