alter table ACT_RU_TASK 
    add CATEGORY_ varchar(255);

Call Sysproc.admin_cmd ('REORG TABLE ACT_RU_TASK');
        
drop index ACT_UNIQ_RU_BUS_KEY;
alter table ACT_RU_EXECUTION drop colum UNI_BUSINESS_KEY;
alter table ACT_RU_EXECUTION drop colum UNI_PROC_DEF_ID;

Call Sysproc.admin_cmd ('REORG TABLE ACT_RU_EXECUTION');

update ACT_GE_PROPERTY set VALUE_ = '5.15-SNAPSHOT' where NAME_ = 'schema.version';
