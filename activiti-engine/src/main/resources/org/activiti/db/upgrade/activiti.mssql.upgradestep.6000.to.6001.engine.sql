alter table ACT_RU_EXECUTION add IS_MI_ROOT_ tinyint;

update ACT_GE_PROPERTY set VALUE_ = '6.0.0.1' where NAME_ = 'schema.version';
