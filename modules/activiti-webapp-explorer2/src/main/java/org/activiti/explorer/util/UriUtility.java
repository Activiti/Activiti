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

package org.activiti.explorer.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * @author 'Frederik Heremans'
 */
public class UriUtility {

  private static final String PARAMETER_SEPARATOR = "&";
  private static final String QUERY_STRING_SEPARATOR = "?";
  private static final String NAME_VALUE_SEPARATOR = "=";
  private static final String URL_PART_SEPARATOR = "/";

  /**
   * Get path fragments. eg: "/task/inbox/123" will return a list with three
   * values: "task", "inbox" and "123" in that order.
   */
  public static List<String> getFragmentParts(String url) {
    if (url != null) {
      List<String> parts = new ArrayList<String>();
      String[] partsArray = url.split(URL_PART_SEPARATOR);
      for (String part : partsArray) {
        if (part.length() > 0) {
          parts.add(part);
        }
      }
      return parts;
    }
    return null;
  }

  /**
   * Extracts the query parameters from the given url fragment.
   */
  public static String extractQueryString(String fragment) {
    if (fragment != null) {
      int firstIndex = fragment.indexOf(QUERY_STRING_SEPARATOR);
      if (firstIndex >= 0) {
        return fragment.substring(firstIndex);
      }
    }
    return null;
  }

  /**
   * Extracts uri from the given url fragment, without the query params.
   */
  public static String extractUri(String fragment) {
    if (fragment != null) {
      int firstIndex = fragment.indexOf(QUERY_STRING_SEPARATOR);
      if (firstIndex >= 0) {
        return fragment.substring(0, firstIndex);
      } else {
        // No query string present, use full fragment
        return fragment;
      }
    }
    return null;
  }

  /**
   * Parses the parameters from the given url query string. When no encoding is
   * passed, default UTF-8 is used. Based on the implementation in Commons
   * httpclient UrlEncodedUtils.
   */
  public static Map<String, String> parseQueryParameters(String queryString, String encoding) {
    Map<String, String> parameters = new LinkedHashMap<String, String>();
    if (queryString != null) {
      // Remove '?' if present at beginning of query-string
      if(queryString.startsWith(QUERY_STRING_SEPARATOR)) {
        queryString = queryString.substring(1);
      }
      
      Scanner scanner = new Scanner(queryString);
      scanner.useDelimiter(PARAMETER_SEPARATOR);
      while (scanner.hasNext()) {
        final String[] nameValue = scanner.next().split(NAME_VALUE_SEPARATOR);
        if (nameValue.length == 0 || nameValue.length > 2)
          throw new IllegalArgumentException("bad parameter");

        final String name = decode(nameValue[0], encoding);
        String value = null;
        if (nameValue.length == 2) {
          value = decode(nameValue[1], encoding);
        }
        parameters.put(name, value);
      }
    }
    return parameters;
  }

  /**
   * Gets a valid query-string based on the given parameters.
   */
  public static String getQueryString(Map<String, String> parameters) {
    StringBuilder result = new StringBuilder();

    if (parameters != null) {
      for (Entry<String, String> param : parameters.entrySet()) {
        final String encodedName = encode(param.getKey(), null);
        final String value = param.getValue();
        final String encodedValue = value != null ? encode(value, null) : "";
        if (result.length() > 0) {
          result.append(PARAMETER_SEPARATOR);
        } else {
          result.append(QUERY_STRING_SEPARATOR);
        }
        result.append(encodedName);
        result.append(NAME_VALUE_SEPARATOR);
        result.append(encodedValue);
      }
    }

    return result.toString();
  }

  /**
   * Gets full path based on the given parts.
   */
  public static String getPath(List<String> parts) {
    if (parts != null) {
      StringBuilder result = new StringBuilder();
      for (String part : parts) {
        if(result.length() > 0) {
          result.append(URL_PART_SEPARATOR);
        }
        result.append(part);
      }
      return result.toString();
    }
    return "";
  }

  private static String decode(final String content, final String encoding) {
    try {
      return URLDecoder.decode(content, encoding != null ? encoding : "UTF-8");
    } catch (UnsupportedEncodingException problem) {
      throw new IllegalArgumentException(problem);
    }
  }

  private static String encode(final String content, final String encoding) {
    try {
      return URLEncoder.encode(content, encoding != null ? encoding : "UTF-8");
    } catch (UnsupportedEncodingException problem) {
      throw new IllegalArgumentException(problem);
    }
  }
}