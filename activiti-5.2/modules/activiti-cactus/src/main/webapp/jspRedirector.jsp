<%--
 - Licensed to the Apache Software Foundation (ASF) under one
 - or more contributor license agreements.  See the NOTICE file
 - distributed with this work for additional information
 - regarding copyright ownership.  The ASF licenses this file
 - to you under the Apache License, Version 2.0 (the
 - "License"); you may not use this file except in compliance
 - with the License.  You may obtain a copy of the License at
 - 
 - http://www.apache.org/licenses/LICENSE-2.0
 - 
 - Unless required by applicable law or agreed to in writing,
 - software distributed under the License is distributed on an
 - "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 - KIND, either express or implied.  See the License for the
 - specific language governing permissions and limitations
 - under the License.   
--%><%@page import="org.apache.cactus.server.*,org.apache.cactus.internal.server.*" session="true" %><%

    /**                                                
     * Note:
     * It is very important not to put any character between the end
     * of the page tag and the beginning of the java code expression, otherwise,
     * the generated servlet containss a 'out.println("\r\n");' and this breaks
     * our mechanism !
     */

    /**
     * This JSP is used as a proxy to call your server-side unit tests. We use
     * a JSP rather than a servlet because for testing custom JSP tags for
     * example we need access to JSP implicit objects (PageContext and
     * JspWriter).
     */

    JspImplicitObjects objects = new JspImplicitObjects();
    objects.setHttpServletRequest(request);
    objects.setHttpServletResponse(response);
    objects.setServletConfig(config);
    objects.setServletContext(application);
    objects.setJspWriter(out);
    objects.setPageContext(pageContext);

    JspTestRedirector redirector = new JspTestRedirector();
    redirector.doGet(objects);
%>