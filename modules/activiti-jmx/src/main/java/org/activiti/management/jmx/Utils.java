/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this    except in compliance with the License.
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

package org.activiti.management.jmx;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Saeid Mirzaei
 */

public class Utils {

  private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

  public static String getHostName() {

    final String DEFAULT_HOST = "localhost";

    String hostName = null;
    boolean canAccessSystemProps = true;
    try {
      // we'll do it this way mostly to determine if we should lookup the hostName
      SecurityManager sm = System.getSecurityManager();
      if (sm != null) {
        sm.checkPropertiesAccess();
      }
    } catch (SecurityException se) {
      canAccessSystemProps = false;
    }

    if (canAccessSystemProps) {
      try {
        hostName = InetAddress.getLocalHost().getHostName();
      } catch (UnknownHostException uhe) {
        LOG.info("Cannot determine localhost name. Fallback to: " + DEFAULT_HOST, uhe);
        hostName = DEFAULT_HOST;
      }
    } else {
      hostName = DEFAULT_HOST;
    }
    return hostName;
  }

}
