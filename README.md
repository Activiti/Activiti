Activiti
========

[![Join Us in Gitter](https://badges.gitter.im/Activiti/Activiti7.svg)](https://gitter.im/Activiti/Activiti7?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status Travis](https://travis-ci.com/Activiti/Activiti.svg?branch=master)](https://travis-ci.com/Activiti/Activiti)
[![Coverage Status](http://img.shields.io/codecov/c/github/Activiti/Activiti/master.svg?maxAge=86400)](https://codecov.io/gh/Activiti/Activiti)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/f859f1bf542b43cab64e1e85706e5243)](https://www.codacy.com/app/Activiti/Activiti?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=Activiti/Activiti&amp;utm_campaign=Badge_Grade)
[![ASL 2.0](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/Activiti/Activiti/blob/master/LICENSE.txt)
[![CLA](https://cla-assistant.io/readme/badge/Activiti/Activiti)](https://cla-assistant.io/Activiti/Activiti)
[![security status](https://www.meterian.com/badge/gh/Activiti/Activiti/security)](https://www.meterian.com/report/gh/Activiti/Activiti)
[![stability status](https://www.meterian.com/badge/gh/Activiti/Activiti/stability)](https://www.meterian.com/report/gh/Activiti/Activiti)

Homepage: http://activiti.org/


Activiti is a light-weight workflow and Business Process Management (BPM) Platform targeted at business people, developers and system admins. Its core is a super-fast and rock-solid BPMN 2 process engine for Java. It's open-source and distributed under the Apache license. Activiti runs in any Java application, on a server, on a cluster or in the cloud. It integrates perfectly with Spring, it is extremely lightweight and based on simple concepts. 

**__NOTE: We moved to the master branch all the content of the development branch that we were using to design and code the next major version of the project. If you want to contribute with version 6.x please look at the 6.x branch.__** 

Activiti JIRA: https://activiti.atlassian.net

If you want to read more about our Repositories structure you can read our [GitBook](https://activiti.gitbooks.io/activiti-7-developers-guide/content/).

Configuring IntelliJ
--------------------

* Force language level 8, to fail-fast when (accidentally) using features available only in newer Java versions.
    * Open menu *File*, menu item *Project Structure*
    * Click list item *Modules*, for each module, tab *Sources*, combobox *Language level* should be automatically set to `8.0 ...`

* Avoid that changes in some resources are ignored in the next run/debug (and you are forced to use mvn)
    * Open menu *File*, menu item *Settings*
    * Click tree item *Compiler*, textfield *Resource patterns*: change to `!?*.java` (remove other content)

* Avoid a `StackOverflowError` when building
    * Open menu *File*, menu item *Settings*
    * Click tree item *Compiler*, tree item *Java Compiler*, textfield *Additional command line parameters*
    * Add `-J-Xss1024k` so it becomes something like `-target 1.8 -J-Xss1024k`

* Recommended code style: use the [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html) with editorconfig
    * Download the IntelliJ code style xml from: [https://google.github.io/styleguide/intellij-java-google-style.xml]
    * Open menu *File*, menu item *Settings*, click tree item *Code Style*, click cogwheel and select *Import scheme*, then *IntelliJ code style xml*
    * Browse where you downloaded the xml and open it. Check that GoogleStyle is the active scheme.
        * Note: IntelliJ IDEA doesn't format your code automatically. You have to press Ctrl+Alt+L keyboard combination to trigger auto formatting when coding is done.
    * There's an `.editorconfig` what has definition for indents, file encoding, line endings. 
    * If you disable it, you need to set the file encoding and number of spaces correctly manually.
    * Eclipse code style xml: [https://google.github.io/styleguide/eclipse-java-google-style.xml]
    * Eclipse needs [editorconfig-eclipse](https://marketplace.eclipse.org/content/editorconfig-eclipse) plugin in order to support EditorConfig files.

* Set manually the correct file encoding (UTF-8 except for properties files) and end-of-line characters (unix):
    * Open menu *File*, menu item *Settings*
    * Click tree item *Code Style*, tree item *General*
        * Combobox *Line separator (for new files)*: `Unix`
    * Click tree item *File Encodings*
        * Combobox *IDE Encoding*: `UTF-8`
        * Combobox *Default encoding for properties files*: `ISO-8859-1`
            * Note: normal i18n properties files must be in `ISO-8859-1` as specified by the java `ResourceBundle` contract.

* Set manually the correct number of spaces when pressing tab:
    * Open menu *File*, menu item *Settings*
    * Click tree item *Code Style*, tree item *General*
    * Click tab *Java*
        * Checkbox *Use tab character*: `off`
        * Textfield *Tab size*: `4`
        * Textfield *Indent*: `4`
        * Textfield *Continuation indent*: `8`
    * Open tab *XML*
        * Checkbox *Use tab character*: `off`
        * Textfield *Tab size*: `2`
        * Textfield *Indent*: `2`
        * Textfield *Continuation indent*: `4`

* Set the correct file headers (do not include @author or a meaningless javadoc):
    * Open menu *File*, menu item *Settings*
    * Click tree item *File templates*, tab *Includes*, list item `File Header`
    * Remove the line *@author Your Name*.
        * We do not accept `@author` lines in source files, see FAQ below.
    * Remove the entire javadoc as automatically templated data is meaningless.

* Set the correct license header
    * Open menu *File*, menu item *Settings*
    * Click tree item *Copyright*, tree item *Copyright profiles*
        * Click button *+* to add a *Copyright profile*
        * Textfield *name*: `Alfresco, Inc. and/or its affiliates`
        * Textarea with content:
            ```
            Copyright $today.year Alfresco, Inc. and/or its affiliates.

            Licensed under the Apache License, Version 2.0 (the "License");
            you may not use this file except in compliance with the License.
            You may obtain a copy of the License at

                  http://www.apache.org/licenses/LICENSE-2.0

            Unless required by applicable law or agreed to in writing, software
            distributed under the License is distributed on an "AS IS" BASIS,
            WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
            See the License for the specific language governing permissions and
            limitations under the License.
            ```
        * Note: Do not start or end with a newline character
        * Note: Do not start with `/**`: it is not a valid javadoc.
    * Click tree item *Copyright*
        * Combobox *Default project copyright*: `Alfresco, Inc. and/or its affiliates`

FAQ
===

* Why do you not accept `@author` lines in your source code?
    * Because the author tags in the java files are a maintenance nightmare
        * A large percentage is wrong, incomplete or inaccurate.
        * Most of the time, it only contains the original author. Many files are completely refactored/expanded by other authors.
        * Git is accurate, that is the canonical source to find the correct author.

    * Because the author tags promote *code ownership*, which is bad in the long run.
        * If people work on a piece they perceive as being owned by someone else, they tend to:
            * only fix what they are assigned to fix, instead of everything that's broken
            * discard responsibility if that code doesn't work properly
            * be scared of stepping on the feet of the owner.

    * Credit to the authors is given:
        * with [Open Hub](https://www.openhub.net/p/activiti/contributors) which also has statistics
        * in [the GitHub web interface](https://github.com/activiti).

# Development commands

## Checkstyle

To check if your code style respect all the rules:
 
```bash
mvn checkstyle:check -DskipCheckstyle=false
```

## Site

To generate the maven site:

```bash
mvn clean site site:stage
```

the site will be generated at: `target/staging/index.html`
