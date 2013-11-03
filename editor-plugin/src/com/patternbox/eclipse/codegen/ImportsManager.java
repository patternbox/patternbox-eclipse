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

package com.patternbox.eclipse.codegen;

import java.util.HashSet;
import java.util.Set;

/**
 * Organize type imports.
 * 
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class ImportsManager implements IImportsManager {
	
	private final Set<String> fIgnores = new HashSet<String>();
	
	private final Set<String> fImports = new HashSet<String>();
	
	/**
    * Constructor
	 */
	public ImportsManager() {
		fIgnores.add("void");
		fIgnores.add("int");
		fIgnores.add("long");
		fIgnores.add("float");
		fIgnores.add("byte");
	}
	
	/**
    * Constructor
    * 
	 * @param imports Predefined imports
	 */
	public ImportsManager(Set<String> imports) {
		this();
		fImports.addAll(imports);
	}

   /**
    * Adds a new import declaration that is sorted in the existing imports.
    * If an import already exists or the import would conflict with another import
    * of an other type with the same simple name  the import is not added.
    * 
    * @param qualifiedTypeName The fully qualified name of the type to import
    * (dot separated)
    * @return Returns the simple type name that can be used in the code or the
    * fully qualified type name if an import conflict prevented the import
    */
	public String addImport(String qualifiedTypeName) {
		
		if (!fIgnores.contains(qualifiedTypeName)) {
			
			fImports.add(qualifiedTypeName);
			
			if (qualifiedTypeName.contains(".")) {			
				return qualifiedTypeName.substring(qualifiedTypeName.lastIndexOf('.')+1);
			}			
		}
		
		return qualifiedTypeName;
	}
	
	/* (non-Javadoc)
	 * @see com.patternbox.eclipse.codegen.IImportsManager#getImports()
	 */
	public Set<String> getImports() {
		return fImports;
	}

}
