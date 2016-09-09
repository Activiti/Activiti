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
package org.activiti.app.web;

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
