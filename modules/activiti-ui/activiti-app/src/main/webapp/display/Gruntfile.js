/*
 * Activiti app component part of the Activiti project
 * Copyright 2005-2015 Alfresco Software, Ltd. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
'use strict';

module.exports = function (grunt) {

    require('load-grunt-tasks')(grunt);
    require('time-grunt')(grunt);

    grunt.initConfig({
        yeoman: {
            app: require('./package.json').appPath || 'app',
            dist: 'dist'
        },
        clean: {
            dist: {
                files: [
                    {
                        dot: true,
                        src: [
                            '.tmp',
                            '<%= yeoman.dist %>/*',
                            '!<%= yeoman.dist %>/.git*'
                        ]
                    }
                ]
            },
            server: '.tmp'
        },
        useminPrepare: {
            html: 'displaymodel.html',
            options: {
                dest: '<%= yeoman.dist %>/'
            }
        },
        usemin: {
            html: ['<%= yeoman.dist %>/{,*/}*.html'],
            css: ['<%= yeoman.dist %>/display/styles/{,*/}*.css'],
            options: {
                dirs: ['<%= yeoman.dist %>']
            }
        },
        // Put files not handled in other tasks here
        copy: {
          dist: {
            files: [{
              expand: true,
              dot: true,
              cwd: '.',
              dest: '<%= yeoman.dist %>',
              src: [
                'fonts/*'
              ]
            }, {
              expand: true,
              cwd: '.tmp/images',
              dest: '<%= yeoman.dist %>/images',
              src: [
                'generated/*'
              ]
            }]
          },
          styles: {
            expand: true,
            cwd: 'styles',
            dest: '.tmp/styles/',
            src: '{,*/}*.css'
          },
          index: {
            expand: true,
            cwd: './',
            src: ['*.html', 'views/**/**.html'],
            dest: '<%= yeoman.dist %>'
          },
          copyCss : {
            files: [
          {expand: true, cwd:'.tmp/concat/display/styles/', src:'*.css', dest:'<%= yeoman.dist %>/display/styles/', filter: 'isFile'}
            ]
          },
          copyJs : {
            files: [
          {expand: true, cwd:'.tmp/concat/display/scripts', src:'*.js', dest:'<%= yeoman.dist %>/display/scripts/', filter: 'isFile'}
            ]
          },
        },  
        ngAnnotate: {
            dist: {
                files: [
                    {
                        expand: true,
                        cwd: '.tmp/concat/display/scripts',
                        src: '*.js',
                        dest: '.tmp/concat/display/scripts'
                    }
                ]
            }
        },
        uglify: {
            dist: {
                options: {
                    mangle: true
                },
                files: {
                    '<%= yeoman.dist %>/display/scripts/displaymodel-logic.js': [
                        '<%= yeoman.dist %>/display/scripts/displaymodel-logic.js'
                    ]
                }
            }
        },
        rev: {
            dist: {
                files: {
                    src: [
                        '<%= yeoman.dist %>/display/{,*/}*.js',
                        '<%= yeoman.dist %>/display/{,*/}*.css',
                        '<%= yeoman.dist %>/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
                    ]
                }
            }
        }
    });

    grunt.registerTask('buildApp', [
        'clean:dist',
        'useminPrepare',
        'copy:styles',
        'concat',
        'copy:dist',
        'ngAnnotate',
        'copy:copyCss',
        'copy:copyJs',
        'copy:index',
        'uglify',
        'rev',
        'usemin'
    ]);


    grunt.registerTask('default', [
        'buildApp'
    ]);

};
