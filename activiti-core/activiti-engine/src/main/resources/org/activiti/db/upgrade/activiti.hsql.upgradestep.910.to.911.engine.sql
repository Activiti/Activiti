alter table ACT_RU_VARIABLE add CONTENT_STORE_NAME_ varchar(64);
alter table ACT_RU_VARIABLE add CONTENT_ID_ varchar(512);
update ACT_GE_PROPERTY set VALUE_ = '9.1.1' where NAME_ = 'schema.version';
