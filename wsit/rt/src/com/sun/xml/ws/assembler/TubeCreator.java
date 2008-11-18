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
package com.sun.xml.ws.assembler;

import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.runtime.config.TubeFactoryConfig;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class that encapsulates logic of loading TubeFactory 
 * instances and creating Tube instances.
 *
 * @author m_potociar
 */
final class TubeCreator {

    private TubeFactory factory;

    TubeCreator(TubeFactory factory) { // TODO remove this constructor
        this.factory = factory;
    }

    TubeCreator(TubeFactoryConfig config) {
        try {
            Class<?> factoryClass = Class.forName(config.getClassName());
            if (TubeFactory.class.isAssignableFrom(factoryClass)) {
                @SuppressWarnings("unchecked") 
                // We can suppress "unchecked" warning here as we are checking for the correct type in the if statement above
                Class<TubeFactory> typedClass = (Class<TubeFactory>) factoryClass;
                this.factory = typedClass.newInstance();
            } else {
                // TODO L10N
                throw new RuntimeException(String.format("Class '%s' does not implement '%s' interface", factoryClass.getName(), TubeFactory.class.getName()));
            }
        } catch (InstantiationException ex) {
            // TODO L10N
            String message = String.format("Unable to instantiate tube factory class");
            Logger.getLogger(TubeCreator.class.getName()).log(Level.SEVERE, message, ex);
            throw new RuntimeException(message, ex);
        } catch (IllegalAccessException ex) {
            // TODO L10N
            String message = String.format("Unable to instantiate tube factory class");
            Logger.getLogger(TubeCreator.class.getName()).log(Level.SEVERE, message, ex);
            throw new RuntimeException(message, ex);
        } catch (ClassNotFoundException ex) {
            // TODO L10N
            String message = String.format("Unable to load tube factory class");
            Logger.getLogger(TubeCreator.class.getName()).log(Level.SEVERE, message, ex);
            throw new RuntimeException(message, ex);
        }
    }

    Tube createTube(ClientTubelineAssemblyContext context) {
        // TODO implement passing init parameters (if any) to the factory
        return factory.createTube(context);
    }

    Tube createTube(ServerTubelineAssemblyContext context) {
        // TODO implement passing init parameters (if any) to the factory
        return factory.createTube(context);
    }

    void updateContext(ClientTubelineAssemblyContext context) {
        if (factory instanceof TubelineAssemblyContextUpdater) {
            ((TubelineAssemblyContextUpdater) factory).prepareContext(context);
        }
    }

    void updateContext(ServerTubelineAssemblyContext context) {
        if (factory instanceof TubelineAssemblyContextUpdater) {
            ((TubelineAssemblyContextUpdater) factory).prepareContext(context);
        }
    }
}
