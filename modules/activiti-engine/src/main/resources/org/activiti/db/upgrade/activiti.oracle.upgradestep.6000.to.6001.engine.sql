alter table ACT_RU_EXECUTION add (IS_MI_ROOT_ NUMBER(1,0) CHECK (IS_MI_ROOT_ IN (1,0)));

update ACT_GE_PROPERTY set VALUE_ = '6.0.0.1' where NAME_ = 'schema.version';
