alter table ACT_RE_PROCDEF add column ENGINE_VERSION_ varchar(255);
update ACT_RE_PROCDEF set ENGINE_VERSION_ = 'activiti-5';

alter table ACT_RE_DEPLOYMENT add column ENGINE_VERSION_ varchar(255);
update ACT_RE_DEPLOYMENT set ENGINE_VERSION_ = 'activiti-5';

alter table ACT_RU_EXECUTION add column ROOT_PROC_INST_ID_ varchar(64);
create index ACT_IDX_EXEC_ROOT on ACT_RU_EXECUTION(ROOT_PROC_INST_ID_);

update ACT_GE_PROPERTY set VALUE_ = '6.0.0.0' where NAME_ = 'schema.version';
