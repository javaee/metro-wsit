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
package com.sun.xml.ws.rx.testing;

import com.sun.xml.ws.api.FeatureConstructor;
import com.sun.istack.logging.Logger;
import com.sun.xml.ws.rx.rm.runtime.RuntimeContext;
import org.glassfish.gmbal.ManagedAttribute;
import org.glassfish.gmbal.ManagedData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.xml.ws.WebServiceFeature;

/**
 *
 * @author Marek Potociar (marek.potociar at sun.com)
 */
@ManagedData
public final class PacketFilteringFeature extends WebServiceFeature {

    public static final String ID = "com.sun.xml.ws.rm.runtime.testing.PacketFilteringFeature";
    //
    private static final Logger LOGGER = Logger.getLogger(PacketFilteringFeature.class);
    //
    private final List<Class<? extends PacketFilter>> filterClasses;

    public PacketFilteringFeature() {
        // this constructor is here just to satisfy JAX-WS specification requirements
        this.filterClasses = Collections.emptyList();
        this.enabled = true;
    }

    public PacketFilteringFeature(boolean enabled) {
        // this constructor is here just to satisfy JAX-WS specification requirements
        this.filterClasses = Collections.emptyList();
        this.enabled = enabled;
    }

    public PacketFilteringFeature(Class<? extends PacketFilter>... filterClasses) {
        this(true, filterClasses);
    }

    @FeatureConstructor({"enabled", "filters"})
    public PacketFilteringFeature(boolean enabled, Class<? extends PacketFilter>... filterClasses) {
        this.enabled = enabled;
        if (filterClasses != null && filterClasses.length > 0) {
            this.filterClasses = Collections.unmodifiableList(Arrays.asList(filterClasses));
        } else {
            this.filterClasses = Collections.emptyList();
        }
    }

    @Override
    @ManagedAttribute
    public String getID() {
        return ID;
    }

    List<PacketFilter> createFilters(RuntimeContext context) {
        List<PacketFilter> filters = new ArrayList<PacketFilter>(filterClasses.size());
        
        for (Class<? extends PacketFilter> filterClass : filterClasses) {
            try {
                final PacketFilter filter = filterClass.newInstance();
                filter.configure(context);
                filters.add(filter);
            } catch (InstantiationException ex) {
                LOGGER.warning("Error instantiating packet filter of class [" + filterClass.getName() + "]", ex);
            } catch (IllegalAccessException ex) {
                LOGGER.warning("Error instantiating packet filter of class [" + filterClass.getName() + "]", ex);
            }
        }

        return filters;
    }

    @ManagedAttribute
    boolean hasFilters() {
        return !filterClasses.isEmpty();
    }
}
