alter table AT_RU_EXECUTION 
add SUSPENSION_STATE_ int;

alter table ACT_RE_PROCDEF
add SUSPENSION_STATE_ int;

alter table ACT_RE_PROCDEF
add REV_ int;

update ACT_RE_PROCDEF set REV_ = 1;
update ACT_RE_PROCDEF set SUSPENSION_STATE_ = 1;
update AT_RU_EXECUTION set SUSPENSION_STATE_ = 1;
