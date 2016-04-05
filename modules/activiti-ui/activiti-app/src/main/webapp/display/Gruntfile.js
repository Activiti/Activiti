/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
