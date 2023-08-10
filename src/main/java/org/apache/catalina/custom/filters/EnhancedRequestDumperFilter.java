package org.apache.catalina.custom.filters;

import org.apache.catalina.custom.http.LoggedHttpRequestWrapper;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;

//  FOLLOWED ARTICLE
// https://tomcat.apache.org/tomcat-8.0-doc/config/filter.html#Request_Dumper_Filter

// CHANGE BELOW IN TOMCAT WEB.XML

/*
 *
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

*
*/

// Add in  CATALINA_BASE/conf/logging.properties

/*
 *
         # To this configuration below, 1request-dumper.org.apache.juli.FileHandler
         # also needs to be added to the handlers property near the top of the file
         1request-dumper.org.apache.juli.FileHandler.level = INFO
         1request-dumper.org.apache.juli.FileHandler.directory = ${catalina.base}/logs
         1request-dumper.org.apache.juli.FileHandler.prefix = request-dumper.
         1request-dumper.org.apache.juli.FileHandler.formatter = org.apache.juli.VerbatimFormatter
         org.apache.catalina.custom.filters.EnhancedRequestDumperFilter.level = INFO
         org.apache.catalina.custom.filters.EnhancedRequestDumperFilter.handlers = \
         1request-dumper.org.apache.juli.FileHandler
 *
 */

public class EnhancedRequestDumperFilter implements Filter {

    private static final String NON_HTTP_REQ_MSG =
            "Not available. Non-http request.";
    private static final String NON_HTTP_RES_MSG =
            "Not available. Non-http response.";

    private static final ThreadLocal<Timestamp> timestamp =
            new ThreadLocal<Timestamp>() {
                @Override
                protected Timestamp initialValue() {
                    return new Timestamp();
                }
            };

    private final Log log = LogFactory.getLog(EnhancedRequestDumperFilter.class); // must not be static

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Adding warning on startup
        System.out.println("======================== Intializing Request Dumper Filter =====================");
        System.out.println("Warning: Please check if it's knowingly enabled in tomcat otheriwse disable from tomcat web.xml as it can given performance hit.");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest hRequest = null;
        HttpServletResponse hResponse = null;

        if (request instanceof HttpServletRequest) {
            hRequest = (HttpServletRequest) request;
        }
        if (response instanceof HttpServletResponse) {
            hResponse = (HttpServletResponse) response;
        }

        // Log pre-service information
        doLog("START TIME        ", getTimestamp());

        if (hRequest == null) {
            doLog("        requestURI", NON_HTTP_REQ_MSG);
            doLog("          authType", NON_HTTP_REQ_MSG);
        } else {
            doLog("        requestURI", hRequest.getRequestURI());
            doLog("          authType", hRequest.getAuthType());
        }

        doLog(" characterEncoding", request.getCharacterEncoding());
        doLog("     contentLength",
                Long.toString(request.getContentLengthLong()));
        doLog("       contentType", request.getContentType());

        if (hRequest == null) {
            doLog("       contextPath", NON_HTTP_REQ_MSG);
            doLog("            cookie", NON_HTTP_REQ_MSG);
            doLog("            header", NON_HTTP_REQ_MSG);
        } else {
            doLog("       contextPath", hRequest.getContextPath());
            Cookie cookies[] = hRequest.getCookies();
            if (cookies != null) {
                for (int i = 0; i < cookies.length; i++) {
                    doLog("            cookie", cookies[i].getName() +
                            "=" + cookies[i].getValue());
                }
            }
            Enumeration<String> hnames = hRequest.getHeaderNames();
            while (hnames.hasMoreElements()) {
                String hname = hnames.nextElement();
                Enumeration<String> hvalues = hRequest.getHeaders(hname);
                while (hvalues.hasMoreElements()) {
                    String hvalue = hvalues.nextElement();
                    doLog("            header", hname + "=" + hvalue);
                }
            }
        }

        doLog("            locale", request.getLocale().toString());

        if (hRequest == null) {
            doLog("            method", NON_HTTP_REQ_MSG);
        } else {
            doLog("            method", hRequest.getMethod());
        }

