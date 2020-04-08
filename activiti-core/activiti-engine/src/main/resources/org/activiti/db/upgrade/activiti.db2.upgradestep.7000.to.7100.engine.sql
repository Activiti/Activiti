update ACT_GE_PROPERTY set VALUE_ = '7.0.0.0' where NAME_ = 'schema.version';

alter table ACT_RE_DEPLOYMENT add VERSION_ integer default 1;
Call Sysproc.admin_cmd ('REORG TABLE ACT_RE_DEPLOYMENT');

alter table ACT_RE_DEPLOYMENT add PROJECT_RELEASE_VERSION_ varchar(255);
Call Sysproc.admin_cmd ('REORG TABLE ACT_RE_DEPLOYMENT');
