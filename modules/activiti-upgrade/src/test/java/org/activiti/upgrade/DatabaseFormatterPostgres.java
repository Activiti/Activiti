package org.activiti.upgrade;


public class DatabaseFormatterPostgres extends DatabaseFormatter {

  @Override
  public String formatBinary(byte[] bytes) {
    StringBuffer sb = new StringBuffer();
    sb.append("decode('");
    appendBytesInHex(sb, bytes);
    sb.append("', 'hex')");
    return sb.toString();
  }

}
