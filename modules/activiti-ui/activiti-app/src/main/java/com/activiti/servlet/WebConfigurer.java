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
package com.activiti.servlet;

import java.util.EnumSet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import com.activiti.conf.ApplicationConfiguration;

/**
 * Configuration of web application with Servlet 3.0 APIs.
 */
public class WebConfigurer implements ServletContextListener {
	
    private final Logger log = LoggerFactory.getLogger(WebConfigurer.class);

    public AnnotationConfigWebApplicationContext context;
    
    public void setContext(AnnotationConfigWebApplicationContext context) {
        this.context = context;
    }
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        log.debug("Configuring Spring root application context");

        ServletContext servletContext = sce.getServletContext();

        AnnotationConfigWebApplicationContext rootContext = null;
        
        if (context == null) {
            rootContext = new AnnotationConfigWebApplicationContext();
            rootContext.register(ApplicationConfiguration.class);
            
            if (rootContext.getServletContext() == null) {
              rootContext.setServletContext(servletContext);
            }
            
            rootContext.refresh();
            context = rootContext;
            
        } else {
            rootContext = context;
            if (rootContext.getServletContext() == null) {
              rootContext.setServletContext(servletContext);
            }
        }
        
        servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, rootContext);

        EnumSet<DispatcherType> disps = EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ASYNC);

        initSpring(servletContext, rootContext);
        initSpringSecurity(servletContext, disps);

        log.debug("Web application fully configured");
    }

    /**
     * Initializes Spring and Spring MVC.
     */
    private void initSpring(ServletContext servletContext, AnnotationConfigWebApplicationContext rootContext) {
        log.debug("Configuring Spring Web application context");
        AnnotationConfigWebApplicationContext appDispatcherServletConfiguration = new AnnotationConfigWebApplicationContext();
        appDispatcherServletConfiguration.setParent(rootContext);
        appDispatcherServletConfiguration.register(AppDispatcherServletConfiguration.class);

        log.debug("Registering Spring MVC Servlet");
        ServletRegistration.Dynamic appDispatcherServlet = servletContext.addServlet("appDispatcher", 
                new DispatcherServlet(appDispatcherServletConfiguration));
        appDispatcherServlet.addMapping("/app/*");
        appDispatcherServlet.setLoadOnStartup(1);
        appDispatcherServlet.setAsyncSupported(true);
    }

    /**
     * Initializes Spring Security.
     */
    private void initSpringSecurity(ServletContext servletContext, EnumSet<DispatcherType> disps) {
        log.debug("Registering Spring Security Filter");
        FilterRegistration.Dynamic springSecurityFilter = servletContext.addFilter("springSecurityFilterChain", new DelegatingFilterProxy());

        springSecurityFilter.addMappingForUrlPatterns(disps, false, "/*");
        springSecurityFilter.setAsyncSupported(true);
    }


    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Destroying Web application");
        WebApplicationContext ac = WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext());
        AnnotationConfigWebApplicationContext gwac = (AnnotationConfigWebApplicationContext) ac;
        gwac.close();
        log.debug("Web application destroyed");
    }
}
