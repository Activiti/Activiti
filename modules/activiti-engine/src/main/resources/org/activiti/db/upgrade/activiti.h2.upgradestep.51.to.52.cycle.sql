create table ACT_CY_CONN_CONFIG (
      ID_ varchar NOT NULL,
      PLUGIN_ID_ varchar NOT NULL,
      INSTANCE_NAME_ varchar NOT NULL,
      INSTANCE_ID_ varchar NOT NULL,
      USER_ varchar,
      GROUP_ varchar,
      VALUES_ clob,
      primary key (ID_)
);

drop table ACT_CY_CONFIG if exists;

create table ACT_CY_CONFIG (
	ID_ varchar NOT NULL,
	GROUP_ varchar NOT NULL,
	KEY_ varchar NOT NULL,
	VALUE_ clob,
	primary key (ID_)
);
