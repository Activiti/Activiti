package org.activiti.rest.common.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JsonpFilter {

  protected void afterHandle(HttpServletRequest request, HttpServletResponse response) {
    /*String jsonp = request.getResourceRef().getQueryAsForm().getFirstValue("callback");

    if (jsonp != null) {
      StringBuilder stringBuilder = new StringBuilder(jsonp);
      stringBuilder.append("(");

      if ((response.getStatus().getCode() >= 300)) {
        stringBuilder.append("{code:");
        stringBuilder.append(response.getStatus().getCode());
        stringBuilder.append(",msg:'");
        stringBuilder.append(response.getStatus().getDescription()
            .replace("'", "\\'"));
        stringBuilder.append("'}");
        response.setStatus(Status.SUCCESS_OK);
      } else {
        Representation representation = response.getEntity();
        if (representation != null) {
          try {
            InputStream is = representation.getStream();
            if (is != null) {
              ByteArrayOutputStream bos = new ByteArrayOutputStream();
              byte[] buf = new byte[0x10000];
              int len;
              while ((len = is.read(buf)) > 0) {
                bos.write(buf, 0, len);
              }
              stringBuilder.append(bos.toString("UTF-8"));
            } else {
              response.setStatus(Status.SERVER_ERROR_INTERNAL, "NullPointer in Jsonp filter");
            }
          } catch (IOException e) {
            response.setStatus(Status.SERVER_ERROR_INTERNAL, e.getMessage());
          }
        }
      }

      stringBuilder.append(");");
      response.setEntity(new StringRepresentation(stringBuilder.toString(),
          MediaType.TEXT_JAVASCRIPT));
    }*/
  }
}
