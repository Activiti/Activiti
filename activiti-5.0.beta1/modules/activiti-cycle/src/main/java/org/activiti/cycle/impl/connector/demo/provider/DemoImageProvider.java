package org.activiti.cycle.impl.connector.demo.provider;

import org.activiti.cycle.ContentType;

public class DemoImageProvider extends DemoProvider {

  public DemoImageProvider() {
    super("Image", ContentType.JPEG, false);
  }

}
