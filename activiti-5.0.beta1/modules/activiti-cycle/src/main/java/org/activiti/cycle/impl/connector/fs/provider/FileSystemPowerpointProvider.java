package org.activiti.cycle.impl.connector.fs.provider;

import org.activiti.cycle.ContentType;

public class FileSystemPowerpointProvider extends FileSystemBinaryProvider {

  public static final String NAME = "Powerpoint";

  public FileSystemPowerpointProvider() {
    super(NAME, ContentType.MS_POWERPOINT);
  }
}
