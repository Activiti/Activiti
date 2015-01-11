alter table ACT_RE_PROCDEF add column ENGINE_VERSION varchar(255);
update ACT_RE_PROCDEF set ENGINE_VERSION = 'activiti-5';

alter table ACT_RE_DEPLOYMENT add column ENGINE_VERSION varchar(255);
update ACT_RE_DEPLOYMENT set ENGINE_VERSION = 'activiti-5';

update ACT_GE_PROPERTY set VALUE_ = '5.17.0.3' where NAME_ = 'schema.version';
