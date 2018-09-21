/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

/*
 * $Id: URIResolver.java,v 1.2 2010-10-21 15:37:39 snajper Exp $
 */

package com.sun.xml.wss.impl.resolver;

import java.util.Set;
import java.util.Vector;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xml.security.utils.resolver.ResourceResolverContext;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.NamedNodeMap;

import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPException;
import javax.xml.soap.AttachmentPart;

import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.impl.SecurableSoapMessage;
import com.sun.xml.wss.XWSSecurityException;

import javax.xml.transform.TransformerException;

import com.sun.xml.wss.swa.MimeConstants;
import com.sun.xml.wss.impl.misc.URI;
import org.apache.xml.security.utils.XMLUtils;
import org.apache.xml.security.signature.XMLSignatureInput;
import org.apache.xml.security.utils.resolver.ResourceResolver;
import org.apache.xml.security.utils.resolver.ResourceResolverSpi;
import org.apache.xml.security.utils.resolver.ResourceResolverException;
import com.sun.xml.wss.WSITXMLFactory;
import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import com.sun.xml.wss.impl.dsig.NamespaceContextImpl;
import com.sun.xml.wss.logging.LogStringsMessages;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * This resolver is used for resolving URIs.
 *
 * Resolves URLs that refers to attachments that has a (1) Content-ID
 * or a (2) Content-Location MIME header. 
 *
 * In case of Content-Location, the URL may require resolution to determine 
 * the referenced attachment [RFC2557].
 *
 * Also resolves (3) URL's that are Ids on XML elements within the 
 * SOAPMessage.
 *
 * @author XWS-Security Team
 *
 */
public class URIResolver extends ResourceResolverSpi {

   int referenceType = -1;

   private SOAPMessage soapMsg = null;

   private static String implementationClassName = 
                                URIResolver.class.getName();

   protected static final Logger log =
        Logger.getLogger(
            LogDomainConstants.IMPL_DOMAIN,
            LogDomainConstants.IMPL_DOMAIN_BUNDLE);

   public URIResolver() {}

    public URIResolver(SOAPMessage soapMsg) {
        this.soapMsg = soapMsg;
    }

    @Override
    public XMLSignatureInput engineResolveURI(ResourceResolverContext context) throws ResourceResolverException {
        return engineResolve(context.attr, context.baseUri);
    }

    @Override
    public boolean engineCanResolveURI(ResourceResolverContext context) {
        return engineCanResolve(context.attr, context.baseUri);
    }

   public void setSOAPMessage(SOAPMessage soapMsg) {
        this.soapMsg = soapMsg;
   }

   /**
    * Method getResolverName
    *
    * @return The resolver implementation class name
    */
   public static String getResolverName() {
      return implementationClassName;
   }

   /**
    * Method engineResolve
    *
    * @param uri
    * @param baseURI
    *
    * @return XMLSignatureInput
    *
    * @throws ResourceResolverException
    */
   public XMLSignatureInput engineResolve(Attr uri, String baseURI)
          throws ResourceResolverException {

     XMLSignatureInput result = null;
     
      if (referenceType == -1)
          if (!engineCanResolve(uri, baseURI))
              throw generateException(uri, baseURI, errors[0]); 

      switch (referenceType) {
          case ID_REFERENCE:           
                  result = _resolveId(uri, baseURI);
                  break;
          case CID_REFERENCE:
                  result = _resolveCid(uri, baseURI);
                  break;
          case CLOCATION_REFERENCE:
                  try { 
                     result = _resolveClocation(uri, baseURI);
                  } catch (URIResolverException ure) {
                     result = ResourceResolver.getInstance(uri, baseURI, false).resolve(uri, baseURI, false);
                  }
                  break; 
          default:
      }

      referenceType = -1;
      return result;
   }      

  private XMLSignatureInput _resolveId(Attr uri, String baseUri) 
                throws ResourceResolverException {

      XMLSignatureInput result = null;

      String uriToResolve = uri != null ? uri.getValue() : null;
 
      String uriNodeValue = uri.getNodeValue();
      Document doc = uri.getOwnerDocument();

      XMLUtils.circumventBug2650(doc);
      
      Element selectedElem = null;
      if (uriNodeValue.equals("")) {
         selectedElem = doc.getDocumentElement();
      } else {
         String id = uriNodeValue.substring(1);
         try { 
            selectedElem = getElementById(doc, id);
         } catch (TransformerException e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSS_0603_XPATHAPI_TRANSFORMER_EXCEPTION(e.getMessage()),
                    e.getMessage());
            throw new ResourceResolverException("empty", e, uriToResolve, baseUri);
         }
      }

