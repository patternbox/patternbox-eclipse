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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import com.patternbox.eclipse.editor.DesignPatternPlugin;
import com.patternbox.eclipse.model.DesignPatternPool;
import com.patternbox.eclipse.model.PatternNode;

/**
 * The "New" wizard page allows setting the container for the new file as well as the file name. The
 * page will only accept file name without the extension OR with the extension that matches the
 * expected one (xdp).
 * 
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class NewDesignPatternPage extends WizardPage {

	// ------------------------------------------------------------------------- Field Definitions
	private static final String WIZARD_TITLE = "NewPatternWiz.titel";

	private static final String WIZARD_DESC = "NewPatternWiz.desc";

	private static final String BUTTON_BROWSE = "NewPatternWiz.button.browse";

	private static final String LABEL_CONTAINER = "NewPatternWiz.label.container";

	private static final String LABEL_TREE = "NewPatternWiz.label.patternTree";

	private static final String LABEL_FILENAME = "NewPatternWiz.label.filename";

	private static final String ERR_CONTAINER = "NewPatternWiz.error.container";

	private static final String ERR_PATTERN = "NewPatternWiz.error.patternNode";

	private static final String ERR_FILENAME = "NewPatternWiz.error.filename";

	private Text containerText;

	private Text fileText;

	private Tree fPatternTree;

	private final ISelection selection;

	private boolean fFileNameChanged;

	// ------------------------------------------------------------------------- Constructors
	/**
	 * Constructor for NewDesignPatternPage.
	 * 
	 * @param pageName
	 */
	public NewDesignPatternPage(ISelection selection) {
		super("wizardPage");
		setTitle(DesignPatternPlugin.getResourceString(WIZARD_TITLE));
		setDescription(DesignPatternPlugin.getResourceString(WIZARD_DESC));
		this.selection = selection;
	}

	// ------------------------------------------------------------------------- Private Methods
	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */
	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				containerText.setText(container.getFullPath().toString());
			} // if
		} // if
		LabelProvider labelProv = DesignPatternPlugin.getDefault().getImageProvider();
		new TreeModelWrapper(DesignPatternPool.getRootNode(), fPatternTree, labelProv);
		fFileNameChanged = false;
	}

	/**
	 * Uses the standard container selection dialog to choose the new value for the container field.
	 */
	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), ResourcesPlugin
				.getWorkspace().getRoot(), false, "Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				containerText.setText(((Path) result[0]).toOSString());
			}
		}
	}

	/**
	 * Sets the default file name
	 */
	private void setDefaultFileName(String container, PatternNode node) {
		// set file name
		fileText.setText(node.getModelID());
		// reset flag
		fFileNameChanged = false;
	}

	/**
	 * Ensures that required fields are set.
	 */
	private void dialogChanged() {
		final String container = getContainerName();
		final PatternNode node = getSelectedPatternNode();
		if (!fFileNameChanged && node != null) {
			setDefaultFileName(container, node);
		}
		if (container.length() == 0) {
			updateStatus(DesignPatternPlugin.getResourceString(ERR_CONTAINER));
			return;
		}
		if (node == null) {
			updateStatus(DesignPatternPlugin.getResourceString(ERR_PATTERN));
			return;
		}
		final String fileName = getFileName();
		if (fileName.length() == 0) {
			updateStatus(DesignPatternPlugin.getResourceString(ERR_FILENAME));
			return;
		}
		// if (!fileName.endsWith(BasePlugin.DEFAULT_FILE_EXT)) {
		// updateStatus("File extension must be '" + BasePlugin.DEFAULT_FILE_EXT + "'");
		// return;
		// }
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null && getSelectedPatternNode() != null);
	}

	// ------------------------------------------------------------------------- Public Methods
	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		// /////////////////////////////////////////////////////////
		Label label = new Label(container, SWT.NULL);
		label.setText(DesignPatternPlugin.getResourceString(LABEL_CONTAINER));
		containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerText.setLayoutData(gd);
		containerText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});
		Button button = new Button(container, SWT.PUSH);
		button.setText(DesignPatternPlugin.getResourceString(BUTTON_BROWSE));
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});
		// /////////////////////////////////////////////////////////
		label = new Label(container, SWT.NULL);
		label.setText(DesignPatternPlugin.getResourceString(LABEL_TREE));
		fPatternTree = new Tree(container, SWT.BORDER | SWT.H_SCROLL);
		gd = new GridData(GridData.FILL_VERTICAL | GridData.FILL_HORIZONTAL);
		fPatternTree.setLayoutData(gd);
		fPatternTree.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				dialogChanged();
			}
		});
		label = new Label(container, SWT.NULL);
		// /////////////////////////////////////////////////////////
		label = new Label(container, SWT.NULL);
		label.setText(DesignPatternPlugin.getResourceString(LABEL_FILENAME));
		fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fileText.setLayoutData(gd);
		fileText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				fFileNameChanged = true;
				dialogChanged();
			}
		});
		new Label(container, SWT.NULL).setText(DesignPatternPlugin.DEFAULT_FILE_EXT);
		// /////////////////////////////////////////////////////////
		initialize();
		// dialogChanged();
		setControl(container);
	}

	public String getContainerName() {
		return containerText.getText();
	}

	public PatternNode getSelectedPatternNode() {
		TreeItem[] selNode = fPatternTree.getSelection();
		// check selection
		if (selNode.length == 1 && selNode[0].getData() instanceof PatternNode) {
			return (PatternNode) selNode[0].getData();
		}
		return null;
	}

	public String getFileName() {
		return fileText.getText();
	}
}