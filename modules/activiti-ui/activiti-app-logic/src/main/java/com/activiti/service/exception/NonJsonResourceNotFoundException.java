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
package com.activiti.service.exception;


/**
 * This is needed for when the REST resource is stated to return for example image/png, but needs to throw a 404
 * In this case, the default ExceptionHandlerAdvice will create a message for the 404. But that will lead
 * to a Spring exception and the end result is that it will be transmitted as a 500.
 * 
 * @author jbarrez
 */
public class NonJsonResourceNotFoundException extends BaseModelerRestException {

	private static final long serialVersionUID = 1L;
	
	public NonJsonResourceNotFoundException() {
	}

}