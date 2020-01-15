update ACT_GE_PROPERTY set VALUE_ = '7.1.0.1-M6' where NAME_ = 'schema.version';

alter table ACT_RU_TASK add column BUSINESS_KEY_ varchar(255);
