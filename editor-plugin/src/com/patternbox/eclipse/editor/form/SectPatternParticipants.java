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

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;
import org.eclipse.pde.core.IModelProviderEvent;
import org.eclipse.pde.core.IModelProviderListener;
import org.eclipse.pde.internal.ui.editor.TreeSection;
import org.eclipse.pde.internal.ui.parts.TreePart;
import org.eclipse.pde.internal.ui.util.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.ide.IDE;

import com.patternbox.commons.model.DocumentNode;
import com.patternbox.commons.model.TreeContentProviderImpl;
import com.patternbox.eclipse.codegen.MemberCodeGenerator;
import com.patternbox.eclipse.editor.DesignPatternPlugin;
import com.patternbox.eclipse.model.DesignPatternInstance;
import com.patternbox.eclipse.model.DesignPatternPool;
import com.patternbox.eclipse.model.MemberNode;
import com.patternbox.eclipse.model.RoleNode;
import com.patternbox.eclipse.wizard.RoleMemberWizard;

/**
 * Design pattern participant tree view
 * 
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class SectPatternParticipants extends TreeSection implements IModelChangedListener,
		IModelProviderListener {

	// ------------------------------------------------------------------------- Field Definitions
	// private static final String SECTION_TITLE = "PatternEditor.ParticipantSect.title";
	private static final String ACTION_INSERT = "PatternEditor.ParticipantSect.insert";

	private static final String ACTION_REMOVE = "PatternEditor.ParticipantSect.remove";

	private static final String ACTION_BROWSE = "PatternEditor.ParticipantSect.browse";

	// private static final String ACTION_HELP = "PatternEditor.ParticipantSect.help";
	private static final String REMOVE_DLG_TITLE = "PatternEditor.Question.Message.title";

	private static final String REMOVE_DLG_MSG = "PatternEditor.Question.Message.remove";

	private static final int ACTION_INSERT_IDX = 0;

	private static final int ACTION_REMOVE_IDX = 1;

	private static final int ACTION_BROWSE_IDX = 3;

	private static final int ACTION_HELP_IDX = 5;

	private TreeViewer fParticipantTree;

	private final Display fDisplay;

	private DocumentNode fActiveNode;

	// ------------------------------------------------------------------------- Constructors
	/**
	 * Default constructor
	 * 
	 * @param page
	 *          Owner page
	 */
	public SectPatternParticipants(ParticipantPage page, Composite parent) {
		super(page, parent, Section.DESCRIPTION, getButtonLabels());
		// handleDefaultButton = false; v3.3
		this.fHandleDefaultButton = false; // v.3.3
		final String modelID = getDesignPatternInstance().getModelID();
		final String comment = DesignPatternPool.getDesignPatternModel(modelID).getComment();
		getSection().setDescription(comment + '\n' + (char) 160);
		fDisplay = parent.getDisplay();
	}

	// ------------------------------------------------------------------------- Private Methods
	/**
	 * Creates an array of button labels
	 * 
	 * @return button labels
	 */
	private static String[] getButtonLabels() {
		return new String[] { DesignPatternPlugin.getResourceString(ACTION_INSERT),
				DesignPatternPlugin.getResourceString(ACTION_REMOVE), null,
				DesignPatternPlugin.getResourceString(ACTION_BROWSE) };
	}

	private DesignPatternInstance getDesignPatternInstance() {
		return (DesignPatternInstance) getPage().getModel();
	}

	/**
	 * Update buttons accessibility
	 * 
	 * @param item
	 *          Currently selected tree item
	 */
	private void updateButtons(Object item) {
		// final boolean itemAvail = (item != null && isEditable());
		final boolean itemAvail = (item != null);
		final boolean roleNodeSelected = (itemAvail && item instanceof RoleNode);
		final boolean memberNodeSelected = (itemAvail && item instanceof MemberNode);
		boolean insertBtnEnabled = roleNodeSelected;
		if (roleNodeSelected) {
			// type cast role node
			RoleNode roleNode = (RoleNode) item;
			// check member count
			insertBtnEnabled = roleNode.getChildren().length < roleNode.getMaxOccurence();
			// get dependence list
			final String[] roleList = roleNode.getDependenceRoles();
			// check whether all depended role has got members
			for (int i = 0; roleList != null && i < roleList.length; i++) {
				insertBtnEnabled &= getDesignPatternInstance().getRole(roleList[i]).hasChildren();
			} // for
		} // if
		final boolean browseBtnEnabled = memberNodeSelected && ((MemberNode) item).isResourceAvail();
		getTreePart().setButtonEnabled(ACTION_INSERT_IDX, insertBtnEnabled);
		getTreePart().setButtonEnabled(ACTION_REMOVE_IDX, memberNodeSelected);
		getTreePart().setButtonEnabled(ACTION_BROWSE_IDX, browseBtnEnabled);
	}

	/**
	 * @see org.eclipse.pde.internal.ui.editor.PDESection#createClient(org.eclipse.ui.forms.widgets.Section,
	 *      org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	protected void createClient(Section section, FormToolkit toolkit) {
		Composite container = createClientContainer(section, 2, toolkit);
		TreePart treePart = getTreePart();
		createViewerPartControl(container, SWT.MULTI, 2, toolkit);
		fParticipantTree = treePart.getTreeViewer();
		fParticipantTree.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
		fParticipantTree.setContentProvider(new TreeContentProviderImpl());
		fParticipantTree.setLabelProvider(DesignPatternPlugin.getDefault().getImageProvider());
		fParticipantTree.setInput(getDesignPatternInstance().getRoot());
		fParticipantTree.collapseAll();
		toolkit.paintBordersFor(container);
		section.setClient(container);
		// disable buttons
		updateButtons(null);
	}

	@Override
	protected void selectionChanged(IStructuredSelection selection) {
		fActiveNode = (DocumentNode) selection.getFirstElement();
		// fireSelectionNotification(fActiveNode);
		// getPage().getPDEEditor().setSelection(selection);
		updateButtons(fActiveNode);
	}

	/**
	 * Handles mouse double click
	 */
	@Override
	protected void handleDoubleClick(IStructuredSelection selection) {
		if (selection.getFirstElement() == fActiveNode && fActiveNode instanceof MemberNode
				&& ((MemberNode) fActiveNode).isResourceAvail()) {
			// open editor to browse role-member source
			handleBrowse();
		} else if (selection.getFirstElement() == fActiveNode && fActiveNode instanceof RoleNode) {
			// expand / collapse role node depending on state
			if (fParticipantTree.getExpandedState(fActiveNode)) {
				fParticipantTree.collapseToLevel(fActiveNode, TreeViewer.ALL_LEVELS);
			} else {
				fParticipantTree.expandToLevel(fActiveNode, TreeViewer.ALL_LEVELS);
			}
		} // if - else
	}

	@Override
	protected void buttonSelected(int index) {
		switch (index) {
		case ACTION_INSERT_IDX:
			handleInsert();
			break;
		case ACTION_REMOVE_IDX:
			handleRemove();
			break;
		case ACTION_BROWSE_IDX:
			handleBrowse();
			break;
		case ACTION_HELP_IDX:
			handleHelp();
			break;
		} // switch
		// update images
		fParticipantTree.refresh();
	}

	/**
	 * @see org.eclipse.ui.forms.IFormPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		return getDesignPatternInstance().isDirty();
	}

	@Override
	public void dispose() {
		getDesignPatternInstance().removeModelChangedListener(this);
		super.dispose();
	}

	@Override
	public boolean doGlobalAction(String actionId) {
		if (actionId.equals(ActionFactory.DELETE)) {
			boolean isMemberNode = fActiveNode instanceof MemberNode;
			if (isMemberNode) {
				handleRemove();
				// update images
				fParticipantTree.refresh();
			}
			return isMemberNode;
		}
		return false;
	}

	public void expandTo(Object object) {
		fParticipantTree.setSelection(new StructuredSelection(object), true);
	}

	private void handleAutoReplacement(final MemberCodeGenerator codeGenerator) {
		// check whether automatic replacement is needed
		final java.util.List<MemberNode> replaceList = codeGenerator.getAutomaticReplaceList();
		// get current active page
		final IWorkbenchPage activePage = DesignPatternPlugin.getActivePage();
		// if (!replaceList.isEmpty() && activePage != null && fDisplay != null) {
		if (activePage != null && fDisplay != null) {
			// declare local variables
			IType member;
			ICompilationUnit cu;
			IFile resource;
			final Iterator<MemberNode> it = replaceList.iterator();
			while (it.hasNext()) {
				member = it.next().getMemberType();
				// cu = JavaModelUtil.toOriginal(member.getCompilationUnit()); V3.2
				cu = ((IMember) member.getPrimaryElement()).getCompilationUnit();
				resource = (IFile) cu.getResource();
				try {
					if (resource != null) {
						IDE.openEditor(activePage, resource, true);
					}
				} catch (PartInitException e) {
					DesignPatternPlugin.log(e);
				} // try - catch
			} // while
			// replace all member nodes of the replace collection
			codeGenerator.handleAutomaticReplace(replaceList);
		} // if
	}

	/**
	 * Handle action: insert new design pattern participant member
	 */
	void handleInsert() {
		final RoleNode roleNode = (RoleNode) fActiveNode;
		BusyIndicator.showWhile(fParticipantTree.getTree().getDisplay(), new Runnable() {

			@Override
			public void run() {
				RoleMemberWizard wizard = new RoleMemberWizard(getDesignPatternInstance(), roleNode);
				IWorkbench workbench = DesignPatternPlugin.getActiveWorkbenchWindow().getWorkbench();
				wizard.init(workbench, null);
				WizardDialog dialog = new WizardDialog(DesignPatternPlugin.getActiveWorkbenchShell(),
						wizard);
				dialog.create();
				SWTUtil.setDialogSize(dialog, 300, 200);
				// show member creation dialog
				if (dialog.open() == Window.OK) {
					// get code generator reference
					final MemberCodeGenerator codeGen = wizard.getCodeGenerator();
					// append new member node
					MemberNode member = new MemberNode((RoleNode) fActiveNode, codeGen.getCreatedType());
					// handle automatic code replacement
					handleAutoReplacement(codeGen);
					// show new member node
					fParticipantTree.refresh(fActiveNode);
					// select new member node
					fParticipantTree.setSelection(new StructuredSelection(member));
					// mark underlying model as dirty
					getDesignPatternInstance().setDirty(true);
					// fire model is changed
					getPage().getEditor().editorDirtyStateChanged();
				} // if
			} // func run()
		});
	}

	/**
	 * Handle action: delete selected role node
	 */
	private void handleRemove() {
		// get shell reference
		Shell shell = DesignPatternPlugin.getActiveWorkbenchShell();
		// format dialog strings
		final String title = DesignPatternPlugin.getResourceString(REMOVE_DLG_TITLE);
		final String msg = DesignPatternPlugin.getFormattedMessage(REMOVE_DLG_MSG,
				fActiveNode.toString());
		// open dialog and request user confirmation
		if (MessageDialog.openQuestion(shell, title, msg)) {
			// get role node access
			Object roleNode = fActiveNode.getParent();
			((MemberNode) fActiveNode).delete();
			fParticipantTree.refresh(roleNode);
			// select role node
			fParticipantTree.setSelection(new StructuredSelection(roleNode));
			// mark underlying model as dirty
			getDesignPatternInstance().setDirty(true);
			// fire model is changed
			getPage().getEditor().editorDirtyStateChanged();
		} // if
	}

	/**
	 * Handle action: open selected Java file
	 */
	void handleBrowse() {
		final IType member = ((MemberNode) fActiveNode).getMemberType();
		// final ICompilationUnit cu = JavaModelUtil.toOriginal(member.getCompilationUnit());
		final ICompilationUnit cu = ((IMember) member.getPrimaryElement()).getCompilationUnit();
		final IFile resource = (IFile) cu.getResource();
		final IWorkbenchPage activePage = DesignPatternPlugin.getActivePage();
		if (activePage != null) {
			if (fDisplay != null) {
				fDisplay.asyncExec(new Runnable() {

					@Override
					public void run() {
						try {
							if (resource != null) {
								IDE.openEditor(activePage, resource, true);
								// activePage.openEditor(new FileEditorInput(resource),
								// DesignPatternPlugin.DESIGN_PATTERN_EDITOR_ID);
							}
						} catch (PartInitException e) {
							DesignPatternPlugin.log(e);
						} // try - catch
					} // run
				});
			} // if
		} // if
	}

	// void handleReplace() {
	//
	// final IType member = ((MemberNode) fActiveNode).getMemberType();
	// final ICompilationUnit cu = JavaModelUtil.toOriginal(member.getCompilationUnit());
	//
	// final IFile resource = (IFile) cu.getResource();
	// final IWorkbenchPage activePage= DesignPatternPlugin.getActivePage();
	//
	// if (activePage != null) {
	// final Display display = fParent.getDisplay();
	// //final Display display = getShell().getDisplay();
	// if (display != null) {
	// display.asyncExec(new Runnable() {
	// public void run() {
	// try {
	// final IProgressMonitor monitor = new NullProgressMonitor();
	// cu.createImport("com.patternbox.Hallo", null, monitor);
	// IBuffer buffer = cu.getBuffer();
	// buffer.replace(200, 10, "AUTO-REPLACE-TEST");
	// buffer.save(monitor, false);
	// if (resource != null) activePage.openEditor(resource);
	// } catch (JavaModelException e) {
	// DesignPatternPlugin.log(e);
	// } catch (PartInitException e) {
	// DesignPatternPlugin.log(e);
	// } // try - catch
	// } // run
	// });
	// } // if
	// }
	// }
	/*
	 * void handleInsert2() {
	 * 
	 * BusyIndicator.showWhile(fParticipantTree.getTree().getDisplay(), new Runnable() {
	 * 
	 * public void run() {
	 * 
	 * // JavaAttributeValue value = (JavaAttributeValue)getValue(); // IProject project =
	 * value.getProject(); // IPluginModelBase model = value.getModel(); // ISchemaAttribute attInfo =
	 * value.getAttributeInfo();
	 * 
	 * //JavaAttributeValue value = null; IProject project = null; IPluginModelBase model = null;
	 * ISchemaAttribute attInfo = null;
	 * 
	 * JavaAttributeWizard wizard = new JavaAttributeWizard(project, model, attInfo, "HalloBallo");
	 * WizardDialog dialog = new WizardDialog(DesignPatternPlugin.getActiveWorkbenchShell(), wizard);
	 * dialog.create(); SWTUtil.setDialogSize(dialog, 400, 500); int result = dialog.open(); if
	 * (result == WizardDialog.OK) { }
	 * 
	 * } // func run()
	 * 
	 * }); }
	 */
	/**
	 * Handle action: open selected Java file
	 */
	void handleHelp() {
		// final IHelp help = WorkbenchHelp.getHelpSupport();
		/*
		 * IHelpResource helpRes = new IHelpResource(){
		 * 
		 * public String getHref() { final StringBuffer helpURL = new StringBuffer(); // compose help
		 * URL helpURL.append("/com.patternbox.eclipse.editor/");
		 * helpURL.append(getDesignPatternInstance().getModelID()); helpURL.append(".html"); // return
		 * pattern specific URL return helpURL.toString(); }
		 * 
		 * public String getLabel() { return null; }
		 * 
		 * };
		 * 
		 * help.displayHelpResource(helpRes);
		 */
	}

	/**
	 * Handle model change
	 */
	@Override
	public void modelChanged(IModelChangedEvent event) {
		if (event.getChangeType() == IModelChangedEvent.WORLD_CHANGED) {
			fParticipantTree.refresh();
		}
	}

	/**
	 * Set focus to tree
	 */
	@Override
	public void setFocus() {
		if (fParticipantTree != null)
			fParticipantTree.getTree().setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.pde.core.IModelProviderListener#modelsChanged(org.eclipse.pde.core.IModelProviderEvent
	 * )
	 */
	@Override
	public void modelsChanged(IModelProviderEvent event) {
		// TODO Auto-generated method stub
	}
}