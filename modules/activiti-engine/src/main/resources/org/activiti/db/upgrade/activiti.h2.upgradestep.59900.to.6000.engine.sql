alter table ACT_RE_PROCDEF add column ENGINE_VERSION varchar(255);
update ACT_RE_PROCDEF set ENGINE_VERSION = 'activiti-5';

alter table ACT_RE_DEPLOYMENT add column ENGINE_VERSION varchar(255);
update ACT_RE_DEPLOYMENT set ENGINE_VERSION = 'activiti-5';

alter table ACT_RU_EXECUTION add column ROOT_PROC_INST_ID_ varchar(64);
alter table ACT_RU_EXECUTION
    add constraint ACT_FK_ROOT_PROCINST
    foreign key (ROOT_PROC_INST_ID_)
    references ACT_RU_EXECUTION;   

update ACT_GE_PROPERTY set VALUE_ = '6.0.0.0' where NAME_ = 'schema.version';
