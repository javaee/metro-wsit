/*
 * $Id: MimeHeaderCanonicalizer.java,v 1.3 2010-03-20 12:32:24 kumarjayanti Exp $
 */

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

package com.sun.xml.wss.impl.c14n;

import java.util.Vector;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.soap.MimeHeader;

import com.sun.xml.wss.swa.MimeConstants;
import com.sun.xml.wss.XWSSecurityException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.MimeUtility;

public class MimeHeaderCanonicalizer extends Canonicalizer {
    
    public MimeHeaderCanonicalizer() {}
    
    public byte[] canonicalize(byte[] input) throws XWSSecurityException {
        return null;
    }
    
    public InputStream canonicalize(InputStream input,OutputStream outputStream)
    throws javax.xml.crypto.dsig.TransformException   {
        throw new UnsupportedOperationException();
    }
    @SuppressWarnings("unchecked")
    public byte[] _canonicalize(Iterator mimeHeaders /* internal class, therefore ok */)
    throws XWSSecurityException {
        String _mh = "";
        List mimeHeaderList = new ArrayList();
        while(mimeHeaders.hasNext()){
            mimeHeaderList.add(mimeHeaders.next());
        }
        
        // rf. steps 2-10 Section 4.3.1 SwA Draft 13
        // SAAJ RI returns trimmed (at start) Content-Description values
        // Unstructured MIME Headers can be RFC2047 encoded, step 6
        // Unfold, step 5; Uncomment, step 8; Steps 9-16 not applicable
        String cDescription = getMatchingHeader(mimeHeaderList, MimeConstants.CONTENT_DESCRIPTION);
        if (cDescription != null) {
            _mh += MimeConstants.CONTENT_DESCRIPTION + _CL;
            _mh += uncomment(rfc2047decode(unfold(cDescription)));
            _mh += _CRLF;
        }
        
        // Unfold, step 5; unfold WSP, step 7;
        String cDisposition = getMatchingHeader(mimeHeaderList, MimeConstants.CONTENT_DISPOSITION);
        if (cDisposition != null) {
            _mh += MimeConstants.CONTENT_DISPOSITION + _CL;
            _mh += canonicalizeHeaderLine(uncomment(unfold(cDisposition)), true);
            _mh += _CRLF;
        }
        
        // Unfold, step 5; unfold WSP, step 7;
        String cId = getMatchingHeader(mimeHeaderList, MimeConstants.CONTENT_ID);
        if (cId != null) {
            _mh += MimeConstants.CONTENT_ID + _CL;
            _mh += unfoldWS(uncomment(unfold(cId))).trim();
            _mh += _CRLF;
        }
        
        // Unfold, step 5; unfold WSP, step 7;
        String cLocation = getMatchingHeader(mimeHeaderList, MimeConstants.CONTENT_LOCATION);
        if (cLocation != null) {
            _mh += MimeConstants.CONTENT_LOCATION + _CL;
            _mh += unfoldWS(uncomment(unfold(cLocation))).trim();
            _mh += _CRLF;
        }
        
        // Unfold, step 5; unfold WSP, step 7;
        String cType = getMatchingHeader(mimeHeaderList, MimeConstants.CONTENT_TYPE);
        cType = (cType == null) ? "text/plain; charset=us-ascii" : cType;
        
        _mh += MimeConstants.CONTENT_TYPE + _CL;
        _mh += canonicalizeHeaderLine(uncomment(unfold(cType)), true);
        _mh += _CRLF;
        
        _mh += _CRLF; // step 17
        
        byte[] b = null;
        try {
            b = _mh.getBytes("UTF-8");
        } catch (Exception e) {
            // log
            throw new XWSSecurityException(e);
        }
        
        return b;
    }
    
    private String getMatchingHeader(List mimeHeaders, String key) throws XWSSecurityException {
        String header_line = null;
        try {
            for (int i=0; i<mimeHeaders.size(); i++) {
                MimeHeader mhr = (MimeHeader)mimeHeaders.get(i);
         
                if (mhr.getName().equalsIgnoreCase(key)) {
                    header_line = mhr.getValue();
                    break;
                }
            }
            /*while(mimeHeaders.hasNext()){
                MimeHeader mhr = (MimeHeader)mimeHeaders.next();
                if (mhr.getName().equalsIgnoreCase(key)) {
                    header_line = mhr.getValue();
                    break;
                }
            }*/
        } catch (Exception npe) {
            // log
            throw new XWSSecurityException("Failed to locate MIME Header, " + key);
        }
        return header_line;
    }
    
