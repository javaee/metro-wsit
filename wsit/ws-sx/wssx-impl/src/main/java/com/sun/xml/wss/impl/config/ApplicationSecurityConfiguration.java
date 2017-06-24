/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

/*
 * $Id: ApplicationSecurityConfiguration.java,v 1.2 2010-10-21 15:37:25 snajper Exp $
 */

package com.sun.xml.wss.impl.config;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.xml.wss.impl.policy.SecurityPolicy;
import com.sun.xml.wss.impl.policy.StaticPolicyContext;
import com.sun.xml.wss.impl.policy.SecurityPolicyContainer;

import com.sun.xml.wss.impl.PolicyTypeUtil;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.configuration.*;
import com.sun.xml.wss.impl.policy.mls.MessagePolicy;


/**
 * Represents an XWS-Security configuration object, corresponding to the
 * <code>&lt;xwss:JAXRPCSecurity&gt;</code> element (as defined in XWS-Security, 
 * configuration schema, xwssconfig.xsd).
 */
public class ApplicationSecurityConfiguration extends SecurityPolicyContainer {
    
    private static Logger log = Logger.getLogger(
    LogDomainConstants.WSS_API_DOMAIN,
    LogDomainConstants.CONFIGURATION_DOMAIN_BUNDLE);

    boolean bsp = false;
    
    boolean useCacheFlag = false;
    
    String envHandlerClassName = null;

    boolean optimize = false;

    private boolean retainSecHeader = false;
    private boolean resetMU = false;
       
    public ApplicationSecurityConfiguration() {}
    
    /**
     * Constructor
     * @param handlerClassName the class name of the SecurityEnvironment CallbackHandler
     */
    public ApplicationSecurityConfiguration(String handlerClassName) {
        this.envHandlerClassName = handlerClassName;
    }
    
    /**
     * set the SecurityEnvironment CallbackHandler
     * @param handlerClassName the class name of the SecurityEnvironment Callback Handler
     */
    public void setSecurityEnvironmentHandler(String handlerClassName) {
        this.envHandlerClassName = handlerClassName;
    }
    
    /**
     * get the SecurityEnvironment CallbackHandler
     * @return the class name of the SecurityEnvironment CallbackHandler
     */
    public String getSecurityEnvironmentHandler() {
        return this.envHandlerClassName;
    }
    
    /*
     * @return list of all ApplicationSecurityConfigurations
     * Note : to be called only on the Top level SecurityConfiguration
     */
    public Collection getAllTopLevelApplicationSecurityConfigurations() {

        return servicesList;
        /*
        if (allTopLevelApplicationSecurityConfigs != null)
            return allTopLevelApplicationSecurityConfigs;

        Collection keys = _ctx2PolicyMap.keySet();
        Iterator keyIterator = keys.iterator();
        Collection applicationSecConfig = new ArrayList();
        while (keyIterator.hasNext()) {
            StaticApplicationContext sac = (StaticApplicationContext) keyIterator.next();
            if(sac.isService()){
                ArrayList serviceElements = (ArrayList)_ctx2PolicyMap.get(sac);
                Iterator serviceIterator  = serviceElements.iterator();
                while(serviceIterator.hasNext()){
                    SecurityPolicy policy = (SecurityPolicy)serviceIterator.next();
                    if (PolicyTypeUtil.applicationSecurityConfiguration(policy)) {
                        applicationSecConfig.add(policy);
                    }
                }
            }
        }
        return applicationSecConfig;
        */
    }
    
    /*
     * @return list of all security policies
     */
    @SuppressWarnings("unchecked")
    public Collection getAllPolicies() {
        Collection c = _ctx2PolicyMap.values();
        Collection d = new ArrayList();
        Iterator itr   = c.iterator();
        while (itr.hasNext()) {
            ArrayList list = (ArrayList)itr.next();
            for(int i =0;i< list.size();i++){
                SecurityPolicy policy = (SecurityPolicy)list.get(i);
                if (PolicyTypeUtil.applicationSecurityConfiguration(policy)) {
                    d.addAll(((ApplicationSecurityConfiguration)policy).getAllPolicies());
                }else{
                    d.add(policy);
                }
            }
        }
        return d;
    }
    
    /*
     * @return list of all sender security policies
     */
    @SuppressWarnings("unchecked")
    public Collection getAllSenderPolicies() {
        Collection c = _ctx2PolicyMap.values();
        Collection d = new ArrayList();
        Iterator itr   = c.iterator();
        while (itr.hasNext()) {
            ArrayList list = (ArrayList)itr.next();
            for(int i =0;i< list.size();i++){
                SecurityPolicy policy = (SecurityPolicy)list.get(i);
                if (PolicyTypeUtil.applicationSecurityConfiguration(policy)) {
                    d.addAll(((ApplicationSecurityConfiguration)policy).getAllSenderPolicies());
                }else{
                    DeclarativeSecurityConfiguration dsc = (DeclarativeSecurityConfiguration) policy;
                    MessagePolicy mp = dsc.senderSettings();
                    d.add(mp);
                }
            }
        }
        return d;
    }
    
    /*
     * @return list of all receiver security policies
     */
    @SuppressWarnings("unchecked")
    public Collection getAllReceiverPolicies() {

        if (allReceiverPolicies != null)
            return allReceiverPolicies;

        Collection c = _ctx2PolicyMap.values();
        
        Collection d = new ArrayList();
        Iterator itr   = c.iterator();
        while (itr.hasNext()) {
            ArrayList list = (ArrayList)itr.next();
            for(int i =0;i< list.size();i++){
                SecurityPolicy policy = (SecurityPolicy)list.get(i);
                if (PolicyTypeUtil.applicationSecurityConfiguration(policy)) {
                    d.addAll(((ApplicationSecurityConfiguration)policy).getAllReceiverPolicies());
                }else{
                    if (PolicyTypeUtil.declarativeSecurityConfiguration(policy)) {
                        DeclarativeSecurityConfiguration dsc = (DeclarativeSecurityConfiguration) policy;
                        MessagePolicy mp = dsc.receiverSettings();
                        if ( (mp.getPrimaryPolicies().size() == 0 && mp.getSecondaryPolicies().size() == 0 ) ||
                                (mp.getPrimaryPolicies().size() != 0 && mp.getSecondaryPolicies().size() == 0 ) || 
                                (mp.getPrimaryPolicies().size() != 0 && mp.getSecondaryPolicies().size() != 0 ) ) {
                            d.add(mp);
                        }
                    } else {
                        //probably a DSP
                        d.add(policy);
                    }
                }
            }
        }       
        return d;
    }
    
    /**
     * @return true of if the Configuration is Empty
     */
    public boolean isEmpty() {
        return _ctx2PolicyMap.isEmpty();
    }
    
    /*
     * @return bsp boolean
     */
    public boolean isBSP() {
        return bsp;
    }
    
    /*
     * @param flag boolean (isBsp)
     */
    public void isBSP(boolean flag) {
        bsp = flag;
    }
    
    /*
     * @return useCache boolean
     */
    public boolean useCache() {
        return useCacheFlag;
    }
    
    /*
     * @param flag boolean (useCache)
     */
    public void useCache(boolean flag) {
        useCacheFlag = flag;
    }

    /*
     *@return the Retain Security Header Config Property
     */
    public boolean retainSecurityHeader() {
        return retainSecHeader;
    }
    
    /*
     *@param arg, set the retainSecurityHeader flag. 
     */
    public void retainSecurityHeader(boolean arg) {
        this.retainSecHeader = arg;
    }
    
    /*  map to store inferred ctx to policy mappings (for efficiency of lookup) */
    private Hashtable augmentedCtx2PolicyMap = new Hashtable();
 
    /* configuration for Single Service with No ports */
    private SecurityPolicy configForSingleServiceNoPorts = null;

    
    /*
     * Returns matching DeclarativeSecurityConfiguration (DSC)/DynamicSecurityPolicy (DSP)
     * for a given context according to the following algorithm:
     *
     *<PRE>
     *  SecurityPolicy sp = null; 
     *  if (context is an Operation Level Context) {
     *      if context has DSC|DSP {sp = DSC|DSP}
     *      if (sp == null) {
     *          context = enclosing context (Port Level Context)    
     *          if context has DSC|DSP {sp = DSC|DSP}
     *          if (sp == null) {
     *             context = enclosing context (Service Level Context)
     *             if context has DSC|DSP {sp = DSC|DSP}
     *          }
     *      }
     *  } else if (context is a Port Level Context) {
     *      if context has DSC|DSP {sp = DSC|DSP}
     *      if (sp == null) {
     *         context = enclosing context (Service Level Context)
     *         if context has DSC|DSP {sp = DSC|DSP}
     *      }
     *  }
     *  return sp;
     *</PRE>
     *
     * @param context the static policy identification context represented as a <code>StaticApplicationContext</code>
     * @return the resolved SecurityPolicy instance for the context, null otherwise.
     */
    @SuppressWarnings("unchecked")
    public SecurityPolicy getSecurityConfiguration(StaticApplicationContext context) {
        
        if (configForSingleServiceNoPorts != null)
           return configForSingleServiceNoPorts;

        SecurityPolicy sp = (SecurityPolicy)augmentedCtx2PolicyMap.get(context);
        if (sp != null)
            return sp;

        sp = getDSCORDSP((ArrayList)_ctx2PolicyMap.get(context));
        if (sp != null) {
            //Log at FINE here
            if (MessageConstants.debug) {
                log.log(Level.FINEST, "Learning a new mapping for Context= " + context);
            }
            augmentedCtx2PolicyMap.put(context, sp);
            return sp;
        }
        
        StaticApplicationContext ctx = new StaticApplicationContext(context);
        
        if (ctx.isOperation()) {
            ctx.setOperationIdentifier("");
            sp = getDSCORDSP((ArrayList)_ctx2PolicyMap.get(ctx));
            
            if (sp == null) {
                ctx.setPortIdentifier("");
                sp = getDSCORDSP((ArrayList)_ctx2PolicyMap.get(ctx));
            }
        } else if (ctx.isPort()) {
            ctx.setPortIdentifier("");
            sp = getDSCORDSP((ArrayList)_ctx2PolicyMap.get(ctx));
        }
        
        //learn a new mapping
        if (context != null && sp != null) {
            //Log at FINE here
            if (MessageConstants.debug) {
                log.log(Level.FINEST, "Learning a new mapping for Context= " + context);
            }
            augmentedCtx2PolicyMap.put(context, sp);
        }

        return sp;
    }
    
    
    //NON-API public methods to be set on the TopLevel
    // ApplicationSecurityConfiguration corresponding to
    // <JAXRPCSecurity> element.
    // These methods allow optimizing the most common config scenarios
    
