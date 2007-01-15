/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.xml.ws.transport.tcp.server.glassfish;

import com.sun.xml.ws.api.DistributedPropertySet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.ws.handler.MessageContext;

/**
 * @author Alexey Stashok
 */
public final class ServletFakeArtifactSet extends DistributedPropertySet {

    private static final PropertyMap model;

    private final HttpServletRequest request;
    private final HttpServletResponse response;
    
    static {
        model = parse(ServletFakeArtifactSet.class);
    }

    public DistributedPropertySet.PropertyMap getPropertyMap() {
        return model;
    }
    
    public ServletFakeArtifactSet(final String requestURL, final String servletPath) {
        request = createRequest(requestURL, servletPath);
        response = createResponse();
    }
    
    @Property(MessageContext.SERVLET_RESPONSE)
    public HttpServletResponse getResponse() {
        return response;
    }

    @Property(MessageContext.SERVLET_REQUEST)
    public HttpServletRequest getRequest() {
        return request;
    }

    private static HttpServletRequest createRequest(final String requestURL, final String servletPath) {
        return new FakeServletHttpRequest(requestURL, servletPath);
    }
    
    private static HttpServletResponse createResponse() {
        return new FakeServletHttpResponse();
    }

    public static final class FakeServletHttpRequest implements HttpServletRequest {
        private final StringBuffer requestURL;
        private final String requestURI;
        private final String servletPath;
        
        public FakeServletHttpRequest(final String requestURL, final String servletPath) {
            this.requestURI = requestURL;
            this.requestURL = new StringBuffer(requestURL);
            this.servletPath = servletPath;
        }
        
        public String getAuthType() {
            return null;
        }

        public Cookie[] getCookies() {
            return null;
        }

        public long getDateHeader(final String string) {
            return 0L;
        }

        public String getHeader(final String string) {
            return null;
        }

        public Enumeration getHeaders(final String string) {
            return null;
        }

        public Enumeration getHeaderNames() {
            return null;
        }

        public int getIntHeader(final String string) {
            return -1;
        }

        public String getMethod() {
            return "POST";
        }

        public String getPathInfo() {
            return null;
        }

        public String getPathTranslated() {
            return null;
        }

        public String getContextPath() {
            return null;
        }

        public String getQueryString() {
            return null;
        }

        public String getRemoteUser() {
            return null;
        }

        public boolean isUserInRole(final String string) {
            return true;
        }

        public Principal getUserPrincipal() {
            return null;
        }

        public String getRequestedSessionId() {
            return null;
        }

        public String getRequestURI() {
            return requestURI;
        }

        public StringBuffer getRequestURL() {
            return requestURL;
        }

        public String getServletPath() {
            return servletPath;
        }

        public HttpSession getSession(final boolean b) {
            return null;
        }

        public HttpSession getSession() {
            return null;
        }

        public boolean isRequestedSessionIdValid() {
            return true;
        }

        public boolean isRequestedSessionIdFromCookie() {
            return true;
        }

        public boolean isRequestedSessionIdFromURL() {
            return true;
        }

        public boolean isRequestedSessionIdFromUrl() {
            return true;
        }

        public Object getAttribute(final String string) {
            return null;
        }

        public Enumeration getAttributeNames() {
            return null;
        }

        public String getCharacterEncoding() {
            return null;
        }

        public void setCharacterEncoding(final String string) throws UnsupportedEncodingException {
        }

        public int getContentLength() {
            return 0;
        }

        public String getContentType() {
            return null;
        }

        public ServletInputStream getInputStream() throws IOException {
            return null;
        }

        public String getParameter(final String string) {
            return null;
        }

        public Enumeration getParameterNames() {
            return null;
        }

        public String[] getParameterValues(final String string) {
            return null;
        }

        public Map getParameterMap() {
            return null;
        }

        public String getProtocol() {
            return null;
        }

        public String getScheme() {
            return null;
        }

        public String getServerName() {
            return null;
        }

        public int getServerPort() {
            return 0;
        }

        public BufferedReader getReader() throws IOException {
            return null;
        }

        public String getRemoteAddr() {
            return null;
        }

        public String getRemoteHost() {
            return null;
        }

        public void setAttribute(final String string, final Object object) {
        }

        public void removeAttribute(final String string) {
        }

        public Locale getLocale() {
            return null;
        }

        public Enumeration getLocales() {
            return null;
        }

        public boolean isSecure() {
            return false;
        }

        public RequestDispatcher getRequestDispatcher(final String string) {
            return null;
        }

        public String getRealPath(final String string) {
            return null;
        }

        public int getRemotePort() {
            return 0;
        }

        public String getLocalName() {
            return null;
        }

        public String getLocalAddr() {
            return null;
        }

        public int getLocalPort() {
            return 0;
        }
    }
    
    public static final class FakeServletHttpResponse implements HttpServletResponse {
        public void addCookie(final Cookie cookie) {
        }

        public boolean containsHeader(final String string) {
            return true;
        }

        public String encodeURL(final String string) {
            return null;
        }

        public String encodeRedirectURL(final String string) {
            return null;
        }

        public String encodeUrl(final String string) {
            return null;
        }

        public String encodeRedirectUrl(final String string) {
            return null;
        }

        public void sendError(final int i, final String string) throws IOException {
        }

        public void sendError(final int i) throws IOException {
        }

        public void sendRedirect(final String string) throws IOException {
        }

        public void setDateHeader(final String string, final long l) {
        }

        public void addDateHeader(final String string, final long l) {
        }

        public void setHeader(final String string, final String string0) {
        }

        public void addHeader(final String string,final  String string0) {
        }

        public void setIntHeader(final String string, final int i) {
        }

        public void addIntHeader(final String string, final int i) {
        }

        public void setStatus(final int i) {
        }

        public void setStatus(final int i, final String string) {
        }

        public String getCharacterEncoding() {
            return null;
        }

        public String getContentType() {
            return null;
        }

        public ServletOutputStream getOutputStream() throws IOException {
            return null;
        }

        public PrintWriter getWriter() throws IOException {
            return null;
        }

        public void setCharacterEncoding(final String string) {
        }

        public void setContentLength(final int i) {
        }

        public void setContentType(final String string) {
        }

        public void setBufferSize(final int i) {
        }

        public int getBufferSize() {
            return 0;
        }

        public void flushBuffer() throws IOException {
        }

        public void resetBuffer() {
        }

        public boolean isCommitted() {
            return true;
        }

        public void reset() {
        }

        public void setLocale(final Locale locale) {
        }

        public Locale getLocale() {
            return null;
        }
        
    }
}
