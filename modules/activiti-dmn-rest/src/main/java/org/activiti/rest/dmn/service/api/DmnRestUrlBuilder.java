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

package org.activiti.rest.dmn.service.api;

import org.activiti.dmn.engine.ActivitiDmnIllegalArgumentException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;

/**
 * Helper class for building URLs based on a base URL.
 * 
 * An instance can be created by using {@link #fromRequest(HttpServletRequest)} and {@link #fromCurrentRequest()} which extracts the base URL from the request or by specifying the base URL through
 * {@link #usingBaseUrl(String)}
 * 
 * {@link #buildUrl(String[], Object...)} can be called several times to build URLs based on the base URL
 * 
 * @author Bassam Al-Sarori
 * @auth Yvo Swillens
 */
public class DmnRestUrlBuilder {

  protected String baseUrl = "";

  protected DmnRestUrlBuilder() {
  }

  protected DmnRestUrlBuilder(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public String buildUrl(String[] fragments, Object... arguments) {
    return new StringBuilder(baseUrl).append("/").append(MessageFormat.format(StringUtils.join(fragments, '/'), arguments)).toString();
  }

  /** Uses baseUrl as the base URL */
  public static DmnRestUrlBuilder usingBaseUrl(String baseUrl) {
    if (baseUrl == null)
      throw new ActivitiDmnIllegalArgumentException("baseUrl can not be null");
    if (baseUrl.endsWith("/"))
      baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
    return new DmnRestUrlBuilder(baseUrl);
  }

  /** Extracts the base URL from the request */
  public static DmnRestUrlBuilder fromRequest(HttpServletRequest request) {
    return usingBaseUrl(ServletUriComponentsBuilder.fromServletMapping(request).build().toUriString());
  }

  /** Extracts the base URL from current request */
  public static DmnRestUrlBuilder fromCurrentRequest() {
    return usingBaseUrl(ServletUriComponentsBuilder.fromCurrentServletMapping().build().toUriString());
  }
}