alter table ACT_RE_PROCDEF
    alter column KEY_ set not null;

alter table ACT_RE_PROCDEF
    alter column VERSION_ set not null;
    
alter table ACT_RE_DEPLOYMENT 
add CATEGORY_ varchar(255);

alter table ACT_RE_PROCDEF
    add DESCRIPTION_ varchar(4000);  
    
alter table ACT_RU_TASK
    add SUSPENSION_STATE_ integer;
    
update ACT_RU_TASK set SUSPENSION_STATE= 1; 
