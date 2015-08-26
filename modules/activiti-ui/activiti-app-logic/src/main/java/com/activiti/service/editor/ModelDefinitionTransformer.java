/**
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
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
