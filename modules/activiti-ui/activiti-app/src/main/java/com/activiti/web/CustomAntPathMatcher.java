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
package com.activiti.web;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;

/**
 * Custom path matcher that ignores all unknown http-methods, which do not exist in {@link HttpMethod}
 * enum.
 * 
 * Wraps a {@link AntPathRequestMatcher}
 * 
 * @author Frederik Heremans
 */
public class CustomAntPathMatcher implements RequestMatcher {

    protected AntPathRequestMatcher wrapped;
    protected boolean httpMethodUsed = false;

    /**
     * Creates a matcher with the specific pattern which will match all HTTP
     * methods in a case insensitive manner.
     *
     * @param pattern
     *            the ant pattern to use for matching
     */
    public CustomAntPathMatcher(String pattern) {
        this(pattern, null);
    }

    /**
     * Creates a matcher with the supplied pattern and HTTP method in a case
     * insensitive manner.
     *
     * @param pattern
     *            the ant pattern to use for matching
     * @param httpMethod
     *            the HTTP method. The {@code matches} method will return false
     *            if the incoming request doesn't have the same method.
     */
    public CustomAntPathMatcher(String pattern, String httpMethod) {
        this(pattern,httpMethod,false);
    }

    /**
     * Creates a matcher with the supplied pattern which will match the
     * specified Http method
     *
     * @param pattern
     *            the ant pattern to use for matching
     * @param httpMethod
     *            the HTTP method. The {@code matches} method will return false
     *            if the incoming request doesn't doesn't have the same method.
     * @param caseSensitive
     *            true if the matcher should consider case, else false
     */
    public CustomAntPathMatcher(String pattern, String httpMethod, boolean caseSensitive) {
        httpMethodUsed = httpMethod != null;
        wrapped = new AntPathRequestMatcher(pattern, httpMethod, caseSensitive);
    }
    
    @Override
    public boolean matches(HttpServletRequest request) {
        if(httpMethodUsed) {
            // Check if the method actually exists
            try {
                HttpMethod.valueOf(request.getMethod());
            } catch(IllegalArgumentException iae) {
                // Since it's not possible to register matchers for an unknown method, we know the path/method will
                // never match. Return false to prevent an exception in the wrapped matcher
                return false;
            }
        }
        return wrapped.matches(request);
    }

}
