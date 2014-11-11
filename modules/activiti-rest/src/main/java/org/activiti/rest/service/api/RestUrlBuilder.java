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

package org.activiti.rest.service.api;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;

import org.activiti.engine.ActivitiIllegalArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Helper class for building URLs based on a base URL.
 * 
 * An instance can be created by using {@link #fromRequest(HttpServletRequest)} and {@link #fromCurrentRequest()}
 * which extracts the base URL from the request or by specifying the base URL through {@link #usingBaseUrl(String)}
 * 
 * {@link #buildUrl(String[], Object...)} can be called several times to build URLs based on the base URL 
 * 
 * @author Bassam Al-Sarori
 */
public class RestUrlBuilder {

  protected String baseUrl = "";

  protected RestUrlBuilder(){
  }

  protected RestUrlBuilder(String baseUrl){
    this.baseUrl = baseUrl;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public String buildUrl(String[] fragments, Object ... arguments) {
    StringBuilder urlBuilder = new StringBuilder(baseUrl);
    urlBuilder.append("/");
    urlBuilder.append(MessageFormat.format(StringUtils.join(fragments, '/'), arguments));
    return urlBuilder.toString();
  }

  protected static String extractRequestBaseUrl(HttpServletRequest request){
    String baseUrl = request.getRequestURL().toString();
    if(StringUtils.isNotBlank(request.getPathInfo())){
      baseUrl = baseUrl.substring(0, decodeUrl(baseUrl).indexOf(request.getPathInfo()));
    }
    return baseUrl;
  }

  /** Uses baseUrl as the base URL */
  public static RestUrlBuilder usingBaseUrl(String baseUrl){
    if(baseUrl == null) throw new ActivitiIllegalArgumentException("baseUrl can not be null");
    if(baseUrl.endsWith("/")) baseUrl = baseUrl.substring(0, baseUrl.length()-1);
    return new RestUrlBuilder(baseUrl);
  }

  /** Extracts the base URL from the request */
  public static RestUrlBuilder fromRequest(HttpServletRequest request){
    return usingBaseUrl(extractRequestBaseUrl(request));
  }

  public static RestUrlBuilder fromCurrentRequest(){
    return usingBaseUrl(extractRequestBaseUrl(getCurrentRequest()));
  }

  //from org.springframework.web.servlet.support.ServletUriComponentsBuilder
  protected static HttpServletRequest getCurrentRequest() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    Assert.state(requestAttributes != null, "Could not find current request via RequestContextHolder");
    Assert.isInstanceOf(ServletRequestAttributes.class, requestAttributes);
    HttpServletRequest servletRequest = ((ServletRequestAttributes) requestAttributes).getRequest();
    Assert.state(servletRequest != null, "Could not find current HttpServletRequest");
    return servletRequest;
  }

  protected static String decodeUrl(String url){
    try {
      return URLDecoder.decode(url, "UTF-8");
    } catch (UnsupportedEncodingException uee) {
      throw new IllegalStateException("JVM does not support UTF-8 encoding.", uee);
    }
  }
}