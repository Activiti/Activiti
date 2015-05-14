package org.activiti.explorer.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.activiti.explorer.Constants;

public class ExplorerFilter implements Filter {
  
  private List<String> ignoreList = new ArrayList<String>();

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    ignoreList.add("/ui");
    ignoreList.add("/VAADIN");
    ignoreList.add("/modeler.html");
    ignoreList.add("/editor-app");
    ignoreList.add("/service");
    ignoreList.add("/diagram-viewer");
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    String path = req.getRequestURI().substring(req.getContextPath().length());
    int indexSlash = path.indexOf("/", 1);
    String firstPart = null;
    if (indexSlash > 0) {
      firstPart = path.substring(0, indexSlash);
    } else {
      firstPart = path;
    }
    
    if (ignoreList.contains(firstPart)) {
      
      // Only authenticated users can access /service
      if("/service".equals(firstPart) && req.getRemoteUser() == null &&
        (req.getSession(false) == null || req.getSession().getAttribute(Constants.AUTHENTICATED_USER_ID) == null)){
          ((HttpServletResponse)response).sendError(HttpServletResponse.SC_FORBIDDEN);
          return;
      }
      
      chain.doFilter(request, response); // Goes to default servlet.
    } else {
      request.getRequestDispatcher("/ui" + path).forward(request, response);
    }
  }

  @Override
  public void destroy() {
  }

}
