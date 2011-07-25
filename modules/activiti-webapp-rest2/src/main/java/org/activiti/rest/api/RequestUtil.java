package org.activiti.rest.api;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.activiti.engine.ActivitiException;
import org.restlet.data.Form;

public class RequestUtil {
  
  private static final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");

  public static boolean getBoolean(Form form, String name, boolean defaultValue) {
    boolean value = defaultValue;
    if(form.getValues(name) != null) {
      value = Boolean.valueOf(form.getValues(name));
    }
    return value;
  }
  
  public static int getInteger(Form form, String name, int defaultValue) {
    int value = defaultValue;
    if(form.getValues(name) != null) {
      value = Integer.valueOf(form.getValues(name));
    }
    return value;
  }
  
  public static Date getDate(Form form, String name) {
    Date value = null;
    if(form.getValues(name) != null) {
      
      String input = form.getValues(name);
      
      //this is zero time so we need to add that TZ indicator for 
      if (input.endsWith("Z")) {
        input = input.substring( 0, input.length() - 1) + "GMT-00:00";
      } else {
        int inset = 6;
    
        String s0 = input.substring(0, input.length() - inset);
        String s1 = input.substring(input.length() - inset, input.length());

        input = s0 + "GMT" + s1;
      }
      
      try {
        value = df.parse(input);
      } catch(Exception e) {
        throw new ActivitiException("Failed to parse date " + input);
      }
    }
    return value;
  }
  
  public static String dateToString(Date date) {
    String dateString = null;
    if(date != null) {
      dateString = df.format(date);
    }
    
    return dateString;
  }
}
