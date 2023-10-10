update ACT_GE_PROPERTY set VALUE_ = '8.1.0' where NAME_ = 'schema.version';

alter table ACT_RU_IDENTITY_LINK add column DETAILS_ BLOB;
alter table ACT_HI_IDENTITYLINK add column DETAILS_ BLOB;
