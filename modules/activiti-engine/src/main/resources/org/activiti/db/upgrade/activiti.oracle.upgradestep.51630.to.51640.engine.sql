alter table ACT_RU_VARIABLE
  modify (
    TEXT_ NVARCHAR2(4000),
    TEXT2_ NVARCHAR2(4000)
  );

update ACT_GE_PROPERTY set VALUE_ = '5.16.4.0' where NAME_ = 'schema.version';

create index ACT_IDX_HI_PROCVAR_TASK_ID on ACT_HI_VARINST(TASK_ID_);