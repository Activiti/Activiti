update ACT_GE_PROPERTY set VALUE_ = '7.1.0-M6' where NAME_ = 'schema.version';

alter table ACT_RE_PROCDEF add column APP_VERSION_ integer;
alter table ACT_RU_TASK add column APP_VERSION_ integer;
alter table ACT_RU_EXECUTION add column APP_VERSION_ integer;

alter table ACT_RU_TASK add column BUSINESS_KEY_ varchar(255);
