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
/*
 * $Id: DOMSubTreeData.java,v 1.4 2008/07/03 05:28:55 ofung Exp $
 */
package com.sun.xml.wss.impl.dsig;

import javax.xml.crypto.NodeSetData;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * This is a subtype of NodeSetData that represents a dereferenced
 * same-document URI as the root of a subdocument. The main reason is
 * for efficiency and performance, as some transforms can operate
 * directly on the subdocument and there is no need to convert it
 * first to an XPath node-set.
 */
public class DOMSubTreeData implements NodeSetData {

    private boolean excludeComments;
    private Iterator ni;
    private Node root;

    public DOMSubTreeData(Node root, boolean excludeComments) {
	this.root = root;
	this.ni = new DelayedNodeIterator(root, excludeComments);
	this.excludeComments = excludeComments;
    }

    public Iterator iterator() {
	return ni;
    }

    public Node getRoot() {
	return root;
    }

    public boolean excludeComments() {
	return excludeComments;
    }

    /**
     * This is an Iterator that contains a backing node-set that is
     * not populated until the caller first attempts to advance the iterator.
     */
    static class DelayedNodeIterator implements Iterator {
    	private Node root;
	private List nodeSet;
	private ListIterator li;
	private boolean withComments;

	DelayedNodeIterator(Node root, boolean excludeComments) {
            this.root = root;
            this.withComments = !excludeComments;
	}

        public boolean hasNext() {
            if (nodeSet == null) {
		nodeSet = dereferenceSameDocumentURI(root);
		li = nodeSet.listIterator();
            }
	    return li.hasNext();
        }

        public Object next() {
            if (nodeSet == null) {
		nodeSet = dereferenceSameDocumentURI(root);
		li = nodeSet.listIterator();
            }
            if (li.hasNext()) {
		return (Node) li.next();
            } else {
                throw new NoSuchElementException();
	    }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }

	/**
         * Dereferences a same-document URI fragment.
	 *
	 * @param node the node (document or element) referenced by the
         *	 URI fragment. If null, returns an empty set.
	 * @return a set of nodes (minus any comment nodes)
	 */
	private List dereferenceSameDocumentURI(Node node) {
            List nodeSet = new ArrayList();
            if (node != null) {
		nodeSetMinusCommentNodes(node, nodeSet, null);
            }
            return nodeSet;
	}

	/**
         * Recursively traverses the subtree, and returns an XPath-equivalent
	 * node-set of all nodes traversed, excluding any comment nodes,
	 * if specified.
	 *
         * @param node the node to traverse
	 * @param nodeSet the set of nodes traversed so far
	 * @param the previous sibling node
	 */
	private void nodeSetMinusCommentNodes(Node node, List nodeSet,
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
                         prevSibling.getNodeType() == Node.CDATA_SECTION_NODE)){			return;
                    }
		case Node.PROCESSING_INSTRUCTION_NODE :
                    nodeSet.add(node);
	            break;
		case Node.COMMENT_NODE:
		    if (withComments) { 
                        nodeSet.add(node);
		    }
            }
	}
    }
}
