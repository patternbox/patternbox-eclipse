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
package com.patternbox.eclipse.model;

import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.patternbox.commons.model.DocumentNode;

/**
 * Proxy of an implementation of a design pattern participant.
 * 
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class MemberNode extends DocumentNode {

	// ------------------------------------------------------------------------- Field Definitons
	private final IType fType;

	private final String fName;

	private final String fProject;

	private final String fFullName;

	// ------------------------------------------------------------------------- Constructors
	/**
	 * Constructor
	 * 
	 * @param parent
	 *          Parent node
	 * @param type
	 *          IType reference of the member
	 */
	public MemberNode(RoleNode parent, IType type) {
		super(parent);
		fType = type;
		fName = fType.getElementName();
		fFullName = fType.getFullyQualifiedName();
		fProject = fType.getJavaProject().getPath().toFile().getName();
	}

	/**
	 * Constructor
	 * 
	 * @param parent
	 *          Parent node
	 * @param pack
	 *          Name of the package
	 * @param name
	 *          Member name
	 */
	public MemberNode(RoleNode parent, String projectName, String pack, String name) {
		super(parent);
		fType = getMemberType(projectName, pack, name);
		fName = name;
		fProject = projectName;
		StringBuffer buf = new StringBuffer();
		// check package availability
		if (pack != null && pack.length() > 0) {
			buf.append(pack);
			buf.append('.');
		} // if
		buf.append(name);
		if (fType == null) {
			buf.append(" (not found)");
		}
		fFullName = buf.toString();
	}

	// ------------------------------------------------------------------------- Private Methods
	private static IType getMemberType(String projectName, String pack, String name) {
		// IWorkspaceRoot root = JavaPlugin.getWorkspace().getRoot();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IJavaModel model = JavaCore.create(root);
		IJavaProject project = model.getJavaProject(projectName);
		IType member = null;
		try {
			member = project.findType(pack, name);
		} catch (JavaModelException e1) {
			// return uninitialized member !!!
			// e1.printStackTrace();
		}
		return member;
	}

	// ------------------------------------------------------------------------- Public Methods
	/**
	 * Removes this instance from its parent node.
	 */
	public void delete() {
		((RoleNode) getParent()).removeChild(this);
	}

	public IType getMemberType() {
		return fType;
	}

	/**
	 * 
	 * @uml.property name="name"
	 */
	public String getName() {
		return fName;
	}

	public String getPackage() {
		if (getName().equals(fFullName)) {
			return "";
		}
		return fFullName.substring(0, fFullName.length() - fName.length() - 1);
	}

	public String getProjectName() {
		return fProject;
	}

	public boolean isResourceAvail() {
		return fType != null;
	}

	/**
	 * Name of the class
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return fFullName;
	}
}
