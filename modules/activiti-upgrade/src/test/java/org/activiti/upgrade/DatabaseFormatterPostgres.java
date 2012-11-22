package org.activiti.upgrade;


public class DatabaseFormatterPostgres extends DatabaseFormatter {

  @Override
  public String formatBinary(byte[] bytes) {
    StringBuffer sb = new StringBuffer();
    sb.append("E'\\\\x");
    appendBytesInHex(sb, bytes);
    sb.append("'");
    return sb.toString();
  }

}
