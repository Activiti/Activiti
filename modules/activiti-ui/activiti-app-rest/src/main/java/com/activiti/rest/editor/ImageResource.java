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
package com.activiti.rest.editor;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.util.Date;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.activiti.domain.common.ImageUpload;
import com.activiti.domain.idm.User;
import com.activiti.model.common.ImageUploadRepresentation;
import com.activiti.repository.common.ImageUploadRepository;
import com.activiti.security.SecurityUtils;
import com.activiti.service.exception.InternalServerErrorException;
import com.activiti.service.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class ImageResource {

	private final Logger log = LoggerFactory.getLogger(ImageResource.class);
	
	@Inject
	protected ImageUploadRepository imageUploadRepository;
	
    @Inject
    protected ObjectMapper objectMapper;
    
    private static final Logger logger = LoggerFactory.getLogger(ImageResource.class);
	
	@RequestMapping(value = "/rest/image/{imageId}",
            method = RequestMethod.GET)
    public void getImage(@PathVariable Long imageId, HttpServletResponse response) {
	    ImageUpload imageUpload = imageUploadRepository.findOne(imageId);
	    
	    if (imageUpload == null) {
	        throw new NotFoundException("Image not found with id " + imageId);
	    }
	    
	    response.setHeader("Content-Disposition", "attachment; filename=" + imageUpload.getName());
	    if (imageUpload.getName().toLowerCase().endsWith("png")) {
	        response.setContentType("image/png");
	    } else if (imageUpload.getName().toLowerCase().endsWith("jpeg") || imageUpload.getName().toLowerCase().endsWith("jpg")) {
	        response.setContentType("image/jpeg");
	    } else if (imageUpload.getName().toLowerCase().endsWith("gif")) {
            response.setContentType("image/gif");
        }
	    
        try {
            ServletOutputStream servletOutputStream = response.getOutputStream();
            BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(imageUpload.getImage()));

            byte[] buffer = new byte[8096];
            while (true) {
                int count = in.read(buffer);
                if (count == -1)
                    break;
                servletOutputStream.write(buffer, 0, count);
            }
            
            // Flush and close stream
            servletOutputStream.flush();
            servletOutputStream.close();
        } catch (Exception e) {
            log.error("Could not get image " + imageId, e);
            throw new InternalServerErrorException("Could not get image " + imageId);
        }
	}
	
	@RequestMapping(value = "/rest/images",
	        method = RequestMethod.POST,
	        produces = "application/json")
	public ImageUploadRepresentation createImage(@RequestParam("file") MultipartFile file) {
	    User currentUser = SecurityUtils.getCurrentUserObject();
        
        try {
            ImageUpload imageUpload = new ImageUpload();
            imageUpload.setName(file.getOriginalFilename());
            imageUpload.setCreated(new Date());
            imageUpload.setUserId(currentUser.getId());
            imageUpload.setImage(IOUtils.toByteArray(file.getInputStream()));
            
            imageUploadRepository.save(imageUpload);
            
            return new ImageUploadRepresentation(imageUpload);
            
        } catch (Exception e) {
            log.error("Error saving image " + file.getOriginalFilename(), e);
            throw new InternalServerErrorException("Error saving image " + file.getOriginalFilename());
        }
        
	}
	
	   @RequestMapping(value = "/rest/images/text",
	            method = RequestMethod.POST)
	    public String createImageText(@RequestParam("file") MultipartFile file) {
	        User currentUser = SecurityUtils.getCurrentUserObject();
	        
	        try {
	            ImageUpload imageUpload = new ImageUpload();
	            imageUpload.setName(file.getOriginalFilename());
	            imageUpload.setCreated(new Date());
	            imageUpload.setUserId(currentUser.getId());
	            imageUpload.setImage(IOUtils.toByteArray(file.getInputStream()));
	            
	            imageUploadRepository.save(imageUpload);
	            
	            ImageUploadRepresentation imageUploadRepresentation = new ImageUploadRepresentation(imageUpload);	            
	            String imageUploadRepresentationJson = null;
	            try {
	                imageUploadRepresentationJson = objectMapper.writeValueAsString(imageUploadRepresentation);
	            } catch (Exception e) {
	                logger.error("Error while Image representation json", e);
	                throw new InternalServerErrorException("Image could not be saved");
	            }

	            return imageUploadRepresentationJson;
	        } catch (Exception e) {
	            log.error("Error saving image " + file.getOriginalFilename(), e);
	            throw new InternalServerErrorException("Error saving image " + file.getOriginalFilename());
	        }
	        
	    }
}
