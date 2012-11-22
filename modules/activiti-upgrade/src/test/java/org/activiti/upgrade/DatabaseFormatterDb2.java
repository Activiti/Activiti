package org.activiti.upgrade;

import java.util.Date;


public class DatabaseFormatterDb2 extends DatabaseFormatter {

  @Override
  public String formatBinary(byte[] bytes) {
    StringBuffer sb = new StringBuffer();
    sb.append("blob(X'");
    appendBytesInHex(sb, bytes);
    sb.append("')");
    return sb.toString();
  }

  @Override
  public String formatBoolean(boolean b) {
    return (b ? "1" : "0");
  }

  @Override
  public String formatDate(Date date) {
    StringBuffer sb = new StringBuffer();
    sb.append("TIMESTAMP (");
    sb.append(defaultDateFormat.format(date));
    sb.append(")");
    return sb.toString();
  }
}
