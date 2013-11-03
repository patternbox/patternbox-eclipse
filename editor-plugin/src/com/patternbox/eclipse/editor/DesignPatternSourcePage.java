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

package com.patternbox.eclipse.editor;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.pde.internal.ui.editor.ISortableContentOutlinePage;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.XMLSourcePage;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

public class DesignPatternSourcePage extends XMLSourcePage {

   /**
    * Constructor.
    * 
    * @param editor
    * @param id
    * @param title
    */
   public DesignPatternSourcePage(PDEFormEditor editor, String id, String title) {
      super(editor, id, title);
   }

   public IContentOutlinePage createContentOutlinePage() {
      return null;
   }

   public ILabelProvider createOutlineLabelProvider() {
      return null;
   }

   public ITreeContentProvider createOutlineContentProvider() {
      return null;
   }

   /*
    * (non-Javadoc)
    * 
    * @see org.eclipse.pde.internal.ui.editor.PDESourcePage#createOutlineSorter()
    */
   protected ViewerSorter createOutlineSorter() {
      return null;
   }

   protected void outlineSelectionChanged(SelectionChangedEvent e) {
   }

   /**
    * @see org.eclipse.pde.internal.ui.editor.PDESourcePage#createOutlinePage()
    */
   protected ISortableContentOutlinePage createOutlinePage() {
      // TODO remove this method when the above three stubs
      // are implemented
      return new DesignPatternOutlinePage((PDEFormEditor) getEditor());
   }

   /**
    * @see org.eclipse.ui.texteditor.AbstractTextEditor#isEditable()
    */
   public boolean isEditable() {
      return false;
   }

   /* (non-Javadoc)
    * @see org.eclipse.pde.internal.ui.editor.PDEProjectionSourcePage#isQuickOutlineEnabled()
    */
   @Override
   public boolean isQuickOutlineEnabled() {
      return false;
   }

   /* (non-Javadoc)
    * @see org.eclipse.pde.internal.ui.editor.PDESourcePage#createOutlineComparator()
    */
   @Override
   public ViewerComparator createOutlineComparator() {
      return null;
   }

   /* (non-Javadoc)
    * @see org.eclipse.pde.internal.ui.editor.PDESourcePage#updateSelection(java.lang.Object)
    */
   @Override
   public void updateSelection(Object object) {
   }
}