    private boolean sSNP = false;
    private boolean hasOps = true;
    
    
    /* (non-Javadoc)
     *@return true if config has Operation Level Policies
     */
    public  boolean hasOperationPolicies() {
        return hasOps;
    }
    
    /* (non-Javadoc)
     *set to true if config has Operation Level Policies
     */
    public void hasOperationPolicies(boolean flag) {
        hasOps = flag;
    }
    
    /* (non-Javadoc)
     *set to true if config has single service with no ports
     */
    public void singleServiceNoPorts(boolean flag) {
        sSNP = flag;
    }

    public void resetMustUnderstand(boolean value) {
        this.resetMU = value;
    }
    
    public boolean resetMustUnderstand() {
        return this.resetMU; 
    }
    
    /* (non-Javadoc)
     *@return true if config has single Service and No Ports
     */
    private  boolean singleServiceNoPorts() {
        return sSNP;
    }
    
    
    private SecurityPolicy getDSCORDSP(ArrayList list) {
        
        if (list == null) {
            return null;
        }
        
        Iterator i = list.iterator();
        
        while (i.hasNext()) {
            SecurityPolicy policy = (SecurityPolicy) i.next();
            if (PolicyTypeUtil.applicationSecurityConfiguration(policy)) {
                return ((ApplicationSecurityConfiguration)policy).getDSCORDSP();
            }
        }
        
        return null;
    }
    
    private SecurityPolicy getDSCORDSP() {
        // iterate over the values and return the first encountered DSC or DP
        Collection c = _ctx2PolicyMap.values();
        Iterator i = c.iterator();
        while (i.hasNext()) {
            ArrayList al = (ArrayList)i.next();
            SecurityPolicy policy = (SecurityPolicy) al.iterator().next();
            if (PolicyTypeUtil.declarativeSecurityConfiguration(policy) ||
                 PolicyTypeUtil.dynamicSecurityPolicy(policy)) {
                return policy;
            }
        }
        return null;
    }

    /**
     * @return the type of the policy
     */
    public String getType() {
        return PolicyTypeUtil.APP_SEC_CONFIG_TYPE;
    }

    private Collection allReceiverPolicies = null;
    //private Collection allTopLevelApplicationSecurityConfigs = null;

    /* (non-Javadoc)
     * Internal Method to be called once only by IL/reader only on the
     * Top level ApplicationSecurityConfiguration
     */
    public void init() {
        setConfigForSingleServiceNoPorts();
        allReceiverPolicies = getAllReceiverPoliciesFromConfig();
        //allTopLevelApplicationSecurityConfigs =  getAllTopLevelApplicationSecurityConfigurations();
    }

    private  ArrayList servicesList = new ArrayList();

    //override from parent
    /**
     * Associate more than one SecurityPolicy with a StaticPolicyContext
     * @param ctx StaticPolicyContext
     * @param policy SecurityPolicy
     */
    @Override
    @SuppressWarnings("unchecked")
    public void setSecurityPolicy (StaticPolicyContext ctx, SecurityPolicy policy) {
        if (ctx instanceof StaticApplicationContext) {
            if (((StaticApplicationContext)ctx).isService() && 
                PolicyTypeUtil.applicationSecurityConfiguration(policy)) {
                servicesList.add(policy);
            }
        }

        super.setSecurityPolicy(ctx, policy);
    }

    /*
     * @return list of all receiver security policies
     */
    @SuppressWarnings("unchecked")
    private Collection getAllReceiverPoliciesFromConfig() {
        
        Collection d = new ArrayList();
        for(int i =0;i< servicesList.size();i++){
            ApplicationSecurityConfiguration policy = (ApplicationSecurityConfiguration)servicesList.get(i);
                d.addAll(policy.getAllReceiverPolicies());
        }       
        return d;
    }

    private void setConfigForSingleServiceNoPorts() {
  
        if (singleServiceNoPorts()) {
            Collection c = _ctx2PolicyMap.values();
            ArrayList al = (ArrayList)c.iterator().next();
            ApplicationSecurityConfiguration serviceConfig =
            (ApplicationSecurityConfiguration)al.iterator().next();
            configForSingleServiceNoPorts = serviceConfig.getDSCORDSP();
        }
    }

    public void isOptimized(boolean optimize) {
        this.optimize = optimize;
    }

    public boolean isOptimized() {
        return this.optimize;
    }
}
