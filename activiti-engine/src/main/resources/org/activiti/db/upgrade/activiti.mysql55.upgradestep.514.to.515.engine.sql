# MySQL < 5.6.4 does not support timestamps/dates with millisecond precision.
# This upgrade file lacks the necessary upgrades that are done on a 5.6.4+ installation
# to get millisecond precision.


alter table ACT_RU_TASK 
    add CATEGORY_ varchar(255);
    
alter table ACT_RU_EXECUTION drop foreign key ACT_FK_EXE_PROCDEF;  	

alter table ACT_RU_EXECUTION drop index ACT_UNIQ_RU_BUS_KEY;  

alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCDEF 
    foreign key (PROC_DEF_ID_) 
    references ACT_RE_PROCDEF (ID_);
    
alter table ACT_RE_DEPLOYMENT 
    add TENANT_ID_ varchar(255) default ''; 
    
alter table ACT_RE_PROCDEF 
    add TENANT_ID_ varchar(255) default '';     
    
alter table ACT_RU_EXECUTION
    add TENANT_ID_ varchar(255) default '';    
    
alter table ACT_RU_TASK
    add TENANT_ID_ varchar(255) default '';  
    
alter table ACT_RU_JOB
    add TENANT_ID_ varchar(255) default '';   
    
alter table ACT_RE_MODEL
    add TENANT_ID_ varchar(255) default '';  
    
alter table ACT_RU_EVENT_SUBSCR
   add TENANT_ID_ varchar(255) default ''; 
   
alter table ACT_RU_EVENT_SUBSCR
   add PROC_DEF_ID_ varchar(64);           
    
alter table ACT_RE_PROCDEF
    drop index ACT_UNIQ_PROCDEF;
    
alter table ACT_RE_PROCDEF
    add constraint ACT_UNIQ_PROCDEF
    unique (KEY_,VERSION_, TENANT_ID_);      

update ACT_GE_PROPERTY set VALUE_ = '5.15' where NAME_ = 'schema.version';
