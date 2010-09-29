Cloud42 - Management Framework for Amazon EC2
===========================================

An introduction into Cloud42 as well as detailed documentation can be found on ***[the official project website](http://cloud42.net)***.

You can download the binaries from the [download page](http://cloud42.net/download.php).

License
-------

Cloud42 is licensed under the GNU Lesser General Public License ([LGPL v3](http://www.opensource.org/licenses/lgpl-3.0.html)).


Building and Running Cloud42
--------------------------

This section describes how to build Cloud42 from source. If you just want to use it, you can download the binaries (see link above) and skip directly to point 7.

1. Go download&configure Maven 2 if you don't have it yet: [Download Maven](http://maven.apache.org/download.html). Cloud42 requires Java. It was developed using version 1.6, but it should be able to run on 1.5.x as well. In any case, check your Java installation and make sure your JAVA_HOME environment variable is set.

2. ***This step is only required if you want to use Cloud42 with Eucalyptus:***

   You have to configure Cloud42 to connect to a server different from "ec2.amazonaws.com". For this purpose, we provided a configuration file located at core/service/src/main/resources/config.properties. Please read the comments in this file carefully and adjust all(!) the settings to your needs. 

3. Cloud42 needs to know your AWS credentials to pass its unit tests. You have to enter them in the file core/module-configuration/src/main/resources/test-config.properties

   Note: This file also contains an option allowing you to enable all tests, including the ones that require starting and stopping instances (and therefore cause costs). Adjust it to your needs.

   Note 2: Don't worry, the configuration file containing your AWS credentials will not be included in the resulting WAR file when building Cloud42, since it is only referenced in scope "test".

   Note 3: If you do not know your credentials or even do not have an AWS account yet, building Cloud42 is possible though by skipping the tests. Simply add the parameter -Dmaven.test.skip to the "mvn clean install" mentioned below.


4. Now execute a
   > mvn clean install

   from the root directory (the directory that contains this file)

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

* If you want to test the Web service interface of Cloud42 on your local host, you can find some soapUI projects in the folder tools/soapUI. Don't forget to fill in your credentials in each request. Also note that some version of soapUI (the 2.5.x) return a SocketTimeoutException when starting the long-running process of bundling an AMI. However, this has no effect on the bundling process. In a real world scenario, you probably would invoke the bundling as an asynchronous call.
