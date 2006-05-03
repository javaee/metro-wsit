/*
 * $Id: StaticApplicationContext.java,v 1.1 2006-05-03 22:57:46 arungupta Exp $
 */

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

package com.sun.xml.wss.impl.configuration;

import com.sun.xml.wss.impl.policy.StaticPolicyContext;

/**
 * This class represents the static context associated with any Security Policy elements
 * defined in a <code>xwss:JAXRPCSecurity</code> configuration.
 * The <code>xwss:JAXRPCSecurity</code> element supports Security Policies to be specifed
 * at three levels
 * <UL>
 *   <LI>At a JAXRPC <code>Service</code> level
 *   <LI>At a JAXRPC <code>Port</code> level
 *   <LI>At a JAXRPC <code>Operation</code> level
 * </UL>
 * Accordingly the class StaticApplicationContext has methods to identify if the context
 * represents a Service, Port or Operation, and stores the corresponding context identifiers
 */
public final class StaticApplicationContext implements StaticPolicyContext {

    private boolean isService = false;
    private boolean isPort = false;
    private boolean isOperation = false;

    private String UUID = "";
    private String contextRoot = "";

    private String serviceIdentifier = "";
    private String portIdentifier = "";
    private String operationIdentifier = "";

    
    /**
     * Default constructor
     */
    public StaticApplicationContext() {}

    /**
     * Copy constructor
     *@param context StaticApplicationContext
     */ 
    public StaticApplicationContext(StaticApplicationContext context) {
        copy (context);
    }

    /**
     *Set if this context represents a Service
     *@param isService set to true if this is a service level context
     */
    public void isService (boolean isService) {
        this.isService = isService;
    }

    /**
     *@return true if this context represents a Service
     */
    public boolean isService () {
        return this.isService;
    }

    /**
     *Set if this context represents a Port
     *@param isPort set to true if this is a port level context
     */
    public void isPort (boolean isPort) {
        this.isPort = isPort;
    }

    /**
     *@return true if this context represents a Port
     */
    public boolean isPort () {
        return this.isPort;
    }

    /**
     *Set if this context represents an Operation
     *@param isOperation set to true if this is an Operation level context
     */
    public void isOperation (boolean isOperation) {
        this.isOperation = isOperation;
    }

    /**
     *@return true if this context represents an Operation
     */
    public boolean isOperation () {
        return this.isOperation;
    }

    /**
     *Set the service identifier
     *@param service the Service Identifier
     */
    public void setServiceIdentifier (String service) {
        this.serviceIdentifier = service;
    }

    /**
     *@return the service identifier
     */
    public String getServiceIdentifier () {
        return this.serviceIdentifier;
    }

    /**
     *Set the port identifier
     *@param port the Port Identifier
     */
    public void setPortIdentifier (String port) {
        this.portIdentifier = port;
    }

    /**
     *@return the port identifier
     */
    public String getPortIdentifier () {
        return this.portIdentifier;
    }

    /**
     *Set the Operation identifier
     *@param operation the Operation Identifier
     */
    public void setOperationIdentifier (String operation) {
	isOperation (true);
        this.operationIdentifier = operation;
    }

    /**
     *@return the Operation identifier
     */
    public String getOperationIdentifier () {
        return this.operationIdentifier;
    }

    /**
     *Set the Unique ID associated with the Service context
     *@param uuid the unique id associated with the Service
     */
    public void setUUID (String uuid) {
        this.UUID = uuid;
    }

    /**
     *@return the Unique ID associated with the Service context
     */
    public String getUUID () {
        return this.UUID;
    }

    /**
     *@param ctxRoot the Application Context Root/Identifier for the application
     */
    public void setApplicationContextRoot (String ctxRoot) {
        this.contextRoot = ctxRoot;
    }

    /**
     *@return the Application Context Root/Identifier for the application (if any)
     */
    public String getApplicationContextRoot () {
        return this.contextRoot;
    }

    /**
     *Copy operator
     *@param ctx the StaticApplicationContext to copy from
     */
    public void copy (StaticApplicationContext ctx) {
        setUUID (ctx.getUUID ());
        setApplicationContextRoot (ctx.getApplicationContextRoot ());

        isService (ctx.isService ());
        isPort (ctx.isPort ());
        isOperation (ctx.isOperation ());

        setServiceIdentifier (ctx.getServiceIdentifier ());
        setPortIdentifier (ctx.getPortIdentifier ());
        operationIdentifier = ctx.getOperationIdentifier ();
    }

    /*
     *@return this context
     */
    /*public StaticApplicationContext getStaticContext () {
        return this;
    }*/

    /**
     * equals operator
     * @param obj the Object to be compared with this context for equality
     * @return true if the argument object is equal to this context
     */
    public boolean equals (Object obj) {
        if (obj instanceof StaticApplicationContext) {
            return  equals((StaticApplicationContext)obj);
        }
        return false;
    }

    /**
     * equals operator
     * @param ctx the StaticApplicationContext to be compared with this context for equality
     * @return true if the argument context is equal to this context
     */
    public boolean equals (StaticApplicationContext ctx) {
   
        boolean b1 =
                 (UUID.equalsIgnoreCase (ctx.getUUID())); /* &&
                  contextRoot.equalsIgnoreCase (ctx.getApplicationContextRoot()));*/
        if (!b1) return false;

        boolean b2 =
                 (serviceIdentifier.equalsIgnoreCase (ctx.getServiceIdentifier()) &&
                  portIdentifier.equalsIgnoreCase (ctx.getPortIdentifier()) &&
                  operationIdentifier.equalsIgnoreCase (ctx.getOperationIdentifier()));
        if (!b2) return false;

        return true;
    }

    // TODO : this hashcode is not unique, change it later, but it works for now
    // hashCode needs to be implemented by this class for the equals() operator to 
    // be called by the HashMap.get() method
    // equals() method on HashMap is only called if hashCode succeeds
    /**
     * @return hashcode for this context
     */
    public int hashCode() {
        return 
            UUID.hashCode() + serviceIdentifier.hashCode() + 
                portIdentifier.hashCode() + operationIdentifier.hashCode();
    }

    public String toString() {
        String ret =  "isService=" + isService + "\nisPort=" + isPort + "\nisOperation=" + isOperation +
                      "\nUUID=" + UUID + "\nserviceIdentifier=" + serviceIdentifier + 
                      "\nportIdentifier=" + portIdentifier + "\noperationIdentifier=" + operationIdentifier;
        return ret;
    }

}
