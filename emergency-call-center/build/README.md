# Emergency Call Center - Build folder

For an introduction to the project, check [this link](..).

In this folder you can find all the requested libraries, configuration files and Activiti App packages (in ZIP format).
Starting from a standard Activiti installation, with `activiti-app` and `activiti-rest` up and running, follow the tasks below to install the Emergency Call Center.

* Stop Activiti Tomcat (or Application Server).

* Copy the `emergency-call-center-1.0.jar` file into the `<ativiti-app>/WEB-INF/lib` folder.

* Copy the `emergencyCallCenter.properties` file into a folder of the classpath (for example the `<tomcat>/lib`).

* From a terminal, move on the build folder and execute `./run_departments.sh`.

* Start Activiti again.

* Access to the the `Kickstart App` | `App` and click on `Import App`. A modal window will ask you to point on a ZIP file containing the App package to import. Select the `Emergency Call Center.zip` from the `build` folder.

* Once in the Emergency Call Center and Departments App, don't forget to click on the `Publish` button to make it available.

