package org.activiti.rest.service.api;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

public class PutAwareCommonsMultipartResolver extends CommonsMultipartResolver {

  private static final String MULTIPART = "multipart/";

  @Override
  public boolean isMultipart(HttpServletRequest request) {
      return request != null && isMultipartContent(request);
  }

  /**
   * Utility method that determines whether the request contains multipart
   * content.
   * 
   * @param request The servlet request to be evaluated. Must be non-null.
   * 
   * @return <code>true</code> if the request is multipart; {@code false}
   * otherwise.
   * 
   * @see ServletFileUpload#isMultipartContent(HttpServletRequest)
   */
  public static final boolean isMultipartContent(HttpServletRequest request) {
      final String method = request.getMethod().toLowerCase();
      if (!method.equalsIgnoreCase("post") && !method.equalsIgnoreCase("put")) {
          return false;
      }
      
      String contentType = request.getContentType();
      if (contentType == null) {
          return false;
      }
      
      if (contentType.toLowerCase().startsWith(MULTIPART)) {
          return true;
      }
      return false;
  }
}
