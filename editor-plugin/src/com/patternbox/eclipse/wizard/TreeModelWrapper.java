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

package com.patternbox.eclipse.wizard;

import org.eclipse.jface.viewers.LabelProvider;
//import org.eclipse.pde.internal.core.plugin.DocumentNode;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

import com.patternbox.commons.model.DocumentNode;
import com.patternbox.commons.model.ITreeModel;

/**
 * This class adapts a tree model interface for using it together with a tree.
 *
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class TreeModelWrapper {

   private final TreeItem[] fRootList;
   private final LabelProvider fLabelProvider;

   /**
    * Default constructor
    */
   public TreeModelWrapper(ITreeModel model, Tree tree, LabelProvider labelProvider) {
      // assign label provider
      fLabelProvider = labelProvider;
      // assign tree item root list
      fRootList = appendChildren(model.getRootNode(), tree);
   }

   /**
    * Default constructor
    */
   public TreeModelWrapper(DocumentNode rootNode, Tree tree, LabelProvider labelProvider) {
      // assign label provider
      fLabelProvider = labelProvider;
      // assign tree item root list
      fRootList = appendChildren(rootNode, tree);
   }

   private TreeItem[] appendChildren(DocumentNode docNode, Object itemParent) {

      DocumentNode[] childNodes = docNode.getChildren();

      // check whether children are available
      if (childNodes.length == 0) return null;

      TreeItem[] result = new TreeItem[childNodes.length];

      for (int i=0; i<childNodes.length; i++) {
         // we must distinguish between tree and tree items
         if (itemParent instanceof Tree) {
            result[i] = new TreeItem((Tree) itemParent, 0);
         } else {
            result[i] = new TreeItem((TreeItem) itemParent, 0);
         }
         // set additional item properties
         result[i].setText(fLabelProvider.getText(childNodes[i]));
         result[i].setImage(fLabelProvider.getImage(childNodes[i]));
         result[i].setData(childNodes[i]);
         // append child nodes to the current node
         appendChildren(childNodes[i], result[i]);
      }  // for

      return result;

   }

   /**
    *
    * @return List of all tree root nodes
    */
   public TreeItem[] getModelRootList() {
      return fRootList;
   }

}
