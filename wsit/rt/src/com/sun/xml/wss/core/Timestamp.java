/*
 * $Id: Timestamp.java,v 1.1 2006-05-03 22:57:34 arungupta Exp $
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

package com.sun.xml.wss.core;

import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.XMLUtil;
import com.sun.xml.wss.XWSSecurityException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

import com.sun.xml.wss.impl.misc.SecurityHeaderBlockImpl;
import com.sun.xml.wss.impl.SecurityTokenException;
import com.sun.xml.wss.XWSSecurityException;

import org.w3c.dom.Node;

/**
 * @author XWS-Security RI Development Team
 */
public class Timestamp extends SecurityHeaderBlockImpl {

    private static Logger log =
    Logger.getLogger(
    LogDomainConstants.WSS_API_DOMAIN,
    LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

    // -------------------------- constants --------------------------
    private static String XSD_DATE_TIME = "xsd:dateTime";

    public static final long MAX_CLOCK_SKEW = 1200000; // milliseconds
    public static final long TIMESTAMP_FRESHNESS_LIMIT = 1200000; // milliseconds

    /*
     * /wsu:Timestamp/wsu:Created
     */
    private String created;
    
    private long timeout = 0;

    /*
     * /wsu:Timestamp/wsu:Created/@ValueType
     * optional attribute to specify the type of time data
     * default is "xsd:dateTime".
     */
    private String createdValueType = XSD_DATE_TIME;

    /*
     * /wsu:Timestamp/wsu:Expires
     * This is optional but can appear at most once in a Timestamp element.
     */
    private String expires = null;

    /*
     * /wsu:Timestamp/wsu:Expires/@ValueType
     */
    private String expiresValueType = XSD_DATE_TIME;

    /*
     * /wsu:Timestamp@wsu:Id
     */
    private String wsuId = null;

    public Timestamp() {
    }

    /**
     * Takes a SOAPElement and checks if it has the right name.
     */
    public Timestamp(SOAPElement element) throws XWSSecurityException {
        
        if (!(element.getLocalName().equals("Timestamp") &&
        XMLUtil.inWsuNS(element))) {
            log.log(Level.SEVERE, "WSS0385.error.creating.timestamp", element.getTagName());
            throw new XWSSecurityException("Invalid timestamp element passed");
        }

        setSOAPElement(element);

        // extract and initialize the values of the rest of the variables.
        Iterator children = element.getChildElements();
        while (children.hasNext()) {

            Node object = (Node)children.next();

            if (object.getNodeType() == Node.ELEMENT_NODE) {
                SOAPElement subElement = (SOAPElement) object;
                if ("Created".equals(subElement.getLocalName()) &&
                XMLUtil.inWsuNS(subElement)) {
                                        
                    if (isBSP() && created != null) {
                        // created is already present
                        log.log(Level.SEVERE,"BSP3203.Onecreated.Timestamp");
                        new XWSSecurityException("There can be only one wsu:Created element under Timestamp");
                    }
                        
                    created = subElement.getValue();
                    createdValueType = subElement.getAttribute("ValueType");
                    
                    if (isBSP() && createdValueType!=null && createdValueType.length() > 0) {
                        // BSP:R3225 @ValueType MUST NOT be present
                        log.log(Level.SEVERE,"BSP3225.createdValueType.Timestamp");
                        new XWSSecurityException("A wsu:Created element within a TIMESTAMP MUST NOT include a ValueType attribute.");
                    }                        
                    if ("".equalsIgnoreCase(createdValueType)) {
                        createdValueType = null;
                    }                    
                }

                if ("Expires".equals(subElement.getLocalName()) &&
                XMLUtil.inWsuNS(subElement)) {

                    if (isBSP() && expires != null) {
                        // expires is already present
                        log.log(Level.SEVERE,"BSP3224.Oneexpires.Timestamp");
                        new XWSSecurityException("There can be only one wsu:Expires element under Timestamp");
                    }
                    
                    if (isBSP() && created == null) {
                        // created is not present
                        log.log(Level.SEVERE,"BSP3221.CreatedBeforeExpires.Timestamp");
                        new XWSSecurityException("wsu:Expires must appear after wsu:Created in the Timestamp");
                    }
                    
                    expires = subElement.getValue();
                    // attr@ValueType
                    expiresValueType = subElement.getAttribute("ValueType");

                    if (isBSP() && expiresValueType != null && expiresValueType.length() > 0) {
                        // BSP:R3226 @ValueType MUST NOT be present
                        log.log(Level.SEVERE,"BSP3226.expiresValueType.Timestamp");
                        new XWSSecurityException("A wsu:Expires element within a TIMESTAMP MUST NOT include a ValueType attribute.");
                    }                        
                    if ("".equalsIgnoreCase(expiresValueType)) {
                        expiresValueType = null;
                    }
                }
            }
        }
        wsuId = element.getAttribute("wsu:Id");
        if ("".equalsIgnoreCase(wsuId)) {
            wsuId = null;
        }
    }

    /*
     * Get creation time.
     */
    public String getCreated() {
        return this.created;
    }

    /*
     * Set creation time.
     */
    public void setCreated(String created) {
        this.created = created;
    }

    /*
     * Get the type of timeData.
     */
    public String getCreatedValueType() {
        return this.createdValueType;
    }

    /*
     * Sets the type of timeData.
     */
    public void setCreatedValueType(String createdValueType) {
        this.createdValueType = createdValueType;
    }

    /**
     * The timeout is assumed to be in seconds
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /*
     * @return Get the expiration of the security semantics.
     */
    public String getExpires() {
        return this.expires;
    }

    /*
     * Set the expiration of the security sematics.
     */
    public void setExpires(String expires) {
        this.expires = expires;
    }

    /*
     * @return String Get the type of expires timeData.
     */
    public String getExpiresValueType() {
        return this.expiresValueType;
    }

    /*
     * @return Sets the type of expires timeData.
     */
    public void setExpiresValueType(String expiresValueType) {
        this.expiresValueType = expiresValueType;
    }

    /*
     * Get the type of timeData.
     */
    public String getId() {
        return this.wsuId;
    }

    /*
     *
     * @return Sets the wsu:Id attribute.
     */
    public void setId(String wsuId) {
        this.wsuId = wsuId;
    }

    // create the Element based on the values specified in the class.
    public SOAPElement getAsSoapElement() throws XWSSecurityException {

        createDateTime();

        SOAPElement timestamp;

        try {
            timestamp =
            getSoapFactory().createElement(
            "Timestamp",
            MessageConstants.WSU_PREFIX,
            MessageConstants.WSU_NS);

            timestamp.addNamespaceDeclaration(
            MessageConstants.WSU_PREFIX, MessageConstants.WSU_NS);

            SOAPElement createdElement =
            timestamp.addChildElement("Created", MessageConstants.WSU_PREFIX)
            .addTextNode(created);

            if (createdValueType!= null &&
            !XSD_DATE_TIME.equalsIgnoreCase(createdValueType))
                createdElement.setAttribute("ValueType", createdValueType);
            // BSP:R3225 MUST NOT include ValueType attribute

            if (expires != null) {
                SOAPElement expiresElement =
                timestamp
                .addChildElement("Expires", MessageConstants.WSU_PREFIX)
                .addTextNode(expires);

                if (expiresValueType!= null &&
                !XSD_DATE_TIME.equalsIgnoreCase(expiresValueType))
                    expiresElement.setAttribute("ValueType", expiresValueType);
                // BSP:R3226 - MUST NOT include valueType attribute
            }

            if (wsuId != null) {
                setWsuIdAttr(timestamp, getId());
            }

        } catch (SOAPException se) {
            log.log(Level.SEVERE, "WSS0386.error.creating.timestamp", se.getMessage());
            throw new XWSSecurityException("There was an error creating " +
            " Timestamp "  + se.getMessage());
        }

        setSOAPElement(timestamp);
        return timestamp;
    }

    /*
     * The <wsu:Created> element specifies a timestamp used to
     * indicate the creation time. It is defined as part of the
     * <wsu:Timestamp> definition.
     *
     * Time reference in WSS work should be in terms of
     * dateTime type specified in XML Schema in UTC time(Recommmended)
     */
    public void createDateTime() throws XWSSecurityException {
        if (created == null) {
            Calendar c = new GregorianCalendar();
            int offset = c.get(Calendar.ZONE_OFFSET);
            if (c.getTimeZone().inDaylightTime(c.getTime())) {
                offset += c.getTimeZone().getDSTSavings();
            }
            synchronized (calendarFormatter1) {
                calendarFormatter1.setTimeZone(c.getTimeZone());

                // always send UTC/GMT time
                long beforeTime = c.getTimeInMillis();
                long currentTime = 0;
                currentTime = beforeTime - offset;
                c.setTimeInMillis(currentTime);
                // finished UTC/GMT adjustment

                setCreated(calendarFormatter1.format(c.getTime()));

                c.setTimeInMillis(currentTime + timeout);
                setExpires(calendarFormatter1.format(c.getTime()));
            }
        }
    }

    public static final SimpleDateFormat calendarFormatter1
    = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static final SimpleDateFormat calendarFormatter2
    = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'.'sss'Z'");
}
