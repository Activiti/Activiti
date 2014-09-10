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

package org.activiti.engine.impl.interceptor;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author Tom Baeyens
 */
public class LogInterceptor extends AbstractCommandInterceptor {
  
  private static Logger log = LoggerFactory.getLogger(LogInterceptor.class);

  public <T> T execute(CommandConfig config, Command<T> command) {
    if (!log.isDebugEnabled()) {
      // do nothing here if we cannot log
      return next.execute(config, command);
    }
    log.debug("\n");
    log.debug("--- starting {} --------------------------------------------------------", command.getClass().getSimpleName());
    try {

      return next.execute(config, command);

    } finally {
      log.debug("--- {} finished --------------------------------------------------------", command.getClass().getSimpleName());
      log.debug("\n");
    }
  }
}
