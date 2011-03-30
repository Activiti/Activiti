/**
 * Original code from Signavio core components.
 * Adapted to add support for Activiti custom extensions.
 * 
 * @author Joram Barrez
 * 
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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.model.activity.Task;
import org.activiti.kickstart.bpmn20.model.activity.misc.UserTaskImplementation;
import org.activiti.kickstart.bpmn20.model.activity.resource.Rendering;

/**
 * <p>
 * Java class for tUserTask complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within
 * this class.
 * 
 * <pre>
 * &lt;complexType name="tUserTask">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tTask">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/bpmn20}rendering" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="implementation" type="{http://www.omg.org/bpmn20}tUserTaskImplementation" default="unspecified" />
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tUserTask", propOrder = { "rendering" })
public class UserTask extends Task {

  @XmlAttribute(name = "formKey", namespace = "http://activiti.org/bpmn")
  protected String formKey;

  public void setFormKey(String formKey) {
    this.formKey = formKey;
  }

  public String getFormKey() {
    return formKey;
  }

  /* Constructors */

  /**
   * Default constructor
   */
  public UserTask() {
  }

  /**
   * Copy constructor based on a {@link UserTask}
   * 
   * @param task
   */
  public UserTask(UserTask task) {
    super(task);

    this.getRendering().addAll(task.getRendering());
    this.setImplementation(task.getImplementation());
  }

  protected List<Rendering> rendering;
  @XmlAttribute
  protected UserTaskImplementation implementation;

  /**
   * Gets the value of the rendering property.
   * 
   * <p>
   * This accessor method returns a reference to the live list, not a snapshot.
   * Therefore any modification you make to the returned list will be present
   * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
   * for the rendering property.
   * 
   * <p>
   * For example, to add a new item, do as follows:
   * 
   * <pre>
   * getRendering().add(newItem);
   * </pre>
   * 
   * 
   * <p>
   * Objects of the following type(s) are allowed in the list {@link Rendering }
   * 
   * 
   */
  public List<Rendering> getRendering() {
    if (rendering == null) {
      rendering = new ArrayList<Rendering>();
    }
    return this.rendering;
  }

  /**
   * Gets the value of the implementation property.
   * 
   * @return possible object is {@link UserTaskImplementation }
   * 
   */
  public UserTaskImplementation getImplementation() {
    if (implementation == null) {
      return UserTaskImplementation.UNSPECIFIED;
    } else {
      return implementation;
    }
  }

  /**
   * Sets the value of the implementation property.
   * 
   * @param value
   *          allowed object is {@link UserTaskImplementation }
   * 
   */
  public void setImplementation(UserTaskImplementation value) {
    this.implementation = value;
  }

}
