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
package com.activiti.image;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.activiti.bpmn.model.BpmnModel;
import org.activiti.image.ProcessDiagramGenerator;
import org.activiti.image.exception.ActivitiImageException;
import org.activiti.image.impl.DefaultProcessDiagramGenerator;

public class ImageGenerator {

	public static BufferedImage createImage(BpmnModel bpmnModel) {
        ProcessDiagramGenerator diagramGenerator = new DefaultProcessDiagramGenerator();
        BufferedImage diagramImage = diagramGenerator.generatePngImage(bpmnModel, 1.0);
        return diagramImage;
	}
	
	public static BufferedImage createImage(BpmnModel bpmnModel, double scaleFactor) {
        ProcessDiagramGenerator diagramGenerator = new DefaultProcessDiagramGenerator(scaleFactor);
        BufferedImage diagramImage = diagramGenerator.generatePngImage(bpmnModel, scaleFactor);
        return diagramImage;
	}
	
	public static byte[] createByteArrayForImage(BufferedImage image, String imageType) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    try {
	    	ImageIO.write(image, imageType, out);
	      
	    } catch (IOException e) {
	      throw new ActivitiImageException("Error while generating byte array for process image", e);
	    } finally {
	    	try {
	    		if (out != null) {
	    			out.close();
	    		}
	    	} catch(IOException ignore) {
	    		// Exception is silently ignored
	    	}
	    }
	    return out.toByteArray();
	}
}
