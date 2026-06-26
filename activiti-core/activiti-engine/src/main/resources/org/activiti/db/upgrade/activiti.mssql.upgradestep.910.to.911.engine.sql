alter table ACT_RU_TASK
    add CLAIM_TOKEN_ varchar(64);

create index ACT_IDX_TASK_CLAIM_TOKEN on ACT_RU_TASK(CLAIM_TOKEN_);

update ACT_GE_PROPERTY set VALUE_ = '9.1.1' where NAME_ = 'schema.version';
