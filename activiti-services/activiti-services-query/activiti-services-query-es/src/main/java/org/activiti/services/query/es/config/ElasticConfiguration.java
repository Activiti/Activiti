package org.activiti.services.query.es.config;

import java.io.File;
import java.io.IOException;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@EnableElasticsearchRepositories(basePackages = "org.activiti.services.query.es.model")
public class ElasticConfiguration {

	private static Logger logger = LoggerFactory.getLogger(ElasticConfiguration.class);

	@Bean
	public ElasticsearchOperations elasticsearchTemplate() throws IOException {
		File tmpDir = File.createTempFile("elastic", Long.toString(System.nanoTime()));

		logger.info("Temp directory: " + tmpDir.getAbsolutePath());

		Settings elasticsearchSettings = Settings.builder().put("http.enabled", "true") // 1
				.put("index.number_of_shards", "1")
				.put("path.data", new File(tmpDir, "data").getAbsolutePath()) // 2
				.put("path.logs", new File(tmpDir, "logs").getAbsolutePath()) // 2
				.put("path.work", new File(tmpDir, "work").getAbsolutePath()) // 2
				.put("path.home", tmpDir).build();
		;

		TransportClient client = new PreBuiltTransportClient(elasticsearchSettings);

		return new ElasticsearchTemplate(client);
	}
}
