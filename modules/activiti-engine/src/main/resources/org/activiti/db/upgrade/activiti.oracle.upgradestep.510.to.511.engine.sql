alter table ACT_RE_PROCDEF
    modify KEY_ not null;

alter table ACT_RE_PROCDEF
    modify VERSION_ not null;
    
alter table ACT_RE_DEPLOYMENT 
    add CATEGORY_ NVARCHAR2(255);
    
alter table ACT_RE_PROCDEF
    add DESCRIPTION_ NVARCHAR2(4000);