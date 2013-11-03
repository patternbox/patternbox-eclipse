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

import java.util.HashMap;

import com.patternbox.commons.model.DocumentNode;

/**
 * Categorie tree node implementation.
 *
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class CategorieNode extends DocumentNode {

   // ------------------------------------------------------------------------- Field Definitions

   private final String fName;
   private final HashMap<String, CategorieNode> fChildMap = new HashMap<String, CategorieNode>();

   // ------------------------------------------------------------------------- Constructors

	/**
    * Default constructor
	 * @param parent Parent node of this instance
    * @param name Categorie name
	 */
	public CategorieNode(DocumentNode parent, String name) {
		super(parent);
      fName = name;

      if (parent instanceof CategorieNode) {
         ((CategorieNode) parent).fChildMap.put(fName, this);
      }  // if
	}

   // ------------------------------------------------------------------------- Public Methods

   public CategorieNode findChild(String name) {
      return fChildMap.get(name);
   }

   /**
    * Returns categorie name
    * @return String Categorie name
    * 
    * @uml.property name="name"
    */
   public String getName() {
      return fName;
   }

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return getName();
	}

}