        Enumeration<String> pnames = request.getParameterNames();
        while (pnames.hasMoreElements()) {
            String pname = pnames.nextElement();
            String[] pvalues = request.getParameterValues(pname);
            StringBuilder result = new StringBuilder(pname);
            result.append('=');
            for (int i = 0; i < pvalues.length; i++) {
                if (i > 0) {
                    result.append(", ");
                }
                result.append(pvalues[i]);
            }
            doLog("         parameter", result.toString());
        }

        if (hRequest == null) {
            doLog("          pathInfo", NON_HTTP_REQ_MSG);
        } else {
            doLog("          pathInfo", hRequest.getPathInfo());
        }

        doLog("          protocol", request.getProtocol());

        if (hRequest == null) {
            doLog("       queryString", NON_HTTP_REQ_MSG);
        } else {
            doLog("       queryString", hRequest.getQueryString());
        }

        doLog("        remoteAddr", request.getRemoteAddr());
        doLog("        remoteHost", request.getRemoteHost());

        if (hRequest == null) {
            doLog("        remoteUser", NON_HTTP_REQ_MSG);
            doLog("requestedSessionId", NON_HTTP_REQ_MSG);
        } else {
            doLog("        remoteUser", hRequest.getRemoteUser());
            doLog("requestedSessionId", hRequest.getRequestedSessionId());
        }

        doLog("            scheme", request.getScheme());
        doLog("        serverName", request.getServerName());
        doLog("        serverPort",
                Integer.toString(request.getServerPort()));

        if (hRequest == null) {
            doLog("       servletPath", NON_HTTP_REQ_MSG);
        } else {
            doLog("       servletPath", hRequest.getServletPath());
        }

        doLog("          isSecure",
                Boolean.valueOf(request.isSecure()).toString());

        //TODO IMPT Added hook logic for POST, PUT, PATCH. Rest taken from RequestDumperFilter.java
        if (hRequest != null && Arrays.asList("POST", "PUT", "PATCH").contains(hRequest.getMethod())) {
            log.info("============= Request Body ============");
            log.info(hRequest.getRequestURI());
            request = new LoggedHttpRequestWrapper(hRequest, log);
        }

        doLog("------------------",
                "--------------------------------------------");

        chain.doFilter(request, response);

        // Log post-service information
        doLog("------------------",
                "--------------------------------------------");
        if (hRequest == null) {
            doLog("          authType", NON_HTTP_REQ_MSG);
        } else {
            doLog("          authType", hRequest.getAuthType());
        }

        doLog("       contentType", response.getContentType());

        if (hResponse == null) {
            doLog("            header", NON_HTTP_RES_MSG);
        } else {
            Iterable<String> rhnames = hResponse.getHeaderNames();
            for (String rhname : rhnames) {
                Iterable<String> rhvalues = hResponse.getHeaders(rhname);
                for (String rhvalue : rhvalues) {
                    doLog("            header", rhname + "=" + rhvalue);
                }
            }
        }

        if (hRequest == null) {
            doLog("        remoteUser", NON_HTTP_REQ_MSG);
        } else {
            doLog("        remoteUser", hRequest.getRemoteUser());
        }

        if (hResponse == null) {
            doLog("            status", NON_HTTP_RES_MSG);
        } else {
            doLog("            status",
                    Integer.toString(hResponse.getStatus()));
        }

        doLog("END TIME          ", getTimestamp());
        doLog("==================",
                "============================================");
    }

    @Override
    public void destroy() {

    }

    private void doLog(String attribute, String value) {
        StringBuilder sb = new StringBuilder(80);
        sb.append(Thread.currentThread().getName());
        sb.append(' ');
        sb.append(attribute);
        sb.append('=');
        sb.append(value);
        log.info(sb.toString());
    }

    private String getTimestamp() {
        Timestamp ts = timestamp.get();
        long currentTime = System.currentTimeMillis();

        if ((ts.date.getTime() + 999) < currentTime) {
            ts.date.setTime(currentTime - (currentTime % 1000));
            ts.update();
        }
        return ts.dateString;
    }

    private static final class Timestamp {
        private final Date date = new Date(0);
        private final SimpleDateFormat format =
                new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
        private String dateString = format.format(date);

        private void update() {
            dateString = format.format(date);
        }
    }


}