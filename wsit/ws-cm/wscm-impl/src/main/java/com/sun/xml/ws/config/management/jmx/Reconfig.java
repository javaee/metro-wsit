/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.config.management.jmx;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.config.management.jmx.ReconfigMBean;
import com.sun.xml.ws.config.management.ManagementMessages;

import java.io.Serializable;
import java.util.Map;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;


/**
 * Implements an MBean with support for JMX notifications.
 *
 * You can easily add new attributes and notifications to this implementation
 * through listeners.
 *
 * @author Fabian Ritzmann
 */
class Reconfig extends NotificationBroadcasterSupport implements ReconfigMBean, Serializable {

    private static final Logger LOGGER = Logger.getLogger(Reconfig.class);

    private final Map<String, MBeanAttribute> attributeToListener;
    private final Map<String, ReconfigNotification> notificationToListener;

    public Reconfig(Map<String, MBeanAttribute> attributeNameToListener,
            Map<String, ReconfigNotification> notificationToListener) {
        this.attributeToListener = attributeNameToListener;
        this.notificationToListener = notificationToListener;
    }

    public Object getAttribute(String attributeName)
            throws AttributeNotFoundException, MBeanException, ReflectionException {
        if (attributeName == null) {
            throw LOGGER.logSevereException(new RuntimeOperationsException(
                 new IllegalArgumentException(ManagementMessages.WSM_5073_ATTRIBUTE_NAME_NULL()),
                 ManagementMessages.WSM_5074_GET_ATTRIBUTE_NULL_NAME()));
        }
        final MBeanAttribute listener = attributeToListener.get(attributeName);
        if (listener != null) {
            if (!listener.isReadable()) {
                throw LOGGER.logSevereException(new AttributeNotFoundException(
                    ManagementMessages.WSM_5085_ATTRIBUTE_UNREADABLE(attributeName)));
            }
            return listener.getValue();
        }
        throw LOGGER.logSevereException(new AttributeNotFoundException(
                ManagementMessages.WSM_5075_CANNOT_FIND_ATTRIBUTE(attributeName)));
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException,
            InvalidAttributeValueException, MBeanException, ReflectionException {
        // Check attribute is not null to avoid NullPointerException later on
        if (attribute == null) {
            throw LOGGER.logSevereException(new RuntimeOperationsException(
                  new IllegalArgumentException(ManagementMessages.WSM_5076_ATTRIBUTE_NULL()),
                  ManagementMessages.WSM_5077_SETTER_ATTRIBUTE_NULL(ManagementMessages.RECONFIG_MBEAN_NAME())));
        }
        String name = attribute.getName(); // Note: Attribute's constructor
                                           // ensures it is not null
        Object value = attribute.getValue();

        // Check for a recognized attribute name and call the corresponding
        // setter
        //
        final MBeanAttribute listener = attributeToListener.get(name);
        if (listener != null) {
            if (!listener.isWritable()) {
                throw LOGGER.logSevereException(new AttributeNotFoundException(
                    ManagementMessages.WSM_5086_ATTRIBUTE_UNWRITABLE(name)));
            }
            if (value == null) {
                throw LOGGER.logSevereException(new InvalidAttributeValueException(
                        ManagementMessages.WSM_5078_ATTRIBUTE_VALUE_NULL(name)));
            }
            else {
                try {
                    listener.setValue(value);
                } catch (RuntimeException e) {
                    LOGGER.severe(ManagementMessages.WSM_5072_ATTRIBUTE_UPDATE_FAILED(name, value), e);
                    throw LOGGER.logSevereException(new RuntimeOperationsException(e,
                            ManagementMessages.WSM_5084_ATTRIBUTE_UPDATE_FAILED(name)));
                }
            }
        }
        // unrecognized attribute name:
        else {
            throw LOGGER.logSevereException(new AttributeNotFoundException(
                    ManagementMessages.WSM_5079_ATTRIBUTE_NOT_FOUND(name, ManagementMessages.RECONFIG_MBEAN_NAME())));
        }
    }

    public AttributeList getAttributes(String[] attributes) {
        if (attributes == null) {
            throw LOGGER.logSevereException(new RuntimeOperationsException(
                new IllegalArgumentException(ManagementMessages.WSM_5080_ATTRIBUTE_NAMES_ARRAY_NULL()),
                ManagementMessages.WSM_5081_GET_ATTRIBUTES_NULL_NAME()));
        }
        AttributeList resultList = new AttributeList();

        if (attributes.length == 0)
            return resultList;

        for (int i=0 ; i < attributes.length ; i++) {
            try {
                Object value = getAttribute(attributes[i]);
                resultList.add(new Attribute(attributes[i],value));
            } catch (AttributeNotFoundException e) {
                LOGGER.logSevereException(e);
            } catch (MBeanException e) {
                LOGGER.logSevereException(e);
            } catch (ReflectionException e) {
                LOGGER.logSevereException(e);
            }
        }
        return(resultList);
    }

    public AttributeList setAttributes(AttributeList attributes) {
        // Check attributes is not null to avoid NullPointerException later on
        if (attributes == null) {
            throw LOGGER.logSevereException(new RuntimeOperationsException(
                new IllegalArgumentException(ManagementMessages.WSM_5082_ATTRIBUTE_LIST_NULL()),
                ManagementMessages.WSM_5083_CANNOT_INVOKE_SETTER(ManagementMessages.RECONFIG_MBEAN_NAME())));
        }
        AttributeList resultList = new AttributeList();

        // if attributeNames is empty, nothing more to do
        if (attributes.isEmpty())
            return resultList;

        // for each attribute, try to set it and add to the result list if
        // successful
        for (Object attribute : attributes) {
            Attribute attr = (Attribute) attribute;
            try {
                setAttribute(attr);
                String name = attr.getName();
                Object value = getAttribute(name);
                resultList.add(new Attribute(name,value));
            } catch (Exception e) {
                LOGGER.logSevereException(e);
            }
        }
        return resultList;
    }

    public Object invoke(String actionName, Object[] params, String[] signature)
            throws MBeanException, ReflectionException {
        return null;
    }

    public MBeanInfo getMBeanInfo() {
        OpenMBeanAttributeInfoSupport[] attributes =
            new OpenMBeanAttributeInfoSupport[attributeToListener.size()];
        OpenMBeanConstructorInfoSupport[] constructors =
            new OpenMBeanConstructorInfoSupport[1];
        OpenMBeanOperationInfoSupport[] operations =
            new OpenMBeanOperationInfoSupport[0];
        MBeanNotificationInfo[] notifications =
            new MBeanNotificationInfo[notificationToListener.size()];

        int i = 0;
        for (String attributeName : attributeToListener.keySet()) {
            final MBeanAttribute listener = attributeToListener.get(attributeName);
            attributes[i] = new OpenMBeanAttributeInfoSupport(attributeName,
                listener.getDescription(),
                listener.getType(),
                listener.isReadable(),
                listener.isWritable(),
                false);
            i++;
        }

        i = 0;
        for (String notificationName : notificationToListener.keySet()) {
            ReconfigNotification listener = notificationToListener.get(notificationName);
            notifications[i] = new MBeanNotificationInfo(listener.getNotificationTypes(),
                    listener.getName(),
                    listener.getDescription());
        }

        constructors[0] = new OpenMBeanConstructorInfoSupport(
                ManagementMessages.RECONFIG_MBEAN_NAME(),
                ManagementMessages.RECONFIG_MBEAN_CONSTRUCTOR_DESCRIPTION(ManagementMessages.RECONFIG_MBEAN_NAME()),
                new OpenMBeanParameterInfoSupport[0]);

        return new OpenMBeanInfoSupport(this.getClass().getName(),
                                        ManagementMessages.RECONFIG_MBEAN_DESCRIPTION(),
                                        attributes,
                                        constructors,
                                        operations,
                                        notifications);
    }

}
