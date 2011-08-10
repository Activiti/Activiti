/* Copyright (C) 2005-2011 of Alfresco. All rights reserved.
 *
 * This file is part of Alfresco Pangu.
 * 
 * Alfresco Pangu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Alfresco Pangu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Alfresco Pangu.  If not, see <http://www.gnu.org/licenses/>. 
 */

package org.activiti.engine.test.bpmn.servicetask;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;


/**
 * @author Joram Barrez
 */
public class DummyServiceTask implements JavaDelegate {
  
  public void execute(DelegateExecution execution) throws Exception {
    Integer count = (Integer) execution.getVariable("count");
    count = count+1;
    System.out.println("Count = " + count);
    execution.setVariable("count", count);
  }

}
