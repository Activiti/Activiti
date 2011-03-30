


package org.activiti.kickstart.bpmn20.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.activiti.kickstart.bpmn20.model.data_object.Message;
import org.activiti.kickstart.bpmn20.model.misc.Signal;


/**
 * <p>Java class for tRootElement complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="tRootElement">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/bpmn20}tBaseElement">
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tRootElement")
@XmlSeeAlso(value = {
//    TItemDefinition.class,
//    Category.class,
//    Collaboration.class,
//    TEndPoint.class,
//    TPartnerRole.class,
//    TPartnerEntity.class,
    Signal.class,
//    EventDefinition.class,
//    TError.class,
//    TResource.class,
//    TInterface.class,
//    TCorrelationProperty.class,
    Message.class,

    CallableElement.class//,

//    TEscalation.class
})
public abstract class RootElement
    extends BaseElement
{
	
	public RootElement() {}
	
	public RootElement(RootElement rootElement) {
		super(rootElement);
	}


}
