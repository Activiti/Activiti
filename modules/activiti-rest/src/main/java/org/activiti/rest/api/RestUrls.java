package org.activiti.rest.api;

import java.text.MessageFormat;

import org.apache.commons.lang.StringUtils;

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

/**
 * @author Frederik Heremans
 */
public final class RestUrls {

  /**
   * Base segment for all repository-related resources: <i>repository</i>
   */
  public static final String SEGMENT_REPOSITORY_RESOURCES = "repository";
  
  public static final String SEGMENT_DEPLOYMENT_RESOURCE = "deployments";
  public static final String SEGMENT_DEPLOYMENT_ARTIFACT_RESOURCE = "resources";
  public static final String SEGMENT_DEPLOYMENT_ARTIFACT_RESOURCE_CONTENT = "resourcedata";
  
  /**
   * URL template for the deployment collection: <i>repository/deployments</i>
   */
  public static final String[] URL_DEPLOYMENT_COLLECTION = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_DEPLOYMENT_RESOURCE};
  
  /**
   * URL template for a single deployment: <i>repository/deployments/{0:deploymentId}</i>
   */
  public static final String[] URL_DEPLOYMENT = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_DEPLOYMENT_RESOURCE, "{0}"};
  
  /**
   * URL template listing deployment resources: <i>repository/deployments/{0:deploymentId}/resources/{1}:resourceId</i>
   */
  public static final String[] URL_DEPLOYMENT_RESOURCES = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_DEPLOYMENT_RESOURCE, 
    "{0}", SEGMENT_DEPLOYMENT_ARTIFACT_RESOURCE};
  
  /**
   * URL template for a single deployment resource: <i>repository/deployments/{0:deploymentId}/resources/{1}:resourceId</i>
   */
  public static final String[] URL_DEPLOYMENT_RESOURCE = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_DEPLOYMENT_RESOURCE, 
    "{0}", SEGMENT_DEPLOYMENT_ARTIFACT_RESOURCE, "{1}"};
  
  /**
   * URL template for a single deployment resource content: <i>repository/deployments/{0:deploymentId}/resourcedata/{1}:resourceId</i>
   */
  public static final String[] URL_DEPLOYMENT_RESOURCE_CONTENT = {SEGMENT_REPOSITORY_RESOURCES, SEGMENT_DEPLOYMENT_RESOURCE, 
    "{0}", SEGMENT_DEPLOYMENT_ARTIFACT_RESOURCE_CONTENT, "{1}"};
  
  
  /**
   * Creates an url based on the passed fragments and replaces any placeholders with the given arguments. The
   * placeholders are folowing the {@link MessageFormat} convention 
   * (eg. {0} is replaced by first argument value).
   */
  public static final String createRelativeResourceUrl(String[] segments, Object...arguments) {
    return MessageFormat.format(StringUtils.join(segments, '/'), arguments);
  }
  
  
}
