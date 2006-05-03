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

/*
 * AttachmentData.java
 *
 * Created on April 7, 2005, 11:28 AM
 */

package com.sun.xml.wss.impl.dsig;

import javax.xml.crypto.Data;
import javax.xml.soap.AttachmentPart;

/**
 * <B>Wrapper class to be used with XWSS attachment transform 
 * provider implementation.</B>
 * @author K.Venugopal@sun.com
 */
public class AttachmentData implements Data {
    private AttachmentPart attachment = null;
    /** Creates a new instance of AttachmentData */
    public AttachmentData() {
    }
    
    /**
     *
     * @param attachment
     */    
    public void setAttachmentPart(AttachmentPart attachment){
        this.attachment = attachment;
    }
    
    /**
     *
     * @return
     */    
    public AttachmentPart getAttachmentPart(){
        return attachment;
    }
}