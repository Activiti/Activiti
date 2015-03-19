package org.activiti.camel.util;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;


public class FlagJavaDelegate implements JavaDelegate {
   public static  boolean flag;
   
   public static void reset()  {
     flag = false;
   }

  @Override
  public void execute(DelegateExecution execution) throws Exception {
    flag = true;
    
  }
  
  public static boolean isFlagSet() {
    return flag;
  }
   
}
