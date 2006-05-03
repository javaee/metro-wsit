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
 * DataWrapper.java
 *
 * Created on May 2, 2005, 9:43 AM
 */

package com.sun.xml.wss.impl.dsig;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.policy.mls.SignatureTarget;
import javax.xml.crypto.Data;
import javax.xml.crypto.NodeSetData;
import javax.xml.crypto.OctetStreamData;

/**
 * Wrapper class for JSR 105 Data objects.Caches SignatureTarget
 * object and data resolved using this signature target.Reduces
 * the burden of instanceof checks.
 * @author K.Venugopal@sun.com
 */
public class DataWrapper{
    
    private Data data = null;
    private int type = -1;
    private SignatureTarget signatureTarget = null;
    
    /**
     *
     * @param data
     */    
    DataWrapper(Data data){
        this.data = data;
        if(data instanceof AttachmentData){
            type = MessageConstants.ATTACHMENT_DATA;
        }else if (data instanceof NodeSetData){
            type = MessageConstants.NODE_SET_DATA;
        }else if(data instanceof OctetStreamData){
            type = MessageConstants.OCTECT_STREAM_DATA;
        }
        
    }
    
    /**
     *
     * @return Data object.
     */    
    public Data getData(){
        return this.data;
    }
    
    /**
     *
     * @return type of data object wrapped.
     */    
    public int getType(){
        return type;
    }
    
    /**
     *
     * @return if Data is AttachmentData
     */    
    public boolean isAttachmentData(){
        if(type ==MessageConstants.ATTACHMENT_DATA ){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     *
     * @return true if Data is NodeSetData.
     */    
    public boolean isNodesetData(){
        if(type == MessageConstants.NODE_SET_DATA ){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     *
     * @return true if Data is OctetStreamData.
     */    
    public boolean isOctectData(){
        if(type == MessageConstants.OCTECT_STREAM_DATA ){
            return true;
        }else{
            return false;
        }
    }
    
    /**
     * null if no target has been set.
     * @return
     */    
    public SignatureTarget getTarget(){
        return signatureTarget;
    }
    
    /**
     *
     * @param target
     */    
    public void setTarget(SignatureTarget target){
        this.signatureTarget = target;
    }
}
