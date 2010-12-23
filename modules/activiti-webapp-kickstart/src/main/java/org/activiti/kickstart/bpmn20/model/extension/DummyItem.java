package org.activiti.kickstart.bpmn20.model.extension;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Dummy Element of a property list item. At least one concrete class must
 * exists for the abstract {@link PropertyListItem}
 * 
 * @author Sven Wagner-Boysen
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class DummyItem extends PropertyListItem {

}
