/*
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 * License rights for this program may be obtained from Alfresco Software, Ltd.
 * pursuant to a written agreement and any use of this program without such an
 * agreement is prohibited.
 */
'use strict';

angular.module('activitiAdminApp')
    .constant('gridConstants', {
        defaultTemplate : '<div><div class="ngCellText" title="{{row.getProperty(col.field)}}">{{row.getProperty(col.field)}}</div></div>',
        dateTemplate : '<div><div class="ngCellText" title="{{row.getProperty(col.field) | dateformat:\'full\'}}">{{row.getProperty(col.field) | dateformat}}</div></div>',
        userTemplate : '<div><div class="ngCellText" title="{{row.getProperty(col.field)}}" username="row.getProperty(col.field)"></div></div>',
        groupTemplate : '<div><div class="ngCellText" title="{{row.getProperty(col.field)}}" groupname="row.getProperty(col.field)"></div></div>',
        userObjectTemplate : '<div><div class="ngCellText" title="{{row.getProperty(col.field)}}" user="row.getProperty(col.field)"></div></div>'
    });


    