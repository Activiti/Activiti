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
        files: [{
          dot: true,
          src: [
            '.tmp',
            '<%= yeoman.dist %>/*',
            '!<%= yeoman.dist %>/.git*'
          ]
        }]
      },
      server: '.tmp'
    },
    rev: {
      dist: {
        files: {
          src: [
            '<%= yeoman.dist %>/scripts/{,*/}*.js',
            '<%= yeoman.dist %>/styles/{,*/}*.css',
            '<%= yeoman.dist %>/fonts/*'
          ]
        }
      }
    },
    useminPrepare: {
      html: 'index.html',
      options: {
        dest: '<%= yeoman.dist %>'
      }
    },
    usemin: {
      html: ['<%= yeoman.dist %>/{,*/}*.html'],
      css: ['<%= yeoman.dist %>/styles/{,*/}*.css'],
      options: {
        dirs: ['<%= yeoman.dist %>']
      }
    },
    svgmin: {
      dist: {
        files: [{
          expand: true,
          cwd: 'images',
          src: '{,*/}*.svg',
          dest: '<%= yeoman.dist %>/images'
        }]
      }
    },
    htmlmin: {
      dist: {
        options: {
          /*removeCommentsFromCDATA: true,
          // https://github.com/yeoman/grunt-usemin/issues/44
          //collapseWhitespace: true,
          collapseBooleanAttributes: true,
          removeAttributeQuotes: true,
          removeRedundantAttributes: true,
          useShortDoctype: true,
          removeEmptyAttributes: true,
          removeOptionalTags: true*/
        },
        files: [{
          expand: true,
          cwd: '.',
          src: ['**.html', 'views/**.html'],
          dest: '<%= yeoman.dist %>'
        }]
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
            '*.{ico,png,txt}',
            '.htaccess',
            'images/{,*/}*.{gif,webp}',
            'fonts/*',
            'images/*',
            'images/tour/*',
            'images/form-builder/*',
            'libs/**',
            'editor-app/**',
            '!editor-app/**/*.js',
            'editor-app/libs/*.js'
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
        cwd: '.',
        src: ['*.html', 'views/**/**.html'],
        dest: '<%= yeoman.dist %>'
      },
      copyCss : {
      	files: [
			{expand: true, cwd:'.tmp/concat/styles/', src:'*.css', dest:'<%= yeoman.dist %>/styles/', filter: 'isFile'}
      	]
      },
      copyJs : {
      	files: [
			{expand: true, cwd:'.tmp/concat/scripts', src:'*.js', dest:'<%= yeoman.dist %>/scripts/', filter: 'isFile'}
      	]
      },

      // The css will be minified into one css file, as such the references to the fonts and
      // the images will be wrong. We simply copy them manually to the root so they can be found
      copyEditorFonts : {
            files: [
                {expand: true, cwd:'editor-app/fonts', src:'*.*', dest:'<%= yeoman.dist %>/fonts/', filter: 'isFile'}
            ]
        },
        copyEditorImages : {
            files: [
                {expand: true, cwd:'editor-app/images', src:'*.*', dest:'<%= yeoman.dist %>/images/', filter: 'isFile'}
            ]
        }
    },
    ngAnnotate: {
      options: {
        singleQuotes: true,
      },
      dist: {
        files: [{
          expand: true,
          cwd: '.tmp/concat/scripts',
          src: '*.js',
          dest: '.tmp/concat/scripts'
        }]
      }
    },
    uglify: {
      dist: {
        files: {
          '<%= yeoman.dist %>/scripts/scripts.js': [
            '<%= yeoman.dist %>/scripts/scripts.js'
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
    'usemin',

    // Following need to be done after the minifying and revving: the minified css will refer to these hardcoded
    'copy:copyEditorFonts',
    'copy:copyEditorImages'
  ]);


  grunt.registerTask('default', [
    'buildApp'
  ]);
  
};