    private String unfold(String input) {
        if (input.charAt(0) == _QT.charAt(0) || input.charAt(input.length()-1) == _QT.charAt(0)) return input;
        
        // remove all CRLF sequences
        StringBuffer sb = new StringBuffer();
        
        for (int i=0; i<input.length(); i++) {
            char c = input.charAt(i);
            if (c == _CRLF.charAt(0) && i != input.length()-1)
                if (input.charAt(i+1) == _CRLF.charAt(1)) {
                    i++;
                    continue;
                }
            sb.append(c);
        }
        
        return sb.toString();
    }
    
    private String rfc2047decode(String input) throws XWSSecurityException {
        if (input.charAt(0) == _QT.charAt(0) || input.charAt(input.length()-1) == _QT.charAt(0)) return input;
        
        String decodedText = null;
        try {
            decodedText = MimeUtility.decodeText(input);
        } catch (Exception e) {
            // log
            throw new XWSSecurityException(e);
        }
        return decodedText;
    }
    
    private String unfoldWS(String input) {
        if (input.charAt(0) == _QT.charAt(0) || input.charAt(input.length()-1) == _QT.charAt(0)) return input;
        
        StringBuffer sb = new StringBuffer();
        for (int i=0; i<input.length(); i++) {
            if (input.charAt(i) == _WS.charAt(0) || input.charAt(i) == _HT.charAt(0)) {
                sb.append(_WS.charAt(0));
                for (i++; i != input.length()-1; i++)
                    if (input.charAt(i) != _WS.charAt(0) && input.charAt(i) != _HT.charAt(0)) {
                        sb.append(input.charAt(i));
                        break;
                    }
            } else
                sb.append(input.charAt(i));
        }
        
        return sb.toString();
    }
    
    private String uncomment(String input) {
        if (input.charAt(0) == _QT.charAt(0) || input.charAt(input.length()-1) == _QT.charAt(0)) return input;
        
        for (int oc=0; oc<input.length(); oc++) {
            if ((oc=input.indexOf(_OC)) == -1)
                // no opening brace found
                break;
            
            int offset = input.substring(oc).indexOf(_CC);
            if (offset == -1)
                // no closing brace found
                break;
            
            int cc = oc + offset;
            
            String fs = (oc != 0) ? input.substring(0, oc) : "";
            String bs = input.substring(cc+1);
            
            if (offset == 1) {
                // encountered "..()"
                input = fs + _WS + bs;
                
            } else {
                if (input.substring(oc+1, cc).indexOf(_OC) == -1) {
                    // encountered "..(..).."
                    input = fs + _WS + bs;
                } else {
                    // encountered nested comment, bombs if comments are malformed
                    input = fs + _OC + uncomment(input.substring(oc+1, cc+1)) + bs;
                    
                }
            }
        }
        
        return input;
    }
    
