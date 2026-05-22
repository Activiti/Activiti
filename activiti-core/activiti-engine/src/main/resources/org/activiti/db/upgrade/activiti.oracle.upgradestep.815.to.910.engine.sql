update ACT_RU_VARIABLE
   set TEXT2_ = replace(TEXT2_, 'com.fasterxml.jackson.', 'tools.jackson.')
 where TEXT2_ like 'com.fasterxml.jackson.%';

update ACT_HI_VARINST
   set TEXT2_ = replace(TEXT2_, 'com.fasterxml.jackson.', 'tools.jackson.')
 where TEXT2_ like 'com.fasterxml.jackson.%';

update ACT_HI_DETAIL
   set TEXT2_ = replace(TEXT2_, 'com.fasterxml.jackson.', 'tools.jackson.')
 where TEXT2_ like 'com.fasterxml.jackson.%';

update ACT_GE_PROPERTY set VALUE_ = '9.1.0' where NAME_ = 'schema.version';
