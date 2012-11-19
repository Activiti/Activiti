alter table ACT_RE_PROCDEF 
    MODIFY KEY_ varchar(255) not null;

alter table ACT_RE_PROCDEF 
    MODIFY VERSION_ integer not null;
    
-- http://jira.codehaus.org/browse/ACT-1424    
alter table ACT_RU_JOB 
    MODIFY LOCK_EXP_TIME_ timestamp null;
    
alter table ACT_RE_DEPLOYMENT 
    add CATEGORY_ varchar(255);
    
alter table ACT_RE_PROCDEF
    add DESCRIPTION_ varchar(4000);

alter table ACT_RU_EXECUTION
    add constraint ACT_FK_EXE_PROCDEF 
    foreign key (PROC_DEF_ID_) 
    references ACT_RE_PROCDEF (ID_);
