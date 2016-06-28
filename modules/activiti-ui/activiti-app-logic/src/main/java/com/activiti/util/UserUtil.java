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
package com.activiti.util;

import java.util.ArrayList;
import java.util.List;

import org.activiti.editor.language.json.converter.util.CollectionUtils;

import com.activiti.domain.idm.Group;
import com.activiti.domain.idm.User;

/**
 * @author jbarrez
 */
public class UserUtil {
	
	public static List<Long> getGroupIds(User user) {
	    List<Long> groupIds = new ArrayList<Long>();
	    List<Group> groups = user.getGroups();
	    if (CollectionUtils.isNotEmpty(groups)) {
            for (Group group : groups) {
                groupIds.add(group.getId());
            }
	    }
        return groupIds;
	}

}