    private String canonicalizeHeaderLine(String input, boolean applyStep10SwaDraft13) throws XWSSecurityException {
        int _sc = input.indexOf(_SC);
        if (_sc <=0 || _sc == input.length()-1) return input;
        
        // step 9 MHC SwA Draft 13
        String _fs = input.substring(0, _sc).toLowerCase();
        if (applyStep10SwaDraft13) _fs = quote(_fs, false);
        
        // params found
        String size = null;
        String type = null;
        String charset = null;
        String padding = null;
        String filename = null;
        String read_date = null;
        String creation_date = null;
        String modification_date = null;
        
        String decoded = null;
        
        try {
            decoded = rfc2184decode(input.substring(_sc+1));
        } catch (Exception e) {
            // log
            throw new XWSSecurityException(e);
        }
        
        StringTokenizer strnzr = new StringTokenizer(decoded, _SC);
        while (strnzr.hasMoreElements()) {
            String param = strnzr.nextToken();
            
            String pname = param.substring(0, param.indexOf(_EQ));
            String value = param.substring(param.indexOf(_EQ)+1);
            
            if (pname.equalsIgnoreCase(MimeConstants.TYPE)) {
                type = quote(value.toLowerCase(), true);
            } else
                if (pname.equalsIgnoreCase(MimeConstants.PADDING)) {
                    padding = quote(value.toLowerCase(), true);
                } else
                    if (pname.equalsIgnoreCase(MimeConstants.CHARSET)) {
                        charset = quote(value.toLowerCase(), true);
                    } else
                        if (pname.equalsIgnoreCase(MimeConstants.FILENAME)) {
                            filename = quote(value.toLowerCase(), true);
                        } else
                            if (pname.equalsIgnoreCase(MimeConstants.CREATION_DATE)) {
                                creation_date = quote(value.toLowerCase(), true);
                            } else
                                if (pname.equalsIgnoreCase(MimeConstants.MODIFICATION_DATE)) {
                                    modification_date = quote(value.toLowerCase(), true);
                                } else
                                    if (pname.equalsIgnoreCase(MimeConstants.READ_DATE)) {
                                        read_date = quote(value.toLowerCase(), true);
                                    } else
                                        if (pname.equalsIgnoreCase(MimeConstants.SIZE)) {
                                            size = quote(value.toLowerCase(), true);
                                        }
        }
        
        // no sanity checks
        if (charset != null)
            _fs += _SC + MimeConstants.CHARSET + _EQ + charset;
        
        if (creation_date != null)
            _fs += _SC + MimeConstants.CREATION_DATE + _EQ + creation_date;
        
        if (filename != null)
            _fs += _SC + MimeConstants.FILENAME + _EQ + filename;
        
        if (modification_date != null)
            _fs += _SC + MimeConstants.MODIFICATION_DATE + _EQ + modification_date;
        
        if (padding != null)
            _fs += _SC + MimeConstants.PADDING + _EQ + padding;
        
        if (read_date != null)
            _fs += _SC + MimeConstants.READ_DATE + _EQ + read_date;
        
        if (size != null)
            _fs += _SC + MimeConstants.SIZE + _EQ + size;
        
        if (type != null)
            _fs += _SC + MimeConstants.TYPE + _EQ + type;
        
        return _fs;
    }
    
    private Vector makeParameterVector(String input) {
        Vector<Object> v = new Vector<Object>();
        
        StringTokenizer nzr = new StringTokenizer(input, _SC);
        while (nzr.hasMoreTokens()) {
            v.add(nzr.nextToken());
        }
        
        return v;
    }
    
    private String _rfc2184decode(String input) throws Exception {
        StringTokenizer nzr = new StringTokenizer(input, _SQ);
        
        if (nzr.countTokens() != 3) {
            // log
            throw new XWSSecurityException("Malformed RFC2184 encoded parameter");
        }
        
        String charset  = nzr.nextToken();
        String language = nzr.nextToken();
        //String encoded  = nzr.nextToken();
        
        for (int i=0; i<input.length(); i++) {
            if (input.charAt(i) == _PC.charAt(0)) {
                input = input.substring(0, i) +
                _decodeHexadecimal(input.substring(i+1, i+3), charset, language) +
                input.substring(i+3);
            }
        }
        
        return input;
    }
    
