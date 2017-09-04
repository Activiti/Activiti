package org.activiti.services.query.es.config;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "org.activiti.services.query.es.model")
public class ElasticConfiguration {

	private static Logger logger = LoggerFactory.getLogger(ElasticConfiguration.class);

	@Value("${spring.data.elasticsearch.cluster-name}")
	private String clusterName;

	@Value("${spring.data.elasticsearch.cluster-nodes}")
	private String clusterNodes;

	@Bean
	public ElasticsearchOperations elasticsearchTemplate() throws UnknownHostException {

		String server = clusterNodes.split(":")[0];

		Integer port = Integer.parseInt(clusterNodes.split(":")[1]);

		Settings settings = Settings.builder().put("cluster.name", clusterName).build();

		TransportClient client = new PreBuiltTransportClient(settings);

		client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(server), port));

		return new ElasticsearchTemplate(client);

	}
}
