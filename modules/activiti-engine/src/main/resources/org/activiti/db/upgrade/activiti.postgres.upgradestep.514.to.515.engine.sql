alter table ACT_RU_TASK 
    add CATEGORY_ varchar(255);
    
alter table ACT_RU_EXECUTION drop constraint act_ru_execution_proc_def_id__business_key__key;    

update ACT_GE_PROPERTY set VALUE_ = '5.15-SNAPSHOT' where NAME_ = 'schema.version';
