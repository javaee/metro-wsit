/*
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
*
* Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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
package com.sun.xml.ws.tx.coord.common;

import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.tx.at.WSATConstants;
import com.sun.xml.ws.tx.at.api.Transactional;
import com.sun.xml.ws.tx.coord.common.types.CoordinationContextIF;


public abstract class CoordinationContextBuilder {
    protected String coordinationType;
    protected String identifier;
    protected long expires;
    protected String address;
    protected String txId;
    protected boolean mustUnderstand;
    protected SOAPVersion soapVersion;

    protected Header coordinationHeader;
    Transactional.Version version;


    public static CoordinationContextBuilder newInstance(Transactional.Version version) {
        if(Transactional.Version.WSAT10 == version)
        return new com.sun.xml.ws.tx.coord.v10.CoordinationContextBuilderImpl();
        else if(Transactional.Version.WSAT11 == version || Transactional.Version.WSAT12 == version) {
          return new com.sun.xml.ws.tx.coord.v11.CoordinationContextBuilderImpl();
        }else {
            throw new IllegalArgumentException(version + "is not a supported ws-at version");
        }
    }


    public static CoordinationContextBuilder headers(HeaderList headers, Transactional.Version version) {
        CoordinationContextBuilder builder = null;
        for (int i = 0; i < headers.size(); i++) {
            Header header =  headers.get(i);
            if(header.getLocalPart().equals(WSATConstants.COORDINATION_CONTEXT)){
                if(WSATConstants.WSCOOR10_NS_URI.equals(header.getNamespaceURI())){
                    if (version == Transactional.Version.WSAT10 || version == Transactional.Version.DEFAULT) {
                        builder = new com.sun.xml.ws.tx.coord.v10.CoordinationContextBuilderImpl();
                        builder.version = Transactional.Version.WSAT10;
                    }
                }else if(WSATConstants.WSCOOR11_NS_URI.equals(header.getNamespaceURI())){
                    if (version != Transactional.Version.WSAT10) {
                        builder = new com.sun.xml.ws.tx.coord.v11.CoordinationContextBuilderImpl();
                        builder.version = Transactional.Version.WSAT11;
                    }
                }
                if(builder!=null) {
                  headers.understood(i);
                  return builder.header(header);
                }
            }
        }
        return null;
    }

    public Transactional.Version getVersion() {
        return version;
    }

    public CoordinationContextBuilder address(String address) {
        this.address = address;
        return this;
    }

    public CoordinationContextBuilder txId(String txId) {
        this.txId = txId;
        return this;
    }

    public CoordinationContextBuilder identifier(String identifier) {
        this.identifier = identifier;
        return this;
    }

    public CoordinationContextBuilder expires(long expires) {
        this.expires = expires;
        return this;
    }

    public CoordinationContextBuilder mustUnderstand(boolean mustUnderstand) {
        this.mustUnderstand = mustUnderstand;
        return this;
    }

    public CoordinationContextBuilder soapVersion(SOAPVersion soapVersion) {
        this.soapVersion = soapVersion;
        return this;
    }

  public CoordinationContextBuilder coordinationType(String coordinationType) {
        this.coordinationType = coordinationType;
        return this;
    }

    CoordinationContextBuilder header(Header coordinationHeader) {
      this.coordinationHeader = coordinationHeader;
      return this;
    }

    public CoordinationContextIF buildFromHeader(){
        return _fromHeader(coordinationHeader);
    }
    protected abstract CoordinationContextIF _fromHeader(Header header);

    public abstract CoordinationContextIF build();

    public abstract JAXBRIContext getJAXBRIContext();
}
