NotificationEndpoint
====================

About
-----

This is a sample endpoint implementation that can be used as a sink for SOAP notification messages from Cloud42.
It simply prints out the received SOAP messages.

Usage
-----
To start, execute 

> java -cp bin/notificationEndpoint-1.0.jar de.jw.cloud42.util.notificationEndpoint.App 

from the command line.
The tool will listen to http://localhost:8085/monitor.

Optionally, it is possible to pass the URL of the desired endpoint as command line parameter. Call

> java -cp bin/notificationEndpoint-1.0.jar de.jw.cloud42.util.notificationEndpoint.App http://myendpoint/address


For convenience reasons, a batch script is provided in the folder "bin". Use it like

> run.bat [http://myendpoint/address]

where the argument "http://myendpoint/address" is optional.


