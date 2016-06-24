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
package com.activiti.service.editor;

import com.activiti.model.idm.LightGroupRepresentation;

public class ModelDefinitionTransformer {

    private GroupTransformer groupTransformer;

    public ModelDefinitionTransformer(GroupTransformer groupTransformer) {
        this.groupTransformer = groupTransformer;
    }

    public ModelDefinitionTransformer() {
        this.groupTransformer = new GroupTransformer.LightGroupTransformer();
    }

    public LightGroupRepresentation transformGroup(LightGroupRepresentation rep) {
        return groupTransformer.apply(rep);
    }


    public GroupTransformer getGroupTransformer() {
        return groupTransformer;
    }

    public void setGroupTransformer(GroupTransformer groupTransformer) {
        this.groupTransformer = groupTransformer;
    }


    public abstract static class GroupTransformer {

        public abstract LightGroupRepresentation apply(LightGroupRepresentation group);


        public static final class IdOnlyGroupTransformer extends GroupTransformer {
            @Override
            public LightGroupRepresentation apply(LightGroupRepresentation group) {
                LightGroupRepresentation cleanGroup = new LightGroupRepresentation();
                cleanGroup.setId(group.getId());
                return cleanGroup;
            }
        }

        public static final class LightGroupTransformer extends GroupTransformer {
            @Override
            public LightGroupRepresentation apply(LightGroupRepresentation group) {
                LightGroupRepresentation hydratedGroupRep = new LightGroupRepresentation();
                hydratedGroupRep.setId(group.getId());
                hydratedGroupRep.setName(group.getName());
                return hydratedGroupRep;
            }
        }
    }
}
