/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.config.management.jmx;

import com.sun.istack.logging.Logger;
import com.sun.xml.ws.api.config.management.ReconfigNotifier;
import com.sun.xml.ws.config.management.ManagementMessages;

import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

/**
 *
 * @author Fabian Ritzmann
 */
public class ReconfigNotification implements ReconfigNotifier {

    public static final String NOTIFICATION_TYPE = "sun.metro.config.reconfig.success";
    
    private static final Logger LOGGER = Logger.getLogger(ReconfigNotification.class);

    private final NotificationBroadcasterSupport support;
    private final ObjectName source;

    private volatile long sequenceNumber = 0L;

    /**
     * Initializes the instance.
     *
     * @param support We use the sendNotification method to send notifications.
     * @param source The name of the MBean sending the notification.
     */
    public ReconfigNotification(NotificationBroadcasterSupport support, ObjectName source) {
        this.support = support;
        this.source = source;
    }

    public String[] getNotificationTypes() {
        final String[] types = { NOTIFICATION_TYPE };
        return types;
    }

    public String getName() {
        return ReconfigNotification.class.getName();
    }

    public String getDescription() {
        return ManagementMessages.RECONFIG_NOTIFICATION_DESCRIPTION();
    }

    public void sendNotification() {
        // TODO put messages into properties, define constants
        Notification notification = new Notification(NOTIFICATION_TYPE,
                source,
                this.sequenceNumber++,
                System.currentTimeMillis(),
                "Reconfigured the endpoint successfully");
        this.support.sendNotification(notification);
    }

}