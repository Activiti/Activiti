/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
/**
 * 
 * Global fixed configuration values for Activiti Admin app.
 * 
 */
var ActivitiAdmin = {};
ActivitiAdmin.Config = {};

// General settings
ActivitiAdmin.Config.alert = {};
ActivitiAdmin.Config.alert.infoDisplayTime = 3000;
ActivitiAdmin.Config.alert.errorDisplayTime = 5000;

// Filter settings
ActivitiAdmin.Config.filter = {};
ActivitiAdmin.Config.filter.resultSizes = [10, 25, 50, 100, 100000000];    // fairly large number for 'all'
ActivitiAdmin.Config.filter.defaultResultSize = 25;
ActivitiAdmin.Config.filter.defaultOrder = "asc";
ActivitiAdmin.Config.filter.delay = 400;
