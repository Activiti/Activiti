package org.activiti.upgrade;


public class DatabaseFormatterDb2 extends DatabaseFormatter {

  @Override
  public String formatBinary(byte[] bytes) {
    StringBuffer sb = new StringBuffer();
    sb.append("blob(X'");
    appendBytesInHex(sb, bytes);
    sb.append("')");
    return sb.toString();
  }

}
