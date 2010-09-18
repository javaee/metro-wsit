/*
 * $Id: ResolverId.java,v 1.4 2010-09-18 20:35:47 sm228678 Exp $
 */

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

package com.sun.xml.wss.impl.resolver;

import com.sun.xml.wss.impl.XWSSecurityRuntimeException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

//import com.sun.org.apache.xml.internal.utils.URI;
import com.sun.xml.wss.impl.misc.URI;
import com.sun.org.apache.xml.internal.security.signature.XMLSignatureInput;
import com.sun.org.apache.xml.internal.security.utils.XMLUtils;
import com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverException;
import com.sun.org.apache.xml.internal.security.utils.resolver.ResourceResolverSpi;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.MessageConstants;
import com.sun.xml.wss.impl.dsig.NamespaceContextImpl;
import com.sun.xml.wss.logging.LogDomainConstants;
import com.sun.xml.wss.logging.LogStringsMessages;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;


/**
 * This resolver is used for resolving same-document URIs for eg. URI="#wsuId".
 *
 * @author Anil Tappetla
 */

public class ResolverId extends ResourceResolverSpi {

   /* Implementation class name */
   private static String implementationClassName = 
                           ResolverId.class.getName();

   protected static final Logger log =
        Logger.getLogger(
            LogDomainConstants.WSS_API_DOMAIN,
            LogDomainConstants.WSS_API_DOMAIN_BUNDLE);

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
    * @param BaseURI
    *
    * @return XMLSignatureInput
    *
    * @throws ResourceResolverException
    */
   public XMLSignatureInput engineResolve(Attr uri, String BaseURI)
           throws ResourceResolverException {
      String uriNodeValue = uri.getNodeValue();
      if (MessageConstants.debug) {
          log.log(Level.FINEST, "uri = " + uriNodeValue);
      }
      Document doc = uri.getOwnerDocument();

      // this must be done so that Xalan can catch ALL namespaces
      XMLUtils.circumventBug2650(doc);

      Element selectedElem = null;
      if (uriNodeValue.equals("")) {
         selectedElem = doc.getDocumentElement();
      } else {
         /*
          * URI="#wsuId"
          * Identifies a node-set containing the element with wsu:Id attribute
          * value 'wsuId' of the XML resource containing the signature.
          * XML Signature (and its applications) modify this node-set to
          * include the element plus all descendents including namespaces and
          * attributes -- but not comments.
          */
         String id = uriNodeValue.substring(1);
         try { 
            selectedElem = getElementById(doc, id);
         } catch (TransformerException e) {
            log.log(Level.SEVERE,
                    LogStringsMessages.WSS_0603_XPATHAPI_TRANSFORMER_EXCEPTION(e.getMessage()),
                    e.getMessage());
            throw new ResourceResolverException("empty", e, uri, BaseURI);
         }
      }

      if (selectedElem == null) {
          log.log(Level.SEVERE,
                  LogStringsMessages.WSS_0604_CANNOT_FIND_ELEMENT());
          throw new ResourceResolverException("empty", uri, BaseURI);
      }
      Set resultSet = dereferenceSameDocumentURI(selectedElem);
      XMLSignatureInput result = new XMLSignatureInput(resultSet);

      result.setMIMEType("text/xml");

      try {
         URI uriNew = new URI(new URI(BaseURI), uri.getNodeValue());
         result.setSourceURI(uriNew.toString());
      } catch (URI.MalformedURIException ex) {
         result.setSourceURI(BaseURI);         
      }

      return result;
   }

   /**
    * Method engineCanResolve
    *
    * @param uri
    * @param BaseURI
    *
    * @return true if uri node can be resolved, false otherwise
    */
   public boolean engineCanResolve(Attr uri, String BaseURI) {
      if (uri == null) return false;

      String uriNodeValue = uri.getNodeValue();
      if (uriNodeValue.startsWith("#")) return true;

      return false;
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
      
       Element element = null;
      //------------------------
        NodeList elems = null;
        String xpath =  "//*[@wsu:Id='" + id + "']";
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xPATH = xpathFactory.newXPath();
        xPATH.setNamespaceContext(getNamespaceContext(doc));
        XPathExpression xpathExpr;
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
      
      /*
      Element nscontext = XMLUtils.createDSctx(doc, 
                                               "wsu",
                                               MessageConstants.WSU_NS);
      Element element =
          (Element) XPathAPI.selectSingleNode(
              doc, "//*[@wsu:Id='" + id + "']", nscontext);

      if (element == null) {
         NodeList elems = XPathAPI.selectNodeList(
                                          doc,
                                          "//*[@Id='" + id + "']",
                                          nscontext); 
         for (int i=0; i < elems.getLength(); i++) {
             Element elem = (Element)elems.item(i);
             String namespace = elem.getNamespaceURI();
             if (namespace.equals(MessageConstants.DSIG_NS) ||
                 namespace.equals(MessageConstants.XENC_NS)) {
                element = elem;  
                break;
             }
         }  
      }
       */
       //-----------------------
       
      // look for SAML AssertionID
      if (element == null) {

          NodeList assertions = doc.getElementsByTagName(MessageConstants.SAML_ASSERTION_LNAME);

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

   /**
    * Dereferences a same-document URI fragment.
    *
    * @param node the node referenced by the -
    *             URI fragment. If null, returns -
    *             an empty set.
    * @return 
    */
   private Set dereferenceSameDocumentURI(Node node) {
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
}

