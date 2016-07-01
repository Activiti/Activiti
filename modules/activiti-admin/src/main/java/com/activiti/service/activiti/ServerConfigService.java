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
package com.activiti.service.activiti;

import com.activiti.domain.ServerConfig;
import com.activiti.repository.ServerConfigRepository;
import com.activiti.web.rest.dto.ServerConfigRepresentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jbarrez
 * @author yvoswillens
 */
@Service
public class ServerConfigService extends AbstractEncryptingService {

    private static final String REST_APP_NAME = "rest.app.name";
    private static final String REST_APP_DESCRIPTION = "rest.app.description";
    private static final String REST_APP_HOST = "rest.app.host";
    private static final String REST_APP_PORT = "rest.app.port";
    private static final String REST_APP_CONTEXT_ROOT = "rest.app.contextroot";
    private static final String REST_APP_REST_ROOT = "rest.app.restroot";
    private static final String REST_APP_USER = "rest.app.user";
    private static final String REST_APP_PASSWORD = "rest.app.password";

    @Autowired
    protected Environment environment;

	@Autowired
	protected ServerConfigRepository serverConfigRepository;

	@Transactional
	public void createDefaultServerConfig() {

        ServerConfig serverConfig = getDefaultServerConfig();

        serverConfig.setUserName(environment.getRequiredProperty(REST_APP_USER));
        serverConfig.setPassword(environment.getRequiredProperty(REST_APP_PASSWORD));

		save(serverConfig, true);
	}

    @Transactional
    public ServerConfig findOne(Long id) {
        return serverConfigRepository.findOne(id);
    }

    @Transactional
    public List<ServerConfigRepresentation> findAll() {
        return createServerConfigRepresentation(serverConfigRepository.findAll());
    }

    @Transactional
    public void save(ServerConfig serverConfig, boolean encryptPassword) {
        if (encryptPassword) {
            serverConfig.setPassword(encrypt(serverConfig.getPassword()));
        }
        serverConfigRepository.save(serverConfig);
    }
    
    public String getServerConfigDecryptedPassword(ServerConfig serverConfig) {
        return decrypt(serverConfig.getPassword());
    }

    protected List<ServerConfigRepresentation> createServerConfigRepresentation(List<ServerConfig> serverConfigs) {
        List<ServerConfigRepresentation> serversRepresentations = new ArrayList<ServerConfigRepresentation>();
        for (ServerConfig serverConfig: serverConfigs) {
            serversRepresentations.add(createServerConfigRepresentation(serverConfig));
        }
        return serversRepresentations;
    }
    
    protected ServerConfigRepresentation createServerConfigRepresentation(ServerConfig serverConfig) {
        ServerConfigRepresentation serverRepresentation = new ServerConfigRepresentation();
        serverRepresentation.setId(serverConfig.getId());
        serverRepresentation.setName(serverConfig.getName());
        serverRepresentation.setDescription(serverConfig.getDescription());
        serverRepresentation.setServerAddress(serverConfig.getServerAddress());
        serverRepresentation.setServerPort(serverConfig.getPort());
        serverRepresentation.setContextRoot(serverConfig.getContextRoot());
        serverRepresentation.setRestRoot(serverConfig.getRestRoot());
        serverRepresentation.setUserName(serverConfig.getUserName());
        return serverRepresentation;
    }

    public ServerConfig getDefaultServerConfig() {

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setName(environment.getRequiredProperty(REST_APP_NAME));
        serverConfig.setDescription(environment.getRequiredProperty(REST_APP_DESCRIPTION));
        serverConfig.setServerAddress(environment.getRequiredProperty(REST_APP_HOST));
        serverConfig.setPort(environment.getRequiredProperty(REST_APP_PORT, Integer.class));
        serverConfig.setContextRoot(environment.getRequiredProperty(REST_APP_CONTEXT_ROOT));
        serverConfig.setRestRoot(environment.getRequiredProperty(REST_APP_REST_ROOT));

        return serverConfig;

    }
}
