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

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;

import com.patternbox.commons.model.DocumentNode;
import com.patternbox.commons.xml.EInvalidXmlDocument;
import com.patternbox.commons.xml.XmlDocValidator;
import com.patternbox.eclipse.editor.DesignPatternPlugin;

/**
 * Pool of all available design pattern models *  * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */

public class DesignPatternPool {

   /**
    * 
    * @uml.property name="rootNode"
    * @uml.associationEnd multiplicity="(0 1)"
    */
   // ------------------------------------------------------------------------- Field Definitions
   private static final CategorieNode sRootNode = new CategorieNode(null, "rootNode"); //$NON-NLS-1$

   private static final HashMap<String, PatternNode> sPatternNodeMap = new HashMap<String, PatternNode>();
   private static final HashMap<PatternNode, DesignPatternModel> sDesignPatternMap = new HashMap<PatternNode, DesignPatternModel>();

   /**
    * 
    * @uml.property name="manifestXmlHandler"
    * @uml.associationEnd multiplicity="(0 1)"
    */
   private static final ManifestXmlHandler sManifestXmlHandler = new ManifestXmlHandler();


   // ------------------------------------------------------------------------- Constructors

   // ------------------------------------------------------------------------- Private Methods

   private static CategorieNode findCategorie(String topCatName, String subCatName) {

      if (topCatName == null) return sRootNode;

      CategorieNode topCat = sRootNode.findChild(topCatName);
      if (topCat == null) {
         topCat = new CategorieNode(sRootNode, topCatName);
      }  // if

      if (subCatName == null || subCatName.equals("")) {
         return topCat;
      }  // if

      CategorieNode subCat = topCat.findChild(subCatName);
      if (subCat == null) {
         subCat = new CategorieNode(topCat, subCatName);
      }  // if

      return subCat;

   }

   /**
    * Creates a new design pattern node after manifest file parsing.
    * @param manifestFile Assoziates manifest file
    */
   private static void createPatternNode(File manifestFile) {

      PatternNode node = new PatternNode(manifestFile, sManifestXmlHandler);
      // get categorie names
      final String topCat = sManifestXmlHandler.getTopCategorie();
      final String subCat = sManifestXmlHandler.getSubCategorie();
      // create a categorie node based on the categorie names
      CategorieNode catNode = findCategorie(topCat, subCat);

      // set categorie node as pattern node
      node.setParent(catNode);

      // store design pattern node for access via model identifier
      sPatternNodeMap.put(sManifestXmlHandler.getModelID(), node);

   }

   // ------------------------------------------------------------------------- Protected Methods

   /**
    * Translates the design pattern identifier into its original name
    * @param modelID Design pattern identifier
    * @return Name of the design pattern
    */
   protected static String getDesignPatternName(String modelID) {
      PatternNode patternNode = sPatternNodeMap.get(modelID);
      return patternNode != null ? patternNode.toString() : null;
   }

   // ------------------------------------------------------------------------- Public Methods

   /**
    * Initialize design pattern pool
    * @param installPath Plugin base path
    */
	public static void initialize(String installPath) {

		String pathSuffix = "pattern.mf/"; //$NON-NLS-1$

		File manifestPath = new File(installPath + pathSuffix);

		if (manifestPath != null && manifestPath.isDirectory()) {

         try {
				// get all defined design pattern manifests
				File[] manifestFiles = manifestPath.listFiles(new FilenameFilter() {

					public boolean accept(File dir, String name) {
						return name.endsWith(".xml");
					}

				});

				//synchronized (sManifestXmlHandler) {

					// for each design pattern manifest create a pattern node
					for (int i = 0; i < manifestFiles.length; i++) {

                  // validate code template before applying
                  try {
							XmlDocValidator.getInstance().validate(manifestFiles[i]);
						} catch (EInvalidXmlDocument e) {
                     DesignPatternPlugin.logException(e);
                     continue;
						}

						// parse manifest file
						sManifestXmlHandler.parseManifestFile(manifestFiles[i]);
						// create design pattern node from manifest
						createPatternNode(manifestFiles[i]);
					} // for

				//} // synchronized

         } catch (Exception e) {
            DesignPatternPlugin.logException(e);
			}  // try - catch

		} // if

   }

   /**
    * 
    * @uml.property name="rootNode"
    */
   public static DocumentNode getRootNode() {
      return sRootNode;
   }

   public static DesignPatternModel getDesignPatternModel(String modelID) {
      PatternNode patternNode = sPatternNodeMap.get(modelID);
      return patternNode != null ? getDesignPatternModel(patternNode) : null;
   }

   public static DesignPatternModel getDesignPatternModel(PatternNode patternNode) {

      // look up for the requested design pattern model
      DesignPatternModel result = sDesignPatternMap.get(patternNode);
      // check model availability
      if (result == null) {
         // create new design pattern model and store them into the map
         result = new DesignPatternModel(patternNode);
         sDesignPatternMap.put(patternNode, result);
      }  // if

      return result;
   }

   /**
    * Before you call this method you have to initialize this class. Otherwise the
    * pattern hierarchy will be empty.
    * @return Root handle of all registered design pattern
    * @see DesignPatternPool#initialize(String)
    */
   public static CategorieNode getRegisteredPattern() {
      return sRootNode;
   }

}
