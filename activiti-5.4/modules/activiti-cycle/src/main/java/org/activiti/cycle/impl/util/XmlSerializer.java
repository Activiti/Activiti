package org.activiti.cycle.impl.util;

import com.thoughtworks.xstream.XStream;

/**
 * Used for serializing / unserializing objects to/from xml.
 */
public class XmlSerializer {

  public static String serializeObject(Object o) {
    XStream xstream = new XStream();
    return xstream.toXML(o);
  }

  public static Object unSerializeObject(String serializedObject) {
    XStream xstream = new XStream();
    return xstream.fromXML(serializedObject);
  }

}
