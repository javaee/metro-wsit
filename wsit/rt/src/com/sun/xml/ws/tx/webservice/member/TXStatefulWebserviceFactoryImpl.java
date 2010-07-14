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
package com.sun.xml.ws.tx.webservice.member;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.developer.StatefulWebServiceManager;
import com.sun.xml.ws.tx.common.AddressManager;
import static com.sun.xml.ws.tx.common.Constants.UNKNOWN_ID;
import com.sun.xml.ws.tx.common.StatefulWebserviceFactory;
import com.sun.xml.ws.tx.common.TxLogger;
import com.sun.xml.ws.tx.webservice.member.at.CoordinatorPortType;
import com.sun.xml.ws.tx.webservice.member.at.CoordinatorPortTypeImpl;
import com.sun.xml.ws.tx.webservice.member.at.ParticipantPortType;
import com.sun.xml.ws.tx.webservice.member.at.ParticipantPortTypeImpl;
import com.sun.xml.ws.tx.webservice.member.coord.RegistrationCoordinatorPortType;
import com.sun.xml.ws.tx.webservice.member.coord.RegistrationCoordinatorPortTypeImpl;
import com.sun.xml.ws.tx.webservice.member.coord.RegistrationRequesterPortType;
import com.sun.xml.ws.tx.webservice.member.coord.RegistrationRequesterPortTypeImpl;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.ws.EndpointReference;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;

/**
 * This class ...
 *
 * @author Ryan.Shoemaker@Sun.COM
 * @version $Revision: 1.15.6.2 $
 * @since 1.0
 */
// suppress known deprecation warnings about using short term workaround StatefulWebService.export(Class, String webServiceEndpoint, PortType)
@SuppressWarnings("deprecation")
final public class TXStatefulWebserviceFactoryImpl implements StatefulWebserviceFactory {

    private static final TxLogger logger = TxLogger.getLogger(TXStatefulWebserviceFactoryImpl.class);

    @NotNull
    public StatefulWebServiceManager getManager(@NotNull String serviceName, @NotNull String portName) {
        registerFallback();
        if (serviceName.equals(ParticipantPortTypeImpl.serviceName)) {
            // ParticipantPortTypeImpl && CoordinatorPortTypeImpl have the same serviceName
            if (portName.equals(ParticipantPortTypeImpl.portName)) {
                return ParticipantPortTypeImpl.getManager();
            } else if (portName.equals(CoordinatorPortTypeImpl.portName)) {
                return CoordinatorPortTypeImpl.getManager();
            } else {
                throw new IllegalStateException(
                        LocalizationMessages.WSTX_SERVICE_PORT_NOT_FOUND_5001(portName, serviceName));
            }
        } else if (serviceName.equals(RegistrationRequesterPortTypeImpl.serviceName)) {
            if (portName.equals(RegistrationRequesterPortTypeImpl.portName)) {
                return RegistrationRequesterPortTypeImpl.getManager();
            } else if (portName.equals(RegistrationCoordinatorPortTypeImpl.portName)) {
                return RegistrationCoordinatorPortTypeImpl.getManager();
            } else {
                throw new IllegalStateException(LocalizationMessages.WSTX_SERVICE_PORT_NOT_FOUND_5001(portName, serviceName));
            }
        } else {
            throw new IllegalStateException(LocalizationMessages.WSTX_SERVICE_PORT_NOT_FOUND_5001(portName, serviceName));
        }
    }

    @NotNull
    public EndpointReference createService(@NotNull String serviceName, @NotNull String portName,
                                           @NotNull URI address, @NotNull AddressingVersion addressingVersion,
                                           @NotNull String activityId, @NotNull String registrantId,
                                           long timeoutInMillis) {

        // scale the stateful ws timeout up by 25%
        timeoutInMillis *= 1.25;

        registerFallback();
        if (serviceName.equals(ParticipantPortTypeImpl.serviceName)) {
            // ParticipantPortTypeImpl && CoordinatorPortTypeImpl have the same serviceName
            if (portName.equals(ParticipantPortTypeImpl.portName)) {
                ParticipantPortTypeImpl participant =
                        new ParticipantPortTypeImpl(activityId, registrantId);
                final StatefulWebServiceManager<ParticipantPortTypeImpl> statefulWebServiceManager = ParticipantPortTypeImpl.getManager();
                statefulWebServiceManager.setTimeout(timeoutInMillis, null);
                return statefulWebServiceManager.
                        export(addressingVersion.eprType.eprClass, address.toString(), participant);
            } else if (portName.equals(CoordinatorPortTypeImpl.portName)) {
                CoordinatorPortTypeImpl coordinator =
                        new CoordinatorPortTypeImpl(activityId, registrantId);
                final StatefulWebServiceManager<CoordinatorPortTypeImpl> statefulWebServiceManager = CoordinatorPortTypeImpl.getManager();
                statefulWebServiceManager.setTimeout(timeoutInMillis, null);
                return statefulWebServiceManager.
                        export(addressingVersion.eprType.eprClass, address.toString(), coordinator);
            } else {
                throw new IllegalStateException( LocalizationMessages.WSTX_SERVICE_PORT_NOT_FOUND_5001(portName, serviceName));
            }
        } else if (serviceName.equals(RegistrationRequesterPortTypeImpl.serviceName)) {
            if (portName.equals(RegistrationRequesterPortTypeImpl.portName)) {
                RegistrationRequesterPortTypeImpl registrationRequester =
                        new RegistrationRequesterPortTypeImpl(activityId, registrantId);
                final StatefulWebServiceManager<RegistrationRequesterPortTypeImpl> statefulWebServiceManager = RegistrationRequesterPortTypeImpl.getManager();
                statefulWebServiceManager.setTimeout(timeoutInMillis, null);
                return statefulWebServiceManager.
                        export(addressingVersion.eprType.eprClass, address.toString(), registrationRequester);
            } else if (portName.equals(RegistrationCoordinatorPortTypeImpl.portName)) {
                RegistrationCoordinatorPortTypeImpl registrationCoordinator =
                        new RegistrationCoordinatorPortTypeImpl(activityId);
                final StatefulWebServiceManager<RegistrationCoordinatorPortTypeImpl> statefulWebServiceManager = RegistrationCoordinatorPortTypeImpl.getManager();
                statefulWebServiceManager.setTimeout(timeoutInMillis, null);
                return statefulWebServiceManager.
                        export(addressingVersion.eprType.eprClass, address.toString(), registrationCoordinator);
            } else {
                throw new IllegalStateException( LocalizationMessages.WSTX_SERVICE_PORT_NOT_FOUND_5001(portName, serviceName));
            }
        } else {
            throw new IllegalStateException( LocalizationMessages.WSTX_SERVICE_PORT_NOT_FOUND_5001(portName, serviceName));
        }
    }

    static private boolean registeredFallback = false;
    
    static private boolean wstxServiceAvailable = false;
   
    /**
     * Returns true iff all endpoints for wstx_service are available.
     *
     * Identifies when no coordinator specified for application client.
     * Also, identifies when there is a configuration issue for wstx_service.
     */
    public boolean isWSTXServiceAvailable() {
        registerFallback();
        return wstxServiceAvailable;
    }
    
    /**
     * Instances that handle request for unknown StatefulWebService instance.
     * This can happen when a request for a StatefulWebService is received after the instance has
     * timed out or was explicitly unexported.
     */
    synchronized private void registerFallback() {
        if (!registeredFallback ) {
            registeredFallback = true;

            // force the lazy deployment of our ws so we can access the stateful manager field
            pingStatefulServices();

            if (isWSTXServiceAvailable()) {
                ParticipantPortTypeImpl participant =
                        new ParticipantPortTypeImpl(UNKNOWN_ID, UNKNOWN_ID);
                if (ParticipantPortTypeImpl.getManager() != null) {
                    ParticipantPortTypeImpl.getManager().setFallbackInstance(participant);
                } else {
                    if (logger.isLogging(Level.FINER)) {
                        logger.finer(
                                "registerFallback",
                                String.format("No stateful webservice manager returned from %s instance", ParticipantPortTypeImpl.class.getName()));
                    }
                    wstxServiceAvailable = false;
                }
                
                CoordinatorPortTypeImpl coordinator =
                        new CoordinatorPortTypeImpl(UNKNOWN_ID, UNKNOWN_ID);
                if (CoordinatorPortTypeImpl.getManager() != null) {
                     CoordinatorPortTypeImpl.getManager().setFallbackInstance(coordinator);
                } else {
                    if (logger.isLogging(Level.FINER)) {
                        logger.finer(
                                "registerFallback",
                                String.format("No stateful webservice manager returned from %s instance", CoordinatorPortTypeImpl.class.getName()));
                    }
                    wstxServiceAvailable = false;
                }
               
                
                RegistrationRequesterPortTypeImpl registrationRequester =
                        new RegistrationRequesterPortTypeImpl(UNKNOWN_ID, UNKNOWN_ID);
                 if (RegistrationRequesterPortTypeImpl.getManager() != null) {
                    RegistrationRequesterPortTypeImpl.getManager().setFallbackInstance(registrationRequester);
                } else {
                    if (logger.isLogging(Level.FINER)) {
                        logger.finer(
                                "registerFallback",
                                String.format("No stateful webservice manager returned from %s instance", RegistrationRequesterPortTypeImpl.class.getName()));
                    }
                    wstxServiceAvailable = false;
                }
               
                RegistrationCoordinatorPortTypeImpl registrationCoordinator =
                        new RegistrationCoordinatorPortTypeImpl(UNKNOWN_ID);
                if (RegistrationCoordinatorPortTypeImpl.getManager() != null) {
                    RegistrationCoordinatorPortTypeImpl.getManager().setFallbackInstance(registrationCoordinator);
                } else {
                    if (logger.isLogging(Level.FINER)) {
                        logger.finer(
                                "registerFallback",
                                String.format("No stateful webservice manager returned from %s instance", RegistrationCoordinatorPortTypeImpl.class.getName()));
                    }
                    wstxServiceAvailable = false;
                }
            } else {
                registeredFallback = false;
            }
        }
    }

