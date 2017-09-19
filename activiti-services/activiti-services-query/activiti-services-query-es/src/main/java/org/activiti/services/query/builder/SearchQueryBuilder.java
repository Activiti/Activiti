package org.activiti.services.query.builder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Component;

@Component
public class SearchQueryBuilder {

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

}
