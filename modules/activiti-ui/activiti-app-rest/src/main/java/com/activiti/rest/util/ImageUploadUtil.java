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
package com.activiti.rest.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.activiti.domain.common.ImageUpload;
import com.activiti.service.exception.InternalServerErrorException;
import com.activiti.service.exception.NotFoundException;

/**
 * @author Joram Barrez
 */
public class ImageUploadUtil {

	public static void writeImageUploadToResponse(HttpServletResponse response, ImageUpload imageUpload, boolean fallBackToDefault) throws IOException {
	    byte[] imageBytes = null;
	    String fileName = null;
	    if (imageUpload != null) {
	    	imageBytes = imageUpload.getImage();
	    	fileName = imageUpload.getName();
	    } else if (fallBackToDefault) {
	    	try {
	            imageBytes = IOUtils.toByteArray(ImageUploadUtil.class.getClassLoader().getResourceAsStream("activiti-logo.png"));
	            fileName = "default-logo.png";
	        } catch (IOException e) {
	        	throw new InternalServerErrorException("Could not find default tenant logo");
	        }
	    } else {
	    	throw new NotFoundException();
	    }

	    response.setHeader("Content-Disposition", "attachment; filename="+ fileName);
	    if (fileName.toLowerCase().endsWith("png")) {
	    	response.setContentType("image/png");
	    } else if (fileName.toLowerCase().endsWith("jpeg") || fileName.toLowerCase().endsWith("jpg")) {
	    	response.setContentType("image/jpeg");
	    } else if (fileName.toLowerCase().endsWith("gif")) {
	    	response.setContentType("image/gif");
	    }


	    ServletOutputStream servletOutputStream = response.getOutputStream();
	    BufferedInputStream in = new BufferedInputStream(
	            new ByteArrayInputStream(imageBytes));

	    byte[] buffer = new byte[32384];
	    while (true) {
	    	int count = in.read(buffer);
	    	if (count == -1)
	    		break;
	    	servletOutputStream.write(buffer, 0, count);
	    }

	    // Flush and close stream
	    servletOutputStream.flush();
	    servletOutputStream.close();
    }
	
}
