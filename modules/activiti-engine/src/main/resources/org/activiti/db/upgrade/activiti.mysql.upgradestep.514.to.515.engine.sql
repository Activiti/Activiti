alter table ACT_RU_TASK 
    add CATEGORY_ varchar(255);
    
alter table ACT_RU_EXECUTION drop foreign key ACT_FK_EXE_PROCDEF;  	

alter table ACT_RU_EXECUTION drop index ACT_UNIQ_RU_BUS_KEY;  

alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCDEF 
    foreign key (PROC_DEF_ID_) 
    references ACT_RE_PROCDEF (ID_);

update ACT_GE_PROPERTY set VALUE_ = '5.15-SNAPSHOT' where NAME_ = 'schema.version';
