package org.activiti.cycle.impl.connector.fs.provider;

import org.activiti.cycle.ContentType;

public class FileSystemWordProvider extends FileSystemBinaryProvider {

  public static final String NAME = "Word";

  public FileSystemWordProvider() {
    super(NAME, ContentType.MS_WORD);
  }
}
