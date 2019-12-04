update ACT_GE_PROPERTY set VALUE_ = '5.17.0.1' where NAME_ = 'schema.version';

alter table ACT_RU_EXECUTION add LOCK_TIME_ TIMESTAMP(6);
	
