update ACT_GE_PROPERTY
set VALUE_ = '7.1.0.0'
where NAME_ = 'schema.version';

alter table ACT_RE_DEPLOYMENT
  add VERSION_ INTEGER default 1;
alter table ACT_RE_DEPLOYMENT
  add PROJECT_RELEASE_VERSION_ NVARCHAR2(255);
