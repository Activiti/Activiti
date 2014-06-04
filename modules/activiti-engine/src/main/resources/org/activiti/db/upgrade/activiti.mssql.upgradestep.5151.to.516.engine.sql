alter table ACT_RU_TASK
	add TASK_FORM_KEY_ nvarchar(255);
	
alter table ACT_RU_EXECUTION
	add NAME_ nvarchar(255);
	
	
update ACT_GE_PROPERTY set VALUE_ = '5.16-SNAPSHOT' where NAME_ = 'schema.version';
