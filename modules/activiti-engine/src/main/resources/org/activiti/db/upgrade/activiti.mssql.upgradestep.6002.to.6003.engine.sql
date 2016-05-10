alter table ACT_RU_TASK add CLAIM_TIME_ datetime;

update ACT_GE_PROPERTY set VALUE_ = '6.0.0.3' where NAME_ = 'schema.version';
