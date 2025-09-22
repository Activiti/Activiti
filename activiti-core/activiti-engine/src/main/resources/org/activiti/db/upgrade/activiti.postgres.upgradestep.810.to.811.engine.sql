create index ACT_IDX_TASK_PARENT_TASK_ID on ACT_RU_TASK(PARENT_TASK_ID_);

update ACT_GE_PROPERTY set VALUE_ = '8.1.1' where NAME_ = 'schema.version';