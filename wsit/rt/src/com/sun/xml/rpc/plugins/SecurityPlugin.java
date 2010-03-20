/*
 * $Id: SecurityPlugin.java,v 1.1 2010-03-20 12:35:25 kumarjayanti Exp $
 */

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package com.sun.xml.rpc.plugins;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.FileInputStream;
import java.io.DataInputStream;
import java.io.ByteArrayInputStream;

import java.util.StringTokenizer;

import com.sun.xml.rpc.tools.plugin.ToolPlugin;
import com.sun.xml.rpc.tools.wscompile.UsageIf;
import com.sun.xml.rpc.tools.wscompile.ModelIf;
import com.sun.xml.rpc.tools.wscompile.TieHooksIf;
import com.sun.xml.rpc.tools.wscompile.StubHooksIf;
import com.sun.xml.rpc.tools.wscompile.ModelIf.ModelProperty;

import com.sun.xml.rpc.util.localization.Localizable;
import com.sun.xml.rpc.util.localization.LocalizableMessageFactory;

import com.sun.xml.rpc.processor.model.Port;
import com.sun.xml.rpc.processor.model.Model;
import com.sun.xml.rpc.processor.util.IndentingWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
                                                                                                         
public class SecurityPlugin extends ToolPlugin implements UsageIf, ModelIf,
    StubHooksIf, TieHooksIf {

    private File securityFile = null;
    private LocalizableMessageFactory messageFactory;

    private static final String sec_util = "secPgUtil";
    private static final String sec_util_pkg = "com.sun.xml.rpc.security";
    private static final String SECURITY_PROPERTY = "com.sun.xml.rpc.security";

    public SecurityPlugin() {
        messageFactory = new LocalizableMessageFactory("com.sun.xml.rpc.plugins.sec");
    }

    public Localizable getOptionsUsage() {
        return messageFactory.getMessage("sec.usage.options", (Object[]) null);
    }

    public Localizable getFeaturesUsage() {
        return null;
    }

    public Localizable getInternalUsage() {
        return null;
    }

    public Localizable getExamplesUsage() {
        return null;
    }

    public boolean parseArguments(String[] args, UsageError err) {
        securityFile = null;

        for (int i = 0; i < args.length; i++) {
            if (args[i] != null && args[i].equals("-security")) {
                if ((i + 1) < args.length) {
                    if (securityFile != null) {
                        err.msg =
                            messageFactory.getMessage(
                                "sec.duplicateOption",
                                new Object[] { "-security" });
                        return false;
                    }
                    args[i] = null;
                    securityFile = new File(args[++i]);
                    args[i] = null;
                } else {
                    err.msg =
                        messageFactory.getMessage(
                            "sec.missingOptionArgument",
                            new Object[] { "-security" });
                    return false;
                }
            }
        }

        return true;
    }

    public void updateModel(ModelProperty property) {
        if (securityFile != null) {
		property.attr = SECURITY_PROPERTY;
            try {
                DataInputStream in = new DataInputStream(new FileInputStream(securityFile));
                byte[] xmlBytes = new byte[(int)securityFile.length()];
                in.readFully(xmlBytes);
                in.close();

                /* validate security configuration */
                DocumentBuilderFactory factory =
                   new com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl();
                factory.setAttribute("http://apache.org/xml/features/validation/dynamic", Boolean.FALSE);                
                factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                                     "http://www.w3.org/2001/XMLSchema");
                InputStream is = SecurityPlugin.class.getResourceAsStream("xwssconfig.xsd");

                boolean validate = true;
                try {
                    InputStream isV = SecurityPlugin.class.getResourceAsStream("disablevalidation.xml");
                    if (isV != null)
                        validate = false;
                } catch (Exception e) {
                    //ignore
                }

                if (validate) {
                    factory.setAttribute("http://java.sun.com/xml/jaxp/properties/schemaSource", is);
                    factory.setValidating(true);
                    factory.setIgnoringComments(true);
                    factory.setNamespaceAware(true);
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    builder.setErrorHandler(new ErrorHandler(System.out));
                    ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlBytes);
                    builder.parse(inputStream);
                 }

                property.value = processString(new String(xmlBytes));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
       }
    }

    public void _preHandlingHook(Model model,
                                 IndentingWriter p,
		                     StubHooksState state)
                                 throws IOException {
	  String config = (String)model.getProperty(SECURITY_PROPERTY);
        if (config != null) {
            p.pln("//Generated by security plugin");
            writeCheckMustUnderstandInStub(p);
            p.pln(sec_util + "._preHandlingHook(state);");
            p.pln("super._preHandlingHook(state);");
            p.flush();
            state.superDone = true;
        }
    }

    public void _preRequestSendingHook(Model model,
                                       IndentingWriter p,
            		               StubHooksState state)
                                       throws IOException {
        String config = (String)model.getProperty(SECURITY_PROPERTY);
        if (config != null) {
       	p.pln("//Generated by security plugin");
            p.pln("super._preRequestSendingHook(state);");
            p.pln("bool = " + sec_util + "._preRequestSendingHook(state);");
            p.flush();
            state.superDone = true;
	  }
    }

    public void preHandlingHook(Model model,
                                IndentingWriter p,
                                TieHooksState state)
                                throws IOException {
        String config = (String)model.getProperty(SECURITY_PROPERTY);
        if (config != null) {
        	p.pln("//Generated by security plugin");
            writeCheckMustUnderstandInTie(p);
            p.plnI("try {");
            p.pln("if (!" + sec_util + ".preHandlingHook(state)) return false;");
            p.pOlnI("} catch (javax.xml.rpc.soap.SOAPFaultException sfe) {");
            p.pln("SOAPFaultInfo fault = new SOAPFaultInfo(sfe.getFaultCode(), sfe.getFaultString(), sfe.getFaultActor());");
            p.pln("reportFault(fault, state);");
            p.pln("return false;");
            p.pOln("}");
            p.pln("bool = super.preHandlingHook(state);");
            p.flush();
            state.superDone = true;
	  }
    }

    public void postResponseWritingHook(Model model,
                                        IndentingWriter p,
                                        TieHooksState state)
                                        throws IOException {
        String config = (String)model.getProperty(SECURITY_PROPERTY);
        if (config != null) {
        	p.pln("//Generated by security plugin");
            p.pln("super.postResponseWritingHook(state);");
            p.pln(sec_util + ".postResponseWritingHook(state);");
            p.flush();
            state.superDone = true;
        }
    }

    public void writeStubStatic(Model model, IndentingWriter p) throws IOException {
        // nop
    }

    public void writeTieStatic(Model model, IndentingWriter p) throws IOException {
        // nop  
    }

    public void writeStubStatic(Model model, Port port, IndentingWriter p) throws IOException {
        writeStatics(model, port, p, true);
    }

    public void writeTieStatic(Model model, Port port, IndentingWriter p) throws IOException {
        writeStatics(model, port, p, false);
    }

    private void writeStatics(Model model, Port port, IndentingWriter p, boolean isStub) throws IOException {
        String config = (String)model.getProperty(SECURITY_PROPERTY);
        if (config != null) {
			config = "\"[version 1.0 FCS]" + config + "\"";
        
           String decl = "private static " +
                          sec_util_pkg + ".SecurityPluginUtil " +
                          sec_util + ";";
           String block_begin = "static {";
           String block_body1 = "try {";
           String block_body2 = sec_util +
                                " = new " +
                                sec_util_pkg + ".SecurityPluginUtil(" +
                                config + 
                                ", \"" + port.getName() + "\"" +
                                ", " + Boolean.valueOf(isStub) + ");";
           String block_body3 = "} catch (Exception e) {";
           String block_body4 = "e.printStackTrace();";
           String block_body5 = "throw new RuntimeException(e);";
           String block_body6 = "}";
           String block_end = "}";

           p.pln(decl);
           p.pln();
           p.plnI(block_begin);
           p.plnI(block_body1);
           p.flush();
           char[] array = block_body2.toCharArray();
           for (int i =0; i<array.length; i++) 
               p.write(array[i]);
           p.newLine();
           p.flush();
           p.pOlnI(block_body3);
           p.pln(block_body4);
           p.pOln(block_body5);
           p.pOln(block_body6);
           p.pOln(block_end);
           p.flush();
        }
    }

    private void writeCheckMustUnderstandInStub(IndentingWriter p) throws IOException {
        p.pln("// prepare message for check");
        p.pln(sec_util + ".prepareMessageForMUCheck(state.getResponse().getMessage());");
        p.pln();
        p.pln("com.sun.xml.rpc.client.HandlerChainImpl handlerChain = (com.sun.xml.rpc.client.HandlerChainImpl) state.getHandlerChain();");
        p.plnI("if (handlerChain != null && !handlerChain.isEmpty()) {");
        p.pln("boolean allUnderstood = handlerChain.checkMustUnderstand(state.getMessageContext());");
        p.plnI("if (allUnderstood == false) {");
        p.pln("throw new javax.xml.rpc.soap.SOAPFaultException(" +
              "com.sun.xml.rpc.encoding.soap.SOAPConstants.FAULT_CODE_MUST_UNDERSTAND," +
              "\"SOAP must understand error\"," +
              "_getActor()," +
              "null);");
        p.pOln("}");
        p.pOln("}");
        p.pln();
        p.pln("// restore message after check");
        p.pln(sec_util + ".restoreMessageAfterMUCheck(state.getResponse().getMessage());");
        p.flush();
    }

    private void writeCheckMustUnderstandInTie(IndentingWriter p) throws IOException {
        p.pln("// prepare message for check");
        p.pln(sec_util + ".prepareMessageForMUCheck(state.getRequest().getMessage());");
        p.pln();
        p.pln("com.sun.xml.rpc.client.HandlerChainImpl handlerChain = getHandlerChain();");
        p.plnI("if (handlerChain != null && !handlerChain.isEmpty()) {");
        p.pln("boolean allUnderstood = handlerChain.checkMustUnderstand(state.getMessageContext());");
        p.plnI("if (allUnderstood == false) {");
        p.pln("com.sun.xml.rpc.soap.message.SOAPFaultInfo fault = new " +
              "com.sun.xml.rpc.soap.message.SOAPFaultInfo(com.sun.xml.rpc.encoding.soap.SOAPConstants.FAULT_CODE_MUST_UNDERSTAND," +
              "\"SOAP must understand error\", getActor());");
        p.pln("reportFault(fault, state);");
        p.pln("state.getRequest().setHeaderNotUnderstood(true);");
        p.pln("state.setHandlerFlag(StreamingHandlerState.CALL_NO_HANDLERS);");
        p.pln("return false;");
        p.pOln("}");
        p.pOln("}");
        p.pln();
        p.pln("// restore message after check");
        p.pln(sec_util + ".restoreMessageAfterMUCheck(state.getRequest().getMessage());");
        p.flush();
    }

    private String processString(String config) {
        return replaceOthers(replaceOthers(replaceNewLine(config),
                                           "\"",
                                           "\\\""),
                                           " ",
                                           " ");

    }

    private String replaceNewLine(String config) {
        return config.replace('\r',' ').replace('\n',' ');
    }

    private String replaceOthers(String config, String delim, String append) {
        StringTokenizer strTokenizer = new StringTokenizer(config, delim);
        StringBuffer sbuf = new StringBuffer();
        while(strTokenizer.hasMoreTokens()) {
           String tok = strTokenizer.nextToken();
           sbuf.append(tok);
           // Assuming the string is regular xml -
           if (strTokenizer.hasMoreTokens()) {
              sbuf.append(append);
           }
        }
        return sbuf.toString();
    }

    private static class ErrorHandler extends DefaultHandler {
        PrintStream out;
                                                                                                                                                          
        public ErrorHandler(PrintStream out) {
            this.out = out;
        }
                                                                                                                                                          
        public void error(SAXParseException e) throws SAXException {
            if (out != null)
                out.println(e);
            throw e;
        }
                                                                                                                                                          
        public void warning(SAXParseException e) throws SAXException {
            if (out != null)
                out.println(e);
            else
                ;// log
        }
                                                                                                                                                          
        public void fatalError(SAXParseException e) throws SAXException {
            if (out != null)
                out.println(e);
            throw e;
        }
    }

}
