update ACT_GE_PROPERTY set VALUE_ = '7.1.0.0' where NAME_ = 'schema.version';

alter table ACT_RE_DEPLOYMENT add column IF NOT EXISTS VERSION_ integer default 1;
alter table ACT_RE_DEPLOYMENT add column IF NOT EXISTS PROJECT_RELEASE_VERSION_ varchar(255);
