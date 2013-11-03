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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.wizards.NewElementWizard;
import org.eclipse.jface.wizard.Wizard;

import com.patternbox.eclipse.codegen.MemberCodeGenerator;
import com.patternbox.eclipse.model.DesignPatternInstance;
import com.patternbox.eclipse.model.RoleNode;

/**
 * Wizard for creating new participant members.
 * 
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class RoleMemberWizard extends NewElementWizard {

	// ------------------------------------------------------------------------- Field Definitions
	//private static final String WIZARD_WIN_TITLE = "NewMemberWiz.wintitel"; //$NON-NLS-1$
	private final DesignPatternInstance fModel;

	private final RoleNode fRoleNode;

	private RoleMemberPage fPage;

	public RoleMemberWizard(DesignPatternInstance model, RoleNode roleNode) {
		super();
		fModel = model;
		fRoleNode = roleNode;
		setDefaultPageImageDescriptor(JavaPluginImages.DESC_WIZBAN_NEWCLASS);
		setDialogSettings(JavaPlugin.getDefault().getDialogSettings());
		// setWindowTitle(DesignPatternPlugin.getResourceString(WIZARD_WIN_TITLE));
		setWindowTitle("Design Pattern");
	}

	/**
	 * @see Wizard#createPages
	 */
	@Override
	public void addPages() {
		super.addPages();
		fPage = new RoleMemberPage(this);
		addPage(fPage);
		fPage.init(getSelection());
	}

	/**
	 * @see org.eclipse.jdt.internal.ui.wizards.NewElementWizard#finishPage(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException {
		// use the full progress monitor
		fPage.createType(fModel, fRoleNode, monitor);
		// get compilation unit of created Java type
		// ICompilationUnit cu =
		// JavaModelUtil.toOriginal(getCodeGenerator().getCreatedType().getCompilationUnit()); V3.2
		ICompilationUnit cu = ((IMember) getCodeGenerator().getCreatedType().getPrimaryElement())
				.getCompilationUnit();
		if (cu != null) {
			IResource resource = cu.getResource();
			selectAndReveal(resource);
			openResource((IFile) resource);
		}
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		warnAboutTypeCommentDeprecation();
		return super.performFinish();
	}

	public MemberCodeGenerator getCodeGenerator() {
		return fPage.getCodeGenerator();
	}

	/**
	 * @return
	 */
	public RoleNode getRoleNode() {
		return fRoleNode;
	}

	@Override
	public IJavaElement getCreatedElement() {
		// TODO Auto-generated method stub
		return null;
	}
}
