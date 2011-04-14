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

package org.activiti.rest.impl;


/** http://en.wikipedia.org/wiki/Hypertext_Transfer_Protocol#Request_methods
 * 
 * @author Tom Baeyens
 */
public enum HttpServletMethod {
  
  /** Asks for the response identical to the one that would correspond to a GET request, but without the response body. This is useful for retrieving meta-information written in response headers, without having to transport the entire content. */
  HEAD,
  
  /** Requests a representation of the specified resource. Requests using GET (and a few other HTTP methods) "SHOULD NOT have the significance of taking an action other than retrieval".[1] The W3C has published guidance principles on this distinction, saying, "Web application design should be informed by the above principles, but also by the relevant limitations."[12] See safe methods below. */
  GET,
  
  /** Submits data to be processed (e.g., from an HTML form) to the identified resource. The data is included in the body of the request. This may result in the creation of a new resource or the updates of existing resources or both. */
  POST,

  /** Uploads a representation of the specified resource. */
  PUT,

  /** Deletes the specified resource. */
  DELETE,

  /** Echoes back the received request, so that a client can see what (if any) changes or additions have been made by intermediate servers. */
  TRACE,

  /** Returns the HTTP methods that the server supports for specified URL. This can be used to check the functionality of a web server by requesting '*' instead of a specific resource. */
  OPTIONS,

  /** Converts the request connection to a transparent TCP/IP tunnel, usually to facilitate SSL-encrypted communication (HTTPS) through an unencrypted HTTP proxy.[13] */
  CONNECT,
  
  /** Is used to apply partial modifications to a resource. */
  PATCH
}
