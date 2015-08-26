/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.activiti.service.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Base exception for all exceptions in the REST layer.
 *  
 * @author Frederik Heremans
 * @author jbarrez
 */
public class BaseModelerRestException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	protected String messageKey;
	protected Map<String, Object> customData;
    

    public BaseModelerRestException() {
        super();
    }
    
    public BaseModelerRestException(String message, Throwable cause) {
        super(message, cause);
    }

    public BaseModelerRestException(String message) {
        super(message);
    }

    public BaseModelerRestException(Throwable cause) {
        super(cause);
    }
    
    public void setMessageKey(String messageKey) {
        this.messageKey = messageKey;
    }
    
    public String getMessageKey() {
        return messageKey;
    }

	public Map<String, Object> getCustomData() {
		return customData;
	}

	public void setCustomData(Map<String, Object> customData) {
		this.customData = customData;
	}
    
	public void addCustomData(String key, Object data) {
		if (customData == null) {
			customData = new HashMap<String, Object>();
		}
		customData.put(key, data);
	}
	
}
