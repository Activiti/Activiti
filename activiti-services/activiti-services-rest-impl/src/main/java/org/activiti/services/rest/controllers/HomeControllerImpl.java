/*
 * Licensed under the Apache License, Version 2.0 (the "License");
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
 *
 */

package org.activiti.services.rest.controllers;

import org.activiti.services.rest.api.HomeController;
import org.activiti.services.rest.api.resources.HomeResource;
import org.springframework.hateoas.Resource;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

@RestController
public class HomeControllerImpl implements HomeController {

    @Override
    public Resource getHomeInfo() {
        Resource resource = new Resource(new HomeResource(),
                                         linkTo(ProcessDefinitionControllerImpl.class).withRel("process-definitions"),
                                         linkTo(ProcessInstanceControllerImpl.class).withRel("process-instances"),
                                         linkTo(TaskControllerImpl.class).withRel("tasks"));

        return resource;
    }
}
