/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package common;

import com.sun.xml.ws.security.trust.STSIssuedTokenFeature;
import com.sun.xml.ws.security.trust.impl.client.DefaultSTSIssuedTokenConfiguration;
import java.io.*;
import java.net.*;
import javax.annotation.Resource;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.WebServiceRef;

/**
 *
 * @author jiandong guo
 */
public class ClientServlet extends HttpServlet {
    @WebServiceRef(wsdlLocation = "http://localhost:8080/CalculatorApp/CalculatorWSService?wsdl")
    public CalculatorWSService service;
    
    @Resource
    protected WebServiceContext context;
    
    /** 
    * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
    * @param request servlet request
    * @param response servlet response
    */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<h2>Servlet ClientServlet at " + request.getContextPath () + "</h2>");
                DefaultSTSIssuedTokenConfiguration config = new DefaultSTSIssuedTokenConfiguration();
                STSIssuedTokenFeature feature = new STSIssuedTokenFeature(config);
                org.me.calculator.client.CalculatorWS port = service.getCalculatorWSPort(new WebServiceFeature[]{feature});

                int i = Integer.parseInt(request.getParameter("value1"));
                int j = Integer.parseInt(request.getParameter("value2"));

                config.setTokenType("urn:oasis:names:tc:SAML:1.0:assertion");
                MyClaims claims = new MyClaims();
                claims.addClaimType(MyClaims.ROLE);
                config.setClaims(claims);
                int result = port.add(i, j);
                
                out.println("<br/>");
                out.println("Result:");
                out.println("" + i + " + " + j + " = " + result);
                out.println("<br/>");
                config.setTokenType("urn:oasis:names:tc:SAML:2.0:assertion");
                MyClaims claims1 = new MyClaims();
                claims1.addClaimType(MyClaims.LOCALITY);
                config.setClaims(claims1);
                result = port.add(i, j);

                out.println("<br/>");
                out.println("Result:");
                out.println("" + i + " + " + j + " = " + result);
                ((Closeable)port).close();
            
        } finally { 
            out.close();
        }
    } 

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
    * Handles the HTTP <code>GET</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    } 

    /** 
    * Handles the HTTP <code>POST</code> method.
    * @param request servlet request
    * @param response servlet response
    */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
    * Returns a short description of the servlet.
    */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
    // </editor-fold>
}
