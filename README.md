###### **EnhancedRequestDumperFilter**

This Project is for adding custom filter. It does log POST/PUT/PATCH request bodies as well. For request body and other details dump logging.

The Request Dumper Filter logs information from the request and response objects and is intended to be used for debugging purposes.

The following entries in a web application's web.xml would enable the Request Dumper filter for all requests for that web application.

**Steps:**

1. Download jar from release/packages and add Jar to tomcat path **CATALINA_BASE/lib/tomcatCustomFilters-< VERSION >.jar** 

2. If the entries were added to CATALINA_BASE/conf/**web.xml**, the Request Dumper Filter would be enabled for all web applications.

    ```
    
         <filter>
            <filter-name>requestdumper</filter-name>
            <filter-class>
                org.apache.catalina.custom.filters.EnhancedRequestDumperFilter
            </filter-class>
          </filter>
          <filter-mapping>
            <filter-name>requestdumper</filter-name>
            <url-pattern>*</url-pattern>
          </filter-mapping>
    
    ```

3. The following entries in CATALINA_BASE/conf/**logging.properties** would create a separate log file for the Request Dumper Filter output.
    
    ```
         # To this configuration below, 1request-dumper.org.apache.juli.FileHandler
         # also needs to be added to the handlers property near the top of the file
         1request-dumper.org.apache.juli.FileHandler.level = INFO
         1request-dumper.org.apache.juli.FileHandler.directory = ${catalina.base}/logs
         1request-dumper.org.apache.juli.FileHandler.prefix = request-dumper.
         1request-dumper.org.apache.juli.FileHandler.formatter = org.apache.juli.VerbatimFormatter
         org.apache.catalina.custom.filters.EnhancedRequestDumperFilter.level = INFO
         org.apache.catalina.custom.filters.EnhancedRequestDumperFilter.handlers = \
         1request-dumper.org.apache.juli.FileHandler
    ```