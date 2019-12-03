alter table ACT_RU_EXECUTION add column IS_MI_ROOT_ boolean; 

update ACT_GE_PROPERTY set VALUE_ = '6.0.0.1' where NAME_ = 'schema.version';
