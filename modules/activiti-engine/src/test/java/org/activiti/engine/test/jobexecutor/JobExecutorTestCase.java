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
package org.activiti.engine.test.jobexecutor;

import java.util.Date;

import org.activiti.engine.impl.persistence.entity.MessageEntity;
import org.activiti.engine.impl.persistence.entity.TimerEntity;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;

/**
 * @author Tom Baeyens
 */
public abstract class JobExecutorTestCase extends PluggableActivitiTestCase {

  protected TweetHandler tweetHandler = new TweetHandler();

  public void setUp() throws Exception {
    processEngineConfiguration.getJobHandlers().put(tweetHandler.getType(), tweetHandler);
  }

  public void tearDown() throws Exception {
    processEngineConfiguration.getJobHandlers().remove(tweetHandler.getType());
  }

  protected MessageEntity createTweetMessage(String msg) {
    MessageEntity message = new MessageEntity();
    message.setJobHandlerType("tweet");
    message.setJobHandlerConfiguration(msg);
    return message;
  }

  protected TimerEntity createTweetTimer(String msg, Date duedate) {
    TimerEntity timer = new TimerEntity();
    timer.setJobHandlerType("tweet");
    timer.setJobHandlerConfiguration(msg);
    timer.setDuedate(duedate);
    return timer;
  }

}
