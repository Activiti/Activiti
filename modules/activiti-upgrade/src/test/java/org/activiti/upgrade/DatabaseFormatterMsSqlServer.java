package org.activiti.upgrade;


public class DatabaseFormatterMsSqlServer extends DatabaseFormatter {

  @Override
  public String formatBinary(byte[] bytes) {
    StringBuffer sb = new StringBuffer();
    sb.append("0x");
    appendBytesInHex(sb, bytes);
    return sb.toString();
  }
  
  @Override
  public String formatBoolean(boolean b) {
    return (b ? "1" : "0");
  }

}