      if (selectedElem == null) {
          log.log(Level.SEVERE,
                   LogStringsMessages.WSS_0604_CANNOT_FIND_ELEMENT());
          throw new ResourceResolverException("empty", uriToResolve, baseUri);
      }

      Set resultSet = prepareNodeSet(selectedElem);
      result = new XMLSignatureInput(resultSet);
      result.setMIMEType(MimeConstants.TEXT_XML_TYPE);

      try {
         URI uriNew = new URI(new URI(baseUri), uriNodeValue);
         result.setSourceURI(uriNew.toString());
      } catch (URI.MalformedURIException ex) {
         result.setSourceURI(baseUri);
      }
      return result;
   }

   /*
    * Resolver for content-ID. 
    * 
    * A content-ID MIME header value corresponding to the URL scheme
    * is defined in RFC 2392.
    *
    * For example, content-ID of "foo" may be specified with Content-ID:<foo>
    * and be references with a CID schema URL cid:foo.
    *
    */
   private XMLSignatureInput _resolveCid(Attr uri, String baseUri) 
                throws ResourceResolverException {

      String uriToResolve = uri != null ? uri.getValue() : null;
      XMLSignatureInput result = null;
      String uriNodeValue = uri.getNodeValue();

      if (soapMsg == null) throw generateException(uri, baseUri, errors[1]);
     
      try {  
         AttachmentPart _part = 
              ((SecurableSoapMessage)soapMsg).getAttachmentPart(uriNodeValue);
         if (_part == null) {
             // log
             throw new ResourceResolverException("empty", uriToResolve, baseUri);
         } 
         Object[] obj = AttachmentSignatureInput._getSignatureInput(_part); 
         result = new AttachmentSignatureInput((byte[])obj[1]);
         ((AttachmentSignatureInput)result).setMimeHeaders((Vector)obj[0]);
         ((AttachmentSignatureInput)result).setContentType(_part.getContentType()); 
      } catch (Exception e) {
         // log
         throw new ResourceResolverException("empty", e, uriToResolve, baseUri);
      }

      try {
         URI uriNew = new URI(new URI(baseUri), uriNodeValue);
         result.setSourceURI(uriNew.toString());
      } catch (URI.MalformedURIException ex) {
         result.setSourceURI(baseUri);
      }
      return result;
   }

   private XMLSignatureInput _resolveClocation(Attr uri, String baseUri) 
                throws ResourceResolverException, URIResolverException {
      String uriToResolve = uri != null ? uri.getValue() : null;
      URI uriNew = null;
      XMLSignatureInput result = null; 
      try {
         uriNew = getNewURI(uri.getNodeValue(), baseUri);
      } catch (URI.MalformedURIException ex) {
         // log          
         throw new ResourceResolverException("empty", ex, uriToResolve, baseUri);
      }

      if (soapMsg == null) throw generateException(uri, baseUri, errors[1]);

      try {  
         AttachmentPart _part = ((SecurableSoapMessage)soapMsg).getAttachmentPart(uriNew.toString());
         if (_part == null) {
             // log  
             throw new URIResolverException();
         }
         Object[] obj = AttachmentSignatureInput._getSignatureInput(_part); 
         result = new AttachmentSignatureInput((byte[])obj[1]);
         ((AttachmentSignatureInput)result).setMimeHeaders((Vector)obj[0]);
         ((AttachmentSignatureInput)result).setContentType(_part.getContentType()); 
      } catch (XWSSecurityException e) {
         throw new ResourceResolverException("empty", e, uriToResolve, baseUri);
      } catch (SOAPException spe) {
         // log
         throw new ResourceResolverException("empty", spe, uriToResolve, baseUri);
      } catch (java.io.IOException ioe) {
         // log
         throw new ResourceResolverException("empty", ioe, uriToResolve, baseUri);
      }


      result.setSourceURI(uriNew.toString());
      return result;
   }

   /**
    * Method engineCanResolve
    *
    * @param uri
    * @param baseURI
    *
    * @return true if uri node can be resolved, false otherwise
    */
   public boolean engineCanResolve(Attr uri, String baseURI) {
      if (uri == null) return false;

      String uriNodeValue = uri.getNodeValue();
      
      /* #Id, #wsu:Id */
      if (uriNodeValue.startsWith("#")) {
          referenceType = ID_REFERENCE;
          return true; 
      } 

      /* cid:xxx */
      if (uriNodeValue.startsWith("cid:")) {
          referenceType = CID_REFERENCE;
          return true;
      }

      /* attachmentRef:xxx */
      if (uriNodeValue.startsWith("attachmentRef:")) {
          referenceType = CID_REFERENCE;
          return true;
      }

      URI uriNew = null;
      try {
         uriNew = getNewURI(uriNodeValue, baseURI);
      } catch (URI.MalformedURIException ex) {
         // log
         return false;
      }

      /* content-location of the type http:// (for now) */
      if ((uriNew != null) && 
           uriNew.getScheme().equals("http") ||
           uriNodeValue.startsWith("thismessage:/") ||
          !(uriNew.getScheme().equals("ftp") ||
            uriNew.getScheme().equals("telnet") ||
            uriNew.getScheme().equals("gopher") ||
            uriNew.getScheme().equals("news") ||
            uriNew.getScheme().equals("mailto") ||
            uriNew.getScheme().equals("file"))) {
          // log
          referenceType = CLOCATION_REFERENCE;
          return true;
      }

      return false;
    }
 
   /**
    * Looks up elements with wsu:Id or Id in xenc or dsig namespace
    *
    * @param doc
    * @param id
    *
    * @return element
    *
    * @throws TransformerException
    */
    private Element getElementById(
                    Document doc,
                    String id) 
                    throws TransformerException {

        Element  selement = doc.getElementById(id);
        if (selement != null) {
            if (MessageConstants.debug) {
                log.log(Level.FINEST, "Document.getElementById() returned " + selement);
            }
            return selement;
        }
                                                                                                                     
        if (MessageConstants.debug) {
            log.log(Level.FINEST, "Document.getElementById() FAILED......'" + id + "'");
        }

       //----------------------------------
//       Element nscontext = XMLUtils.createDSctx(doc, 
//                                                "wsu",
//                                                MessageConstants.WSU_NS);
//       Element element =
//           (Element) XPathAPI.selectSingleNode(
//               doc, "//*[@wsu:Id='" + id + "']", nscontext);
//
//       if (element == null) {
//           NodeList elems = XPathAPI.selectNodeList(
//                                           doc,
//                                           "//*[@Id='" + id + "']",
//                                           nscontext); 
//
//           for (int i=0; i < elems.getLength(); i++) {
//                Element elem = (Element)elems.item(i);
//                String namespace = elem.getNamespaceURI();
//                if (namespace.equals(MessageConstants.DSIG_NS) ||
//                    namespace.equals(MessageConstants.XENC_NS)) {
//                    element = elem;  
//                    break;
//                }
//           }  
//        }
        
        Element element = null;
        NodeList elems = null;
        String xpath =  "//*[@wsu:Id='" + id + "']";
        XPathFactory xpathFactory = WSITXMLFactory.createXPathFactory(WSITXMLFactory.DISABLE_SECURE_PROCESSING);
        XPath xPATH = xpathFactory.newXPath();
        xPATH.setNamespaceContext(getNamespaceContext(doc));
        XPathExpression xpathExpr;
        try {
            xpathExpr = xPATH.compile(xpath);
            elems = (NodeList)xpathExpr.evaluate(doc,XPathConstants.NODESET);
        } catch (XPathExpressionException ex) {
            //TODO: this logstring is not in this package
            log.log(Level.SEVERE,
                    "WSS0375.error.apache.xpathAPI",
                    new Object[] {id, ex.getMessage()});
            throw new XWSSecurityRuntimeException(ex);
        }
       
        if (elems != null) {
            if (elems.getLength() > 1) {
                //TODO: localize the string
                throw new XWSSecurityRuntimeException("XPath Query resulted in more than one node");
            } else {
                element = (Element)elems.item(0);
            }
        }
        
        if (element == null) {
            xpath =  "//*[@Id='" + id + "']";
            try {
                xpathExpr = xPATH.compile(xpath);
                elems = (NodeList)xpathExpr.evaluate(doc,XPathConstants.NODESET);
            } catch (XPathExpressionException ex) {
                //TODO: this logstring is not in this package
                log.log(Level.SEVERE,
                       LogStringsMessages.WSS_0375_ERROR_APACHE_XPATH_API(id, ex.getMessage()),
                        new Object[] {id, ex.getMessage()});
                throw new XWSSecurityRuntimeException(ex);
            }
        }
        if (elems != null) {
            if (elems.getLength() > 1) {
                for (int i=0; i < elems.getLength(); i++) {
                    Element elem = (Element)elems.item(i);
                    String namespace = elem.getNamespaceURI();
                    if (namespace.equals(MessageConstants.DSIG_NS) ||
                            namespace.equals(MessageConstants.XENC_NS)) {
                        element = elem;
                        break;
                    }
                }
                
            } else {
                element = (Element)elems.item(0);
            }
        }
        //------------------------------
   
        if (element == null) {

            NodeList assertions =
                doc.getElementsByTagNameNS(MessageConstants.SAML_v1_0_NS,
                    MessageConstants.SAML_ASSERTION_LNAME);
            int len = assertions.getLength();            
            if (len > 0) {
                for (int i=0; i < len; i++) {
                    Element elem = (Element)assertions.item(i);
                    String assertionId = elem.getAttribute(MessageConstants.SAML_ASSERTIONID_LNAME);
                    if (id.equals(assertionId)) {
                        element = elem;
                        break;
                    }
                }
            }
        }

        return element;
    }

    private ResourceResolverException generateException(Attr uri, String baseUri, String error) {
        String uriToResolve = uri != null ? uri.getValue() : null;
        XWSSecurityException xwssE = new XWSSecurityException(error);
        return new ResourceResolverException("empty", xwssE, uriToResolve, baseUri);
    }

   /**
    * prepareNodeSet
    *
    * @param node the node referenced by the -
    *             URI fragment. If null, returns -
    *             an empty set.
    * @return 
    */
   private Set prepareNodeSet(Node node) {
	Set nodeSet = new HashSet();
	if (node != null) {
	    nodeSetMinusCommentNodes(node, nodeSet, null);
	}
	return nodeSet;
   }

   /**
    * Method nodeSetMinusCommentNodes
    *
    * @param node the node to traverse
    * @param nodeSet the set of nodes traversed so far
    * @param the previous sibling node
    */
   @SuppressWarnings("unchecked")
    private void nodeSetMinusCommentNodes(Node node, Set nodeSet,
	Node prevSibling) {
	switch (node.getNodeType()) {
            case Node.ELEMENT_NODE :
		NamedNodeMap attrs = node.getAttributes();
		if (attrs != null) {
                    for (int i = 0; i<attrs.getLength(); i++) {
                        nodeSet.add(attrs.item(i));
                    }
		}
                nodeSet.add(node);
        	Node pSibling = null;
		for (Node child = node.getFirstChild(); child != null;
                    child = child.getNextSibling()) {
                    nodeSetMinusCommentNodes(child, nodeSet, pSibling);
                    pSibling = child;
		}
                break;
            case Node.TEXT_NODE :
            case Node.CDATA_SECTION_NODE:
		// emulate XPath which only returns the first node in
		// contiguous text/cdata nodes
		if (prevSibling != null &&
                    (prevSibling.getNodeType() == Node.TEXT_NODE ||
                     prevSibling.getNodeType() == Node.CDATA_SECTION_NODE)) {
                     return;
		}
            case Node.PROCESSING_INSTRUCTION_NODE :
		nodeSet.add(node);
	}
    }

    private URI getNewURI(String uri, String baseUri)
                throws URI.MalformedURIException {
        if ((baseUri == null) || "".equals(baseUri)) {
             return new URI(uri);
        } else {
             return new URI(new URI(baseUri), uri);
        }
    }
    
     public NamespaceContext getNamespaceContext(Document doc) {
            NamespaceContext nsContext = new NamespaceContextImpl();
            ((NamespaceContextImpl)nsContext).add(
                    doc.getDocumentElement().getPrefix(), doc.getDocumentElement().getNamespaceURI());
            if (doc.getDocumentElement().getNamespaceURI() == MessageConstants.SOAP_1_2_NS) {
                ((NamespaceContextImpl)nsContext).add("SOAP-ENV", MessageConstants.SOAP_1_2_NS);
                ((NamespaceContextImpl)nsContext).add("env", MessageConstants.SOAP_1_2_NS);
            }
            ((NamespaceContextImpl)nsContext).add("wsu", MessageConstants.WSU_NS);
            ((NamespaceContextImpl)nsContext).add("wsse", MessageConstants.WSSE_NS);
        return nsContext;
    }

    private static final class URIResolverException extends Exception {};

    private static final int ID_REFERENCE  = 0;
    private static final int CID_REFERENCE = 1;
    private static final int CLOCATION_REFERENCE  = 2;

    private final String[] errors = new String[] {
                                    "Can not resolve reference type",
                                    "Required SOAPMessage instance to resolve reference"};
}

