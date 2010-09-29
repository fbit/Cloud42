Cloud42 sample BPEL integration
===============================

Created with JDeveloper BPEL Designer

Version 2.0, 2008-11-27

Purpose
-------
This simple BPEL process demonstrates how to use the Cloud42 Web service API from within a BPEL process.

Description
-----------
The process requires AWS credentials (user id, key, secret key), a keypair to use and an image location (on Amazon S3) as input.

At first, it retrieves the EC2 imageId for the given AMI location by querying "listImages". 

Then, it checks whether an instance of this AMI is already running. 

If so, it returns the EC2 instanceId of the first instance of this AMI.

Otherwise an instance of the given AMI is started by invoking the "runInstance" function. In this case, the instanceId of the newly created instance is returned to the client.


The file "bpel sample.jpg" in this folder contains the graphical representation of the process.

Remarks
-------
This is a very simple process that only serves as small example. Advanced BPEL features like error handling are not implemented. Therefore, it expects correct input values, otherwise you will see some errors.
Also note that running an instance does not necessarily mean the instance is in running state when the process finishes. It's more likely that it is in state "pending" for a short time until it has booted. So running the process several times in a short time might result in more than one instance of the same AMI.


Installation and execution
--------------------------
The process was developed and tested using JDeveloper BPEL designer along with the Oracle BPEL Process Manager. It's recommended that you use the provided Jdeveloper project file to open the project with JDeveloper. 
However, it should be possible to deploy it to any other BPEL engine. In this case, simply adjust the build.properties file.

The Cloud42 WSDL file is expected at http://localhost:8080/Cloud42WS/Cloud42BaseService?wsdl.

Note
----
This example works with Cloud42 v1.1.0 . It was not tested with newer versions.