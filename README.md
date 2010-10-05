Cloud42 - Management Framework for Amazon EC2
===========================================

Cloud42 is an Open Source management framework for Amazon EC2, Eucalyptus or any other cloud system compatible to the EC2 API, allowing you to easily manage and monitor your cloud instances.

The Cloud42 web application provides a web-based, rich graphical user interface (GUI) that can be used to control and administrate AMIs and instances.

Apart from the GUI, Cloud42 offers a well-designed, high-level Web service interface, thus allowing to invoke all its functionalities from within other applications or even to orchestrate EC2 instances using BPEL processes. Because of the Web service interface, Cloud42 is especially interesting for developers, too.

An introduction into Cloud42 as well as detailed documentation can be found on ***[the official project website](http://cloud42.net)***.

You can download the binaries from the [download page](http://cloud42.net/download.php).

License
-------

Cloud42 is licensed under the GNU Lesser General Public License ([LGPL v3](http://www.opensource.org/licenses/lgpl-3.0.html)).



Installing the Binary Distribution
-------------------------------

The easiest way to get Cloud42 up and running is to use the binary distribution from the [download page](http://cloud42.net/download.php). 

This description assumes you have already installed Java 1.6 and Apache Tomcat 6.x or Jetty 6.x. Please note that Cloud42 was designed to work with one of these containers. It will run on other Servlet containers or newer versions of Tomcat/Jetty, too, but you may have to add or remove some libraries bundled with Cloud42 to get it working.

1. Unzip the files to any folder on your hard drive.
2. ***If you want to use Cloud42 with Eucalyptus or any other cloud backend other than Amazon EC2:***: Configure Cloud42.
   Cloud42 needs to know the endpoint to connect to. You can specify this in a settings file that must be located at /etc/cloud42/config.properties or in your home folder (on Windows!). An example of this file comes with the binary distribution. Please copy it to the destination folder mentioned above and adjust it to your needs.
    
3. Start the database. To run Cloud42, you have to start a HSQLDB database in server mode. The database system is shipped with Cloud42 and can be found in a subdirectory of the distribution.

   Open a console window, go to the subdirectory tools/hsqldb/database and type

   > java -classpath ../lib/hsqldb.jar org.hsqldb.Server

      .
4. Now you can deploy the WAR-files of Cloud42 in Tomcat 6.x or Jetty 6.x.

   Note that you can install the GUI and the Web service interface of Cloud42 fully independent from each other. Just deploy the WAR-file(s) you need. The file cloud42.war contains the Web application for the graphical part of the management framework, whereas the file cloud42WS.war contains the Web service interface of Cloud42.

That's it!

To test your installation, browse to http://localhost:8080/Cloud42 to access the GUI part of Cloud42.

The WSDLs for the Web service interface can be found at http://localhost:8080/Cloud42WS/Cloud42BaseService?wsdl, http://localhost:8080/Cloud42WS/Cloud42FileService?wsdl, http://localhost:8080/Cloud42WS/Cloud42RemotingService?wsdl and http://localhost:8080/Cloud42WS/Cloud42NotificationService?wsdl . 

Installing from Source Code
--------------------------

This section describes how to build Cloud42 from source.

0. Get the source from our [Github repository](http://github.com/fbit/Cloud42).

1. Go download&configure Maven 2 if you don't have it yet: [Download Maven](http://maven.apache.org/download.html). Cloud42 requires Java. It was developed using version 1.6, but it should be able to run on 1.5.x as well. In any case, check your Java installation and make sure your JAVA_HOME environment variable is set.

2. ***This step is only required if you want to use Cloud42 with Eucalyptus or any other cloud backend other than Amazon EC2:***

   You have to configure Cloud42 to connect to a server different from "ec2.amazonaws.com". For this purpose, we provided a (sample) configuration file located at core/service/src/main/resources/config.properties. You should copy the file to /etc/cloud42/config.properties or into your home folder (on Windows!). Please read the comments in the file carefully and adjust all(!) the settings to your needs.

3. Cloud42 needs to know your AWS credentials to pass its unit tests. To create the configuration, copy the file core/module-configuration/src/main/resources/access.properties to /etc/cloud42 or into your home folder, open it and fill in your keys.

   Note: This file also contains an option allowing you to enable all tests, including the ones that require starting and stopping instances (and therefore cause costs). Adjust it to your needs.

   Note 2: If you do not know your credentials or even do not have an AWS account yet, building Cloud42 is possible though by skipping the tests. Simply add the parameter -Dmaven.test.skip to the "mvn clean install" mentioned below.


4. Now execute a
   > mvn clean install

   from the root directory (the directory that contains this README file)

5. Start the database. To run Cloud42, you have to start the HSQL database in server mode. Go to tools/hsqldb/database and type
   > java -classpath ../lib/hsqldb.jar org.hsqldb.Server

6. Run webapp:

   Now you can start the web application using Jetty, a lightweight container. From the webapp folder, run
   > mvn jetty:run

   Browse to http://localhost:8080/Cloud42 and enjoy.

7. Run webservice

   The webservice is a simple web application that includes the Axis2 Servlet. This means, you can deploy it without any installations or configurations.
   From the webservice folder, type
   > mvn jetty:run

   The WSDLs can be found at http://localhost:8080/Cloud42WS/Cloud42BaseService?wsdl, http://localhost:8080/Cloud42WS/Cloud42FileService?wsdl, http://localhost:8080/Cloud42WS/Cloud42RemotingService?wsdl and http://localhost:8080/Cloud42WS/Cloud42NotificationService?wsdl .

   Note: if the web application from previous step is still running, you may want to use another port for the webservice. To do this, add the option -Djetty.port=8081 to your command.


8. If you want to use the webapp or the webservice with any application server of your choice, just deploy the .war files from the target folders in webapp/ or webservice/.


Working with the code
---------------------
  
* In order to build Eclipse project files simply execute
  > mvn eclipse:clean eclipse:eclipse
  
  from the root folder and import the resulting project files into your Eclipse workspace.
  Don't forget to set a classpath variable in the Eclipse IDE for M2_REPO pointing to your local maven repository.

* If you want to generate a project documentation including JavaDoc, execute the following steps:
  > mvn site
  
  from the root folder to create the maven project site. Then:
  > mvn site:stage -DstagingDirectory="<your_folder>"
  
  to deploy it to a folder of your choice. Warning: both of these steps are very time-consuming!



Misc
----

* In the folder "BPEL" a sample BPEL process invoking Cloud42 is provided. A corresponding readme file can be found there, too.

* The folder utils/NotificationEndpoint contains a tool that is very useful for testing the Cloud42 notification mechanism. See the corresponding readme file.
