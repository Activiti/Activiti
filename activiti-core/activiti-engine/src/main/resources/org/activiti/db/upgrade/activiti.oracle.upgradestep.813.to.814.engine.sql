begin
execute immediate 'DROP INDEX ACT_IDX_RE_PROCDEF_DEPLOYMENT_ID';
exception
   when others then
      null;
end;
/

create index ACT_IDX_RE_PROCDEF_DEPLOYMENT_ID on ACT_RE_PROCDEF(DEPLOYMENT_ID_ ASC, ID_ ASC);

update ACT_GE_PROPERTY set VALUE_ = '8.1.4' where NAME_ = 'schema.version';
