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