    private String _decodeHexadecimal(String input, String charset, String language) throws Exception {
        // ignoring language
        byte b = Byte.decode("0x"+input.toUpperCase()).byteValue();
        return new String(new byte[] {b}, MimeUtility.javaCharset(charset));
    }
    @SuppressWarnings("unchecked")
    private String rfc2184decode(String input) throws Exception {
        int index = -1;
        String pname = "";
        String value = "";
        String decoded = "";
        
        Vector v = makeParameterVector(input);
        for (int i=0; i<v.size(); i++) {
            String token = (String) v.elementAt(i);
            
            int idx = token.indexOf(_EQ);
            String pn = token.substring(0, idx).trim();
            String pv = token.substring(idx+1).trim();
            
            if (pn.endsWith(_AX)) {
                // is language encoded, strip _AX
                pn = pn.substring(0, pn.length()-1);
                pv = _rfc2184decode(pv);
                
                token = pn + _EQ + pv;
                
                v.setElementAt(token, i);
                
                i--;
                
            } else {
                int ix = pn.indexOf(_AX);
                
                if (ix == -1) {
                    // flush out the previous param
                    if (!pname.equals(""))
                        decoded += _SC + pname + _EQ + pv;
                    
                    // write the current param
                    decoded += _SC + pn + _EQ + pv;
                    
                    // reset state
                    pname = "";
                    value = "";
                    index = -1;
                    continue;
                }
                
                // parameter continuation
                String pn_i = pn.substring(0, ix).trim();
                int curr = new Integer(pn.substring(ix+1).trim()).intValue();
                
                if (pn_i.equalsIgnoreCase(pname)) {
                    if (curr != index+1) {
                        // log
                        throw new XWSSecurityException("Malformed RFC2184 encoded parameter");
                    }
                    
                    value += concatenate2184decoded(value, pv);
                    index++;
                    
                } else {
                    if (curr == 0) {
                        // flush out previous param
                        if (!pname.equals(""))
                            decoded += _SC + pname + _EQ + value;
                        
                        // store state
                        pname = pn_i;
                        value = pv;
                        index++;
                    } else {
                        // log
                        throw new XWSSecurityException("Malformed RFC2184 encoded parameter");
                    }
                }
            }
            
        }
        
        // flush out an unwritten param
        if (!pname.equals(""))
            decoded += _SC + pname + _EQ + value;
        
        return decoded;
    }
    
    private String concatenate2184decoded(String v0, String v1) throws XWSSecurityException {
        boolean v0Quoted = (v0.charAt(0) == _QT.charAt(0) && v0.charAt(v0.length()-1) == _QT.charAt(0));
        boolean v1Quoted = (v1.charAt(0) == _QT.charAt(0) && v1.charAt(0) == _QT.charAt(0));
        
        if (v0Quoted != v1Quoted) {
            // log
            throw new XWSSecurityException("Malformed RFC2184 encoded parameter");
        }
        
        String value = null;
        
        if (v0Quoted) {
            value = v0.substring(0, v0.length()-1) + v1.substring(1);
        } else
            value = v0 + v1;
        
        
        return value;
    }
    
    private String quote(String input, boolean force) {
        if (input.charAt(0) == _QT.charAt(0) || input.charAt(input.length()-1) == _QT.charAt(0))
            input = _QT + unquoteInner(input.substring(1, input.length()-1)) + _QT;
        else
            if (force)
                input = _QT + quoteInner(unfoldWS(input).trim()) + _QT;
            else
                input = unfoldWS(input).trim();
        
        return input;
    }
    
    private String unquoteInner(String input) {
        StringBuffer sb = new StringBuffer();
        
        for (int i=0; i<input.length(); i++) {
            char c = input.charAt(i);
            
            if (c == _BS.charAt(0)) {
                if (i == input.length()-1) {
                    sb.append(c);
                    sb.append(c);
                    break;
                }
                
                i++;
                char d = input.charAt(i);
                if (d == _QT.charAt(0) || d == _BS.charAt(0)) {
                    sb.append(c);
                    sb.append(d);
                    continue;
                } else {
                    sb.append(d);
                    continue;
                }
            }
            
            if (c == _QT.charAt(0)) {
                sb.append(_BS.charAt(0));
                sb.append(c);
            }
            
            sb.append(c);
        }
        
        return sb.toString();
    }
    
    private String quoteInner(String input) {
        StringBuffer sb = new StringBuffer();
        
        for (int i=0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (c == _BS.charAt(0) || c == _QT.charAt(0)) sb.append(_BS.charAt(0));
            
            sb.append(c);
        }
        
        return sb.toString();
    }
    
    // rfc2822 mime header delimiters
    private static final String _WS = " ";
    private static final String _SC = ";";
    private static final String _BS = "\\";
    private static final String _FS = "/";
    private static final String _EQ = "=";
    private static final String _CL = ":";
    private static final String _OC = "(";
    private static final String _CC = ")";
    private static final String _QT = "\"";
    private static final String _SQ = "'";
    private static final String _HT = "\t";
    private static final String _AX = "*";
    private static final String _PC = "%";
    
    private static final String _CRLF = "\r\n";
}
