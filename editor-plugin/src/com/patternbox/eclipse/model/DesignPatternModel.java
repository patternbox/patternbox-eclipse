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

import java.io.*;
import java.util.*;

import org.xml.sax.*;

import com.patternbox.commons.xml.XmlWriter;
import com.patternbox.commons.model.*;

/**
 * Implementation of a design pattern model. *  * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */

public class DesignPatternModel implements ITreeModel {

   /**
    * 
    * @uml.property name="manifestXmlHandler"
    * @uml.associationEnd multiplicity="(0 1)"
    */
   // ------------------------------------------------------------------------- Field Definitons
   private final ManifestXmlHandler fManifestXmlHandler = new ManifestXmlHandler();

   /**
    * 
    * @uml.property name="root"
    * @uml.associationEnd multiplicity="(0 1)"
    */
   private final DocumentNode fRoot = new DocumentNode(null);

   // ------------------------------------------------------------------------- Constructors

	/**
	 * Constructor
    * @param manifestFile Manifest file of a design pattern
	 */
	public DesignPatternModel(File manifestFile) {

		try {
 			// parse manifest file
         fManifestXmlHandler.parseManifestFile(manifestFile);

		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

   /**
    * Constructor
    * @param patternNode Tree node of a design pattern used in the design pattern wizard
    * @see com.patternbox.eclipse.wizard.NewDesignPatternWizard
    */
   public DesignPatternModel(PatternNode patternNode) {
      // extract manifest file and call constructor
      this(patternNode.getManifestFile());
   }

   // ------------------------------------------------------------------------- Public Methods

   /**
    *
    * @return File content of the new design pattern instance
    */
   public String getNewInstanceContent() {

      Map<String, String> atts = new HashMap<String, String>();

      XmlWriter xml = new XmlWriter();

      atts.put(PatternXmlHandler.ATTR_ID, getModelID());
      xml.openElement(PatternXmlHandler.ELEM_PATTERN, atts, null);

      RolePropertiesContainer props;
      Iterator<RolePropertiesContainer> it = fManifestXmlHandler.getRolePropertiesList().iterator();

      while (it.hasNext()) {
         props = it.next();
         atts.clear();
         atts.put(PatternXmlHandler.ATTR_NAME, props.getName());
         xml.addEmptyElement(PatternXmlHandler.ELEM_ROLE, atts);
      }  // while

      xml.closeElement();

      return xml.toString();
   }

	/**
	 * @see com.patternbox.commons.model.ITreeModel#getRootNode()
	 */
	public DocumentNode getRootNode() {
		return fRoot;
	}

   /**
    * This method returns the model identifier
    * @return String model identifier
    */
   public String getModelID() {
      return fManifestXmlHandler.getModelID();
   }

   /**
    * This method returns the model comment
    * @return Model comment
    */
   public String getComment() {
      return fManifestXmlHandler.getComment();
   }

   public RolePropertiesContainer getRoleProperties(String roleName) {
      return fManifestXmlHandler.getRoleProperties(roleName);
   }

   public String getAuthor() {
      return fManifestXmlHandler.getAuthor();
   }
   
   public Set<String> getRoles() {
      return fManifestXmlHandler.getRoles();
   }

}
