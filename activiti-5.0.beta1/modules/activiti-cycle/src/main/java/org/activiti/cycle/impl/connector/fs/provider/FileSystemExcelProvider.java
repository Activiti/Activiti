package org.activiti.cycle.impl.connector.fs.provider;

import org.activiti.cycle.ContentType;

public class FileSystemExcelProvider extends FileSystemBinaryProvider {

  public static final String NAME = "Excel";

  public FileSystemExcelProvider() {
    super(NAME, ContentType.MS_EXCEL);
  }
}
