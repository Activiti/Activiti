alter table ACT_RE_PROCDEF
    modify KEY_ not null;

alter table ACT_RE_PROCDEF
    modify VERSION_ not null;
    
alter table ACT_RE_DEPLOYMENT 
    add CATEGORY_ NVARCHAR2(255);
    
alter table ACT_RE_PROCDEF
    add DESCRIPTION_ NVARCHAR2(4000);
    
alter table ACT_RU_TASK
    add SUSPENSION_STATE_ INTEGER;
    
update ACT_RU_TASK set SUSPENSION_STATE= 1; 
    