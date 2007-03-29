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
 * @version $Revision: 1.10 $
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
                                           @NotNull String activityId, @NotNull String registrantId) {
        registerFallback();
        if (serviceName.equals(ParticipantPortTypeImpl.serviceName)) {
            // ParticipantPortTypeImpl && CoordinatorPortTypeImpl have the same serviceName
            if (portName.equals(ParticipantPortTypeImpl.portName)) {
                ParticipantPortTypeImpl participant =
                        new ParticipantPortTypeImpl(activityId, registrantId);
                return ParticipantPortTypeImpl.getManager().
                        export(addressingVersion.eprType.eprClass, address.toString(), participant);
            } else if (portName.equals(CoordinatorPortTypeImpl.portName)) {
                CoordinatorPortTypeImpl coordinator =
                        new CoordinatorPortTypeImpl(activityId, registrantId);
                return CoordinatorPortTypeImpl.getManager().
                        export(addressingVersion.eprType.eprClass, address.toString(), coordinator);
            } else {
                throw new IllegalStateException( LocalizationMessages.WSTX_SERVICE_PORT_NOT_FOUND_5001(portName, serviceName));
            }
        } else if (serviceName.equals(RegistrationRequesterPortTypeImpl.serviceName)) {
            if (portName.equals(RegistrationRequesterPortTypeImpl.portName)) {
                RegistrationRequesterPortTypeImpl registrationRequester =
                        new RegistrationRequesterPortTypeImpl(activityId, registrantId);
                return RegistrationRequesterPortTypeImpl.getManager().
                        export(addressingVersion.eprType.eprClass, address.toString(), registrationRequester);
            } else if (portName.equals(RegistrationCoordinatorPortTypeImpl.portName)) {
                RegistrationCoordinatorPortTypeImpl registrationCoordinator =
                        new RegistrationCoordinatorPortTypeImpl(activityId);
                return RegistrationCoordinatorPortTypeImpl.getManager().
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
    private void registerFallback() {
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
                    wstxServiceAvailable = false;
                }
                
                CoordinatorPortTypeImpl coordinator =
                        new CoordinatorPortTypeImpl(UNKNOWN_ID, UNKNOWN_ID);
                if (CoordinatorPortTypeImpl.getManager() != null) {
                     CoordinatorPortTypeImpl.getManager().setFallbackInstance(coordinator);
                } else {
                    wstxServiceAvailable = false;
                }
               
                
                RegistrationRequesterPortTypeImpl registrationRequester =
                        new RegistrationRequesterPortTypeImpl(UNKNOWN_ID, UNKNOWN_ID);
                 if (RegistrationRequesterPortTypeImpl.getManager() != null) {
                    RegistrationRequesterPortTypeImpl.getManager().setFallbackInstance(registrationRequester);
                } else {
                    wstxServiceAvailable = false;
                }
               
                RegistrationCoordinatorPortTypeImpl registrationCoordinator =
                        new RegistrationCoordinatorPortTypeImpl(UNKNOWN_ID);
                if (RegistrationCoordinatorPortTypeImpl.getManager() != null) {
                    RegistrationCoordinatorPortTypeImpl.getManager().setFallbackInstance(registrationCoordinator);
                } else {
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
