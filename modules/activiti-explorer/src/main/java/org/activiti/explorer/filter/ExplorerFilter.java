package org.activiti.explorer.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

public class ExplorerFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    String path = req.getRequestURI().substring(req.getContextPath().length());
    
    if (path.startsWith("/ui") || path.startsWith("/VAADIN") || path.startsWith("/api") || path.startsWith("/editor") || path.startsWith("/explorer") || 
        path.startsWith("/libs") || path.startsWith("/service")) {
        
      chain.doFilter(request, response); // Goes to default servlet.
    } else {
      request.getRequestDispatcher("/ui" + path).forward(request, response);
    }
  }

  @Override
  public void destroy() {
  }

}
