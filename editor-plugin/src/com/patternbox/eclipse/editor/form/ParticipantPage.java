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

package com.patternbox.eclipse.editor.form;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.IBaseModel;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import com.patternbox.eclipse.editor.DesignPatternPlugin;
import com.patternbox.eclipse.model.DesignPatternInstance;

/**
 * Design pattern participant form page
 *
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class ParticipantPage extends PDEFormPage {

   public static final String PAGE_ID = "Design Pattern"; //$NON-NLS-1$
   private static final String PAGE_HEADING = "PatternEditor.MainPage.heading"; //$NON-NLS-1$
   private static final String PAGE_TAB_NAME = "PatternEditor.MainPage.tabName"; //$NON-NLS-1$

   private SectPatternParticipants fSectParticipants;
   
   private DesignPatternInstance fModel;

	/**
	 * @param editor
	 */
	public ParticipantPage(FormEditor editor) {
		super(editor, PAGE_ID, DesignPatternPlugin.getResourceString(PAGE_TAB_NAME));
	}
	
	/**
	 * Saves all model changes to the underlying resource
	 * @throws IOException
	 * @throws CoreException
	 */
	private void saveModel() throws IOException, CoreException {
	   
	   IFile inFile = fModel.getInputFile();
	   
	   InputStream inStream = new ByteArrayInputStream(fModel.getXmlString().getBytes());
	   
	   inFile.setContents(inStream, true, true, null);
	   
	   inStream.close();
	   
	   fModel.setDirty(false);
	   
	}
	
	protected void createFormContent(IManagedForm managedForm) {

		super.createFormContent(managedForm);
		ScrolledForm form = managedForm.getForm();

      // compose page heading
      StringBuffer heading = new StringBuffer();
      heading.append(DesignPatternPlugin.getResourceString(PAGE_HEADING));
      heading.append(": ");
      if (fModel != null) {
          heading.append(fModel.getDesignPatternName());    	  
      }

      // set page heading
		form.setText(heading.toString());

		Composite body = form.getBody();
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.makeColumnsEqualWidth = true;
		layout.marginWidth = 10;
		layout.verticalSpacing = 20;
		layout.horizontalSpacing = 10;
		body.setLayout(layout);

		fSectParticipants = new SectPatternParticipants(this, body);
		fSectParticipants.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
		managedForm.addPart(fSectParticipants);

	}
	
   /**
    * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
    */
   protected void setInput(IEditorInput input) {
      try {
         fModel = new DesignPatternInstance(input);
         this.setPartName(DesignPatternPlugin.getResourceString(PAGE_TAB_NAME) + 
                  " (" + fModel.getDesignPatternName() + ")");
      } catch (CoreException e) {
         DesignPatternPlugin.logException(e);
      }
   }
   
   /**
    * @see org.eclipse.ui.ISaveablePart#doSave(org.eclipse.core.runtime.IProgressMonitor)
    */
   public void doSave(IProgressMonitor monitor) {
      
      try {
         saveModel();
      } catch (Exception e) {
         DesignPatternPlugin.logException(e);
         return;
      }
      
      // reset dirty state
      fModel.setDirty(false);
      getEditor().editorDirtyStateChanged();
      
      //FileEditorInput input = (FileEditorInput) getEditorInput();
      
      //IPath path = input.getFile().getLocation();
      
		/*StringWriter swriter = new StringWriter();
		
		
		
		OutputStream outStream = new FileOutputStream();
		
		
      PrintWriter writer = new PrintWriter();
      
      fModel.save(getEditorInput());*/
   }
   
   /**
    * @see org.eclipse.pde.internal.ui.editor.PDEFormPage#getModel()
    */
   public IBaseModel getModel() {
      return fModel;
   }
   
}
