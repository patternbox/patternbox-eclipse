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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import com.patternbox.eclipse.codegen.CodeTemplateXmlHandler;
import com.patternbox.eclipse.codegen.MemberCodeGenerator;
import com.patternbox.eclipse.editor.DesignPatternPlugin;
import com.patternbox.eclipse.model.DesignPatternInstance;
import com.patternbox.eclipse.model.RoleNode;

/**
 * Wizard page for new partcipant members.
 * 
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class RoleMemberPage extends NewTypeWizardPage {

	// ------------------------------------------------------------------------- Field Definitions
	private static final String WIZARD_TITLE = "NewMemberWiz.titel";

	private static final String WIZARD_DESC = "NewMemberWiz.desc";

	private static final CodeTemplateXmlHandler sCodeTemplateXmlHandler = new CodeTemplateXmlHandler();

	private final static String PAGE_NAME = "NewClassWizardPage"; //$NON-NLS-1$

	private static IPackageFragmentRoot sPackageRoot = null;

	private static IPackageFragment sPackageFrag = null;

	private final RoleMemberWizard fWizard;

	private MemberCodeGenerator fCodeGenerator;

	/**
	 * Creates a new <code>NewClassWizardPage</code>
	 */
	public RoleMemberPage(RoleMemberWizard wizard) {
		super(true, PAGE_NAME);
		fWizard = wizard;
		final String roleName = fWizard.getRoleNode().getName();
		setTitle(DesignPatternPlugin.getFormattedMessage(WIZARD_TITLE, roleName));
		setDescription(DesignPatternPlugin.getResourceString(WIZARD_DESC));
	}

	// -------- Initialization ---------
	/**
	 * Sets the current source folder (model and text field) to the given package fragment root.
	 * 
	 * @param canBeModified
	 *          if <code>false</code> the source folder field can not be changed by the user. If
	 *          <code>true</code> the field is editable
	 */
	@Override
	public void setPackageFragmentRoot(IPackageFragmentRoot root, boolean canBeModified) {
		super.setPackageFragmentRoot(root, canBeModified);
		sPackageRoot = root;
	}

	/**
	 * The wizard owning this page is responsible for calling this method with the current selection.
	 * The selection is used to initialize the fields of the wizard page.
	 * 
	 * @param selection
	 *          used to initialize the fields
	 */
	public void init(IStructuredSelection selection) {
		super.setPackageFragmentRoot(sPackageRoot, true);
		setPackageFragment(sPackageFrag, true);
		final String roleName = fWizard.getRoleNode().getName();
		String className = roleName;
		IPackageFragment pack = getPackageFragment();
		if (pack != null) {
			ICompilationUnit cu;
			for (int i = 1; i < Integer.MAX_VALUE; i++) {
				className = roleName + ((i == 1) ? "" : String.valueOf(i));
				cu = pack.getCompilationUnit(className + ".java"); //$NON-NLS-1$
				if (!cu.getResource().exists())
					break;
			} // for
		} // if
		setTypeName(className, true);
	}

	// ------ validation --------
	private void doStatusUpdate() {
		// status of all used components
		IStatus[] status = new IStatus[] { fContainerStatus,
				isEnclosingTypeSelected() ? fEnclosingTypeStatus : fPackageStatus, fTypeNameStatus,
				fModifierStatus, fSuperClassStatus, fSuperInterfacesStatus };
		// the mode severe status will be displayed and the ok button enabled/disabled.
		updateStatus(status);
	}

	/*
	 * @see NewContainerWizardPage#handleFieldChanged
	 */
	@Override
	protected void handleFieldChanged(String fieldName) {
		super.handleFieldChanged(fieldName);
		doStatusUpdate();
	}

	// ------ ui --------
	/*
	 * @see WizardPage#createControl
	 */
	@Override
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		Composite composite = new Composite(parent, SWT.NONE);
		int nColumns = 4;
		GridLayout layout = new GridLayout();
		layout.numColumns = nColumns;
		composite.setLayout(layout);
		// pick & choose the wanted UI components
		createContainerControls(composite, nColumns);
		createPackageControls(composite, nColumns);
		createTypeNameControls(composite, nColumns);
		setControl(composite);
		Dialog.applyDialogFont(composite);
		// WorkbenchHelp.setHelp(composite, IJavaHelpContextIds.NEW_CLASS_WIZARD_PAGE);
	}

	/*
	 * @see WizardPage#becomesVisible
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			setFocus();
		}
	}

	// ---- creation ----------------
	/**
	 * Creates the new type using the entered field values.
	 * 
	 * @param monitor
	 *          a progress monitor to report progress.
	 */
	public void createType(DesignPatternInstance model, RoleNode roleNode, IProgressMonitor monitor)
			throws CoreException {
		// super.createType(monitor);
		fCodeGenerator = sCodeTemplateXmlHandler.createRoleMember(model, roleNode, monitor,
				getTypeName(), getPackageFragmentRoot(), getPackageFragment());
	}

	public MemberCodeGenerator getCodeGenerator() {
		return fCodeGenerator;
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	@Override
	public void dispose() {
		sPackageFrag = getPackageFragment();
		super.dispose();
	}
}
