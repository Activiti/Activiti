drop index ACT_IDX_HI_PRO_INST_END;
drop index ACT_IDX_HI_PRO_I_BUSKEY;
drop index ACT_IDX_HI_ACT_INST_START;
drop index ACT_IDX_HI_ACT_INST_END;
drop index ACT_IDX_HI_DETAIL_PROC_INST;
drop index ACT_IDX_HI_DETAIL_ACT_INST;
drop index ACT_IDX_HI_DETAIL_TIME;
drop index ACT_IDX_HI_DETAIL_NAME;
drop index ACT_IDX_HI_DETAIL_TASK_ID;
drop index ACT_IDX_HI_PROCVAR_PROC_INST;
drop index ACT_IDX_HI_PROCVAR_NAME_TYPE;

alter table ACT_HI_PROCINST
    drop constraint ACT_UNIQ_HI_BUS_KEY;
    
drop table ACT_HI_PROCINST if exists;
drop table ACT_HI_ACTINST if exists;
drop table ACT_HI_PROCVARIABLE if exists;
drop table ACT_HI_TASKINST if exists;
drop table ACT_HI_DETAIL if exists;
drop table ACT_HI_COMMENT if exists;
drop table ACT_HI_ATTACHMENT if exists;
