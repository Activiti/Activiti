alter table ACT_RU_EXECUTION add START_TIME_ TIMESTAMP(6) not null;
alter table ACT_RU_EXECUTION add START_USER_ID_ NVARCHAR2(255);

update ACT_GE_PROPERTY set VALUE_ = '6.0.0.2' where NAME_ = 'schema.version';
