alter table ACT_RU_TASK 
    add CATEGORY_ NVARCHAR2(255);
    
drop index ACT_UNIQ_RU_BUS_KEY;  

alter table ACT_RE_DEPLOYMENT 
    add TENANT_ID_ NVARCHAR2(255) default '';  
    
alter table ACT_RE_PROCDEF 
    add TENANT_ID_ NVARCHAR2(255) default ''; 
    
alter table ACT_RU_EXECUTION
    add TENANT_ID_ NVARCHAR2(255) default '';  
    
alter table ACT_RU_TASK
    add TENANT_ID_ NVARCHAR2(255) default '';
    
alter table ACT_RU_JOB
    add TENANT_ID_ NVARCHAR2(255) default ''; 
    
alter table ACT_RE_MODEL
    add TENANT_ID_ NVARCHAR2(255) default ''; 
    
alter table ACT_RU_EVENT_SUBSCR
   add TENANT_ID_ NVARCHAR2(255) default '';  
   
alter table ACT_RU_EVENT_SUBSCR
   add PROC_DEF_ID_ NVARCHAR2(64);   
    
alter table ACT_RE_PROCDEF
    drop constraint ACT_UNIQ_PROCDEF;
    
begin
  execute immediate 'drop index ACT_UNIQ_PROCDEF';
exception
  when others then
    if sqlcode != -1418 then
      raise;
    end if;
end;
/
    
alter table ACT_RE_PROCDEF
    add constraint ACT_UNIQ_PROCDEF
    unique (KEY_,VERSION_, TENANT_ID_);          

update ACT_GE_PROPERTY set VALUE_ = '5.15' where NAME_ = 'schema.version';
