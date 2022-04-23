update ACT_GE_PROPERTY
set VALUE_ = '7.1.0-M6'
where NAME_ = 'schema.version';

alter table ACT_RE_PROCDEF
  add APP_VERSION_ int;
alter table ACT_RU_TASK
  add APP_VERSION_ int;
alter table ACT_RU_EXECUTION
  add APP_VERSION_ int;

alter table ACT_RU_TASK
  add BUSINESS_KEY_ nvarchar(255);
