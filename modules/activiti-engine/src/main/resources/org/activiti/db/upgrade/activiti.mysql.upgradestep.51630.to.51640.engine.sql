update ACT_GE_PROPERTY set VALUE_ = '5.16.4.0' where NAME_ = 'schema.version';

create index ACT_IDX_HI_PROCVAR_TASK_ID on ACT_HI_VARINST(TASK_ID_);

alter table ACT_HI_ATTACHMENT add TIME_ datetime(3)
