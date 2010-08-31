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

import org.activiti.engine.impl.runtime.MessageEntity;
import org.activiti.engine.impl.runtime.TimerEntity;
import org.activiti.engine.test.ProcessEngineTestCase;

/**
 * @author Tom Baeyens
 */
public class JobExecutorTestCase extends ProcessEngineTestCase {

  protected TweetHandler tweetHandler = new TweetHandler();

  public void setUp() throws Exception {
    processEngineConfiguration.getJobHandlers().addJobHandler(tweetHandler);
  }

  public void tearDown() throws Exception {
    processEngineConfiguration.getJobHandlers().removeJobHandler(tweetHandler);
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
