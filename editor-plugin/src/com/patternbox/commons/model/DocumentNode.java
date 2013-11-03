/**************************** Copyright notice ********************************

Copyright (C) 2003-2012 by Dirk Ehms, http://www.patternbox.com. All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:
1. Redistributions of source code must retain the above copyright
notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions and the following disclaimer in the
documentation and/or other materials provided with the distribution.
THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

******************************************************************************/

package com.patternbox.commons.model;

import java.util.*;

/**
 * Abstract tree node class. The access must be done with the tree model.
 * @see com.patternbox.commons.model.ITreeModel
 *
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class DocumentNode { // implements IDocumentNode {

   // ------------------------------------------------------------------------- Field Definitions

   private final ArrayList<DocumentNode> fChildren = new ArrayList<DocumentNode>();
   private DocumentNode fParent;

   // ------------------------------------------------------------------------- Constructors

   /**
    * Creates a new instance of a tree node and appends itself to
    * its parent node.
    *
    * @param parent Parent tree node of this instance.
    */
   public DocumentNode(DocumentNode parent) {
      setParent(parent);
   }


   // ------------------------------------------------------------------------- Private Methods


   /**
    * Appends a child node to its parent node
    */
   private void appendChild(DocumentNode child) {
      fChildren.add(child);
   }

   // ------------------------------------------------------------------------- Public Methods

   /**
    * Removes a child node
    * @return <code>true</code>, if successful
    */
   public boolean removeChild(DocumentNode child) {
      return fChildren.remove(child);
   }

   /**
    * Returns the child nodes of the current instance.
    * The result is not modified by the requester.
    *
    * @return an array of child nodes
    */
   public DocumentNode[] getChildren() {
      return fChildren.toArray(new DocumentNode[fChildren.size()]);
   }

   /**
    * Return a collection of all associated child nodes.
    * @return Collection of children node
    */
   public Collection<DocumentNode> getChildrenCollection() {
      return fChildren;
   }

   /**
    * Returns the parent of the current instance, or <code>null</code>
    * indicating that the parent can't be computed.
    *
    * @return TreeNode the parent tree node, or <code>null</code> if it
    *   has none or if the parent cannot be computed
    */
   public DocumentNode getParent() {
      return fParent;
   }

   /**
    * @see org.eclipse.pde.internal.core.plugin.IDocumentNode#setParent(org.eclipse.pde.internal.core.plugin.IDocumentNode)
    */
   public void setParent(DocumentNode parentNode) {

      // remove old parent
      if (fParent != null) {
         fParent.fChildren.remove(this);
      }  // if

      // set new parent
      fParent = parentNode;

      // append current node as child to its parent node
      if (fParent != null) {
         fParent.appendChild(this);
      }  // if

   }

   /**
    * Returns whether the current instance has children.
    *
    * @return <code>true</code> if the current instance has children,
    *  and <code>false</code> if it has no children
    */
   public boolean hasChildren() {
      return !fChildren.isEmpty();
   }

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return (fParent == null) ? "root node" : super.toString();
	}

	/**
	 * @see org.eclipse.pde.internal.core.plugin.IDocumentNode#getSourceRange()
	 */
	/*public ISourceRange getSourceRange() {
		return null;
	}*/

}
