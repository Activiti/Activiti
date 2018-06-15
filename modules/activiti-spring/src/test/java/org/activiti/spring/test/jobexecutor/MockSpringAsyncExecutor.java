package org.activiti.spring.test.jobexecutor;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.activiti.engine.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.activiti.engine.runtime.Job;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class MockSpringAsyncExecutor extends DefaultAsyncJobExecutor {

	@Autowired
	protected DataSource dataSource;

	@Override
	public boolean executeAsyncJob(Job job) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		List<Map<String, Object>> list = jdbcTemplate.queryForList("SELECT * FROM act_ru_job WHERE id_ = " + job.getId());
		assertEquals( list.size(), 1 );
		assertEquals( list.get(0).get("ID_"), job.getId());
		return false;
	}
}
