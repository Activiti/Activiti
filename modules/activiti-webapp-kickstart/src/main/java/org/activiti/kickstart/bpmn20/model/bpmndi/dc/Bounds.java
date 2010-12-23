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
package org.activiti.kickstart.bpmn20.model.bpmndi.dc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for Bounds complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Bounds">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="x" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="y" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="width" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="height" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Bounds")
public class Bounds {

    @XmlAttribute(name = "x", required = true)
    protected double x;
    @XmlAttribute(name = "y", required = true)
    protected double y;
    @XmlAttribute(name = "width", required = true)
    protected double width;
    @XmlAttribute(name = "height", required = true)
    protected double height;

    /**
     * Gets the value of the x property.
     * 
     */
    public double getX() {
        return x;
    }

    /**
     * Sets the value of the x property.
     * 
     */
    public void setX(double value) {
        this.x = value;
    }

    /**
     * Gets the value of the y property.
     * 
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the value of the y property.
     * 
     */
    public void setY(double value) {
        this.y = value;
    }

    /**
     * Gets the value of the width property.
     * 
     */
    public double getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     */
    public void setWidth(double value) {
        this.width = value;
    }

    /**
     * Gets the value of the height property.
     * 
     */
    public double getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     */
    public void setHeight(double value) {
        this.height = value;
    }

}
