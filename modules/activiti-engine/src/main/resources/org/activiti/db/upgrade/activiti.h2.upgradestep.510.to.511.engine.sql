alter table ACT_RE_PROCDEF
    alter column KEY_ set not null;

alter table ACT_RE_PROCDEF
    alter column VERSION_ set not null;
    
alter table ACT_RE_DEPLOYMENT 
add CATEGORY_ varchar(255);
