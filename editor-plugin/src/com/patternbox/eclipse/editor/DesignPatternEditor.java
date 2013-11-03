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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.pde.core.build.IBuildModel;
import org.eclipse.pde.internal.core.build.IBuildObject;
import org.eclipse.pde.internal.ui.PDEPlugin;
import org.eclipse.pde.internal.ui.editor.ISortableContentOutlinePage;
import org.eclipse.pde.internal.ui.editor.MultiSourceEditor;
import org.eclipse.pde.internal.ui.editor.PDEFormEditor;
import org.eclipse.pde.internal.ui.editor.PDEFormPage;
import org.eclipse.pde.internal.ui.editor.PDESourcePage;
//import org.eclipse.pde.internal.ui.editor.SystemFileEditorInput;
import org.eclipse.pde.internal.ui.editor.context.InputContext;
import org.eclipse.pde.internal.ui.editor.context.InputContextManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.properties.IPropertySheetPage;

import com.patternbox.eclipse.editor.form.ParticipantPage;

/**
 * Design pattern plugin editor class
 * 
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class DesignPatternEditor extends MultiSourceEditor {

	private ParticipantPage fMainPage;

	public DesignPatternEditor() {
	}

	@Override
	protected void createResourceContexts(InputContextManager manager, IFileEditorInput input) {
		IFile file = input.getFile();
		manager.putContext(input, new DesignPatternInputContext(this, input, true));
		manager.monitorFile(file);
	}

	@Override
	protected InputContextManager createInputContextManager() {
		DesignPatternInputContextManager manager = new DesignPatternInputContextManager(this);
		// manager.setUndoManager(new BuildUndoManager(this));
		return manager;
	}

	@Override
	public void monitoredFileAdded(IFile file) {
		String name = file.getName();
		if (name.equalsIgnoreCase("build.properties")) { //$NON-NLS-1$
			if (!fInputContextManager.hasContext(DesignPatternInputContext.CONTEXT_ID)) {
				IEditorInput in = new FileEditorInput(file);
				fInputContextManager.putContext(in, new DesignPatternInputContext(this, in, false));
			}
		}
	}

	@Override
	public boolean monitoredFileRemoved(IFile file) {
		// TODO may need to check with the user if there
		// are unsaved changes in the model for the
		// file that just got removed under us.
		return true;
	}

	@Override
	public void editorContextAdded(InputContext context) {
		addSourcePage(context.getId());
	}

	@Override
	public void contextRemoved(InputContext context) {
		if (context.isPrimary()) {
			close(true);
			return;
		}
		IFormPage page = findPage(context.getId());
		if (page != null)
			removePage(context.getId());
	}

	@Override
	protected void createStorageContexts(InputContextManager manager, IStorageEditorInput input) {
		manager.putContext(input, new DesignPatternInputContext(this, input, true));
	}

	@Override
	protected void addEditorPages() {
		try {
			if (getEditorInput() instanceof IFileEditorInput) {
				fMainPage = new ParticipantPage(this);
				addPage(fMainPage);
				// addPage(new DesignPatternMasterPage(this));
			}
		} catch (PartInitException e) {
			PDEPlugin.logException(e);
		}
		addSourcePage(DesignPatternInputContext.CONTEXT_ID);
	}

	@Override
	protected String computeInitialPageId() {
		String firstPageId = super.computeInitialPageId();
		if (firstPageId == null) {
			InputContext primary = fInputContextManager.getPrimaryContext();
			if (primary.getId().equals(DesignPatternInputContext.CONTEXT_ID))
				firstPageId = ParticipantPage.PAGE_ID;
			if (firstPageId == null)
				firstPageId = ParticipantPage.PAGE_ID;
		}
		return firstPageId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.pde.internal.ui.neweditor.MultiSourceEditor#createXMLSourcePage(org.eclipse.pde
	 * .internal.ui.neweditor.PDEFormEditor, java.lang.String, java.lang.String)
	 */
	@Override
	protected PDESourcePage createSourcePage(PDEFormEditor editor, String title, String name,
			String contextId) {
		// return new DesignPatternSourcePage(editor, "Source", "Source");
		return new DesignPatternSourcePage(editor, title, name);
	}

	@Override
	protected ISortableContentOutlinePage createContentOutline() {
		return new DesignPatternOutlinePage(this);
	}

	protected IPropertySheetPage getPropertySheet(PDEFormPage page) {
		return null;
	}

	@Override
	public String getTitle() {
		return super.getTitle();
	}

	protected boolean isModelCorrect(Object model) {
		return model != null ? ((IBuildModel) model).isValid() : false;
	}

	protected boolean hasKnownTypes() {
		return false;
	}

	@Override
	public Object getAdapter(Class key) {
		// No property sheet needed - block super
		if (key.equals(IPropertySheetPage.class)) {
			return null;
		}
		return super.getAdapter(key);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#getInputContext(java.lang.Object)
	 */
	@Override
	protected InputContext getInputContext(Object object) {
		InputContext context = null;
		if (object instanceof IBuildObject) {
			context = fInputContextManager.findContext(DesignPatternInputContext.CONTEXT_ID);
		}
		return context;
	}

	/**
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {
		fMainPage.doSave(monitor);
		// super.doSave(monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.pde.internal.ui.editor.PDEFormEditor#getEditorID()
	 */
	@Override
	protected String getEditorID() {
		return DesignPatternPlugin.getPluginId();
		// return "com.patternbox.eclipse.editor";
	}

	/*
	 * protected void createSystemFileContexts(InputContextManager manager, SystemFileEditorInput
	 * input) { manager.putContext(input, new DesignPatternInputContext(this, input, true)); }
	 */
	@Override
	protected void createSystemFileContexts(InputContextManager contexts, FileStoreEditorInput input) {
		contexts.putContext(input, new DesignPatternInputContext(this, input, true));
	}
}
