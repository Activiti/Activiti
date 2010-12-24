alter table ACT_RU_VARIABLE 
add TASK_ID_ varchar(64);

update ACT_GE_PROPERTY
set 
  VALUE_ = '5.1-SNAPSHOT',
  REV_ = 2
where
  NAME_ = 'schema.version';