    private boolean pingServices = true;

    /**
     * A workaround for WSIT issue 309
     * <p/>
     * The GF performance team does not want our wstx services to load during app server
     * startup, so they are marked to lazy deploy upon the first invocation.  Unfortunately
     * we need to access the stateful webservice manager field in some of these services
     * before the first invocation, so we will ping each of them to force the load to happen.
     * <p/>
     * This will only happen once and it will only happen after we are certain that an app
     * needs these services running.
     */
    private void pingStatefulServices() {
        if (pingServices) {
            pingServices = false;
            wstxServiceAvailable = true; // if any pingService fails, this reverts to false.

            if (logger.isLogging(Level.FINEST)) {
                logger.finest("pingStatefulServices", "pinging register service...");
            }
            pingService((AddressManager.getAddress(RegistrationCoordinatorPortType.class, false).toString() + "?wsdl"),
                    RegistrationCoordinatorPortTypeImpl.class);
            if (!wstxServiceAvailable) {
                return; // short-circuit if the first ping fails
            }
            if (logger.isLogging(Level.FINEST)) {
                logger.finest("pingStatefulServices", "pinging registerResponse service...");
            }
            pingService((AddressManager.getAddress(RegistrationRequesterPortType.class, false).toString() + "?wsdl"),
                    RegistrationRequesterPortTypeImpl.class);
            if (logger.isLogging(Level.FINEST)) {
                logger.finest("pingStatefulServices", "pinging ATCoordinator service...");
            }
            pingService((AddressManager.getAddress(CoordinatorPortType.class, false).toString() + "?wsdl"),
                    CoordinatorPortTypeImpl.class);
            if (logger.isLogging(Level.FINEST)) {
                logger.finest("pingStatefulServices", "pinging ATParticipant service...");
            }
            pingService((AddressManager.getAddress(ParticipantPortType.class, false).toString() + "?wsdl"),
                    ParticipantPortTypeImpl.class);
        }
    }

    /*
     * This method accesses a url specifically to force the app server to completely
     * load our stateful webservices.  We're pinging the services by accessing their
     * wsdl files.
     *
     * PostCondition: if pingService fails, sets wstxServiceAvailable to false.
     */
    private void pingService(String urlAddr, Class sws) {
        final String METHOD = "pingService";
        HttpURLConnection conn = null;
        InputStream response = null;
        BufferedReader reader = null;
        try {
            URL url = new URL(urlAddr);
            conn = (HttpURLConnection) url.openConnection();
            if (conn instanceof HttpsURLConnection) {
                ((HttpsURLConnection) conn).setHostnameVerifier(
                        new HostnameVerifier() {
                            public boolean verify(String string, SSLSession sSLSession) {
                                return true;
                            }
                        });
            }
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded"); // taken from wsimport
            conn.connect();
            response = conn.getInputStream();
            reader = new BufferedReader(
                    new InputStreamReader(response));
            String line = reader.readLine();
            while (line != null) {
//              logger.finest(METHOD, line);
                line = reader.readLine();
            }
            if (logger.isLogging(Level.FINEST)) {
                logger.finest(METHOD, "RESPONSE CODE: " + conn.getResponseCode());
            }
            if (sws.getDeclaredField("manager").equals(null)) {
                logger.severe(METHOD, 
                              LocalizationMessages.ENDPOINT_NOT_AVAILABLE_5002(urlAddr, sws.getName()));
            } else {
                logger.finest(METHOD, "Injection succeeded");
            }
        } catch (Exception e) {
            if (wstxServiceAvailable) {
                wstxServiceAvailable = false;
                
                // only print stacktrace for first endpoint ping failure.
                logger.warning(METHOD,
                        LocalizationMessages.ENDPOINT_NOT_AVAILABLE_5002(urlAddr, sws.getName()));
                logger.fine(METHOD,
                        LocalizationMessages.ENDPOINT_NOT_AVAILABLE_5002(urlAddr, sws.getName()), e);
            } else {
                logger.severe(METHOD, 
                        LocalizationMessages.ENDPOINT_NOT_AVAILABLE_5002(urlAddr, sws.getName()));
            }
        } finally {
            try {
                if (conn != null) conn.disconnect();
                if (reader != null) reader.close();
            } catch (Exception e) {
                // ignore
            }
        }
    }
}
