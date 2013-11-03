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
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.pde.core.IEditable;
import org.eclipse.pde.internal.core.AbstractModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.xml.sax.SAXException;

import com.patternbox.commons.model.DocumentNode;
import com.patternbox.commons.xml.XmlWriter;
import com.patternbox.eclipse.editor.DesignPatternPlugin;

/**
 * This class represents an instance of a particular design pattern *  * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */

public class DesignPatternInstance extends AbstractModel implements IEditable {

	private static final long serialVersionUID = 3257850961060706614L;

   /**
    * 
    * @uml.property name="patternXmlHandler"
    * @uml.associationEnd multiplicity="(0 1)"
    */
   private transient static PatternXmlHandler sPatternXmlHandler = new PatternXmlHandler();

   /**
    * 
    * @uml.property name="root"
    * @uml.associationEnd multiplicity="(0 1)"
    */
   private DocumentNode fRoot = new DocumentNode(null);

   private String fModelID;
   private String fDesignPatternName;

	private boolean fEnabled;
   private boolean fIsDirty;
   private IFile fInputFile; 

   /**
    * Constructor
    * @param input Model input
    * @throws CoreException
    */
	public DesignPatternInstance(IEditorInput input) throws CoreException {

      // call inherited constructor
		super();

		if (input instanceof FileEditorInput) {
			createModel((FileEditorInput) input);
			fInputFile = ((FileEditorInput) input).getFile(); 
		}

		// get role nodes set
		DocumentNode[] roleNodes = fRoot.getChildren();
		// set role nodes design pattern instance
		for (int i = 0; i < roleNodes.length; i++) {
         ((RoleNode) roleNodes[i]).setDesignPatternInstance(this);
		} // for
	}

	private void createModel(FileEditorInput input) throws CoreException {

      InputStream stream = input.getFile().getContents(false);

		load(stream, false, input.getFile().getProject().getName());

		try {
			stream.close();
		} catch (IOException e) {
			DesignPatternPlugin.logException(e);
		}
	}

   public boolean isEnabled() {
      return fEnabled;
   }

   /**
    * Returns model root.
    * @return Model root node
    * 
    * @uml.property name="root"
    */
   public DocumentNode getRoot() {
      return fRoot;
   }


   /**
    * Returns the particular role node of a design pattern
    * @param roleName Name of the requested role node
    * @return Role node instance
    */
   public RoleNode getRole(String roleName) {

      // declare result variable and set default value
      RoleNode result = null;
      DocumentNode[] children = fRoot.getChildren();

      // for each role node check name and compare it with the given name
      for (int i=0; i<children.length; i++) {
         if (((RoleNode) children[i]).getName().equals(roleName)) {
            result = (RoleNode) children[i];
         }  // if
      }  // for

      return result;
   }

   public MemberNode getMember(String roleName, int index) {
      // get members of the given role node
      DocumentNode[] children = getRole(roleName).getChildren();
      // return requested node
      return (children != null && children.length > index) ? (MemberNode) children[index] : null;
   }

   public synchronized void load(InputStream stream, boolean outOfSync) throws CoreException {
      load(stream, outOfSync, "");
   }

	public synchronized void load(InputStream stream, boolean outOfSync, String project)
		throws CoreException {

	  setLoaded(false);
      try {
         // parse input stream and assign root node
         fRoot = sPatternXmlHandler.parsePatternStream(stream, project);
         // save design pattern model identifier
         fModelID = sPatternXmlHandler.getModelID();
         // save design pattern name
         fDesignPatternName = DesignPatternPool.getDesignPatternName(fModelID);
         // set loaded flag
		 setLoaded(true);
         // update time stamp if stream is out of sync
         if (!outOfSync) updateTimeStamp();

      } catch (SAXException e) {
         throwParseErrorsException(e);
      } catch (IOException e) {
         throwParseErrorsException(e);
      }

   }

   public void reload(InputStream stream, boolean outOfSync) throws CoreException {
      load(stream, outOfSync);
   }

   /**
    * 
    * @uml.property name="enabled"
    */
   public void setEnabled(boolean newEnabled) {
      fEnabled = newEnabled;
   }

   
   public String getXmlString() {
      
      // create new XML writer
      XmlWriter xml = new XmlWriter();

      // create attributes container
      Map<String, String> atts = new TreeMap<String, String>();

      // write root element
      atts.put(PatternXmlHandler.ATTR_ID, fModelID);
      xml.openElement(PatternXmlHandler.ELEM_PATTERN, atts, null);

      // --- BEGIN: write participant nodes -------------------------------

      // declare variables for later usage
      RoleNode role;
      MemberNode member;
      DocumentNode[] memberList;

      // get all participants
      DocumentNode[] roleList = fRoot.getChildren();

      // write each participant as XML element
      for (int i=0; i<roleList.length; i++) {

         // cast document node as role node for further proceedings
         role = (RoleNode) roleList[i];

         // save role name
         atts.clear();
         atts.put(PatternXmlHandler.ATTR_NAME, role.getName());
         // append automatic replace list
         if (role.getReplaceNodesAsString() != null) {
            atts.put(PatternXmlHandler.ATTR_REPLACE, role.getReplaceNodesAsString());
         }  // if

         // write current role
         xml.openElement(PatternXmlHandler.ELEM_ROLE, atts, null);

         // --- BEGIN: write member nodes -------------------------------

         // get member access
         memberList = role.getChildren();

         // write each participant as XML element
         for (int j=0; j<memberList.length; j++) {

            // cast document node as role node for further proceedings
            member = (MemberNode) memberList[j];
            final String project = member.getProjectName();
            // save role name
            atts.clear();

            if (project != null & project.length() > 0)
               atts.put(PatternXmlHandler.ATTR_PROJECT, project);

            atts.put(PatternXmlHandler.ATTR_PACKAGE, member.getPackage());
            atts.put(PatternXmlHandler.ATTR_NAME, member.getName());
            // write current member
            xml.addEmptyElement(PatternXmlHandler.ELEM_MEMBER, atts);

         } // for

         // --- END: write member nodes -------------------------------

         // close element
         xml.closeElement();

      }  // for

      // --- END: write participant nodes -------------------------------

      // close root element
      xml.closeElement();

      // return XML content
      return xml.toString();
   }

	/**
	 * @see org.eclipse.pde.internal.core.AbstractModel#updateTimeStamp()
	 */
	protected void updateTimeStamp() {
		// ToDo Auto-generated method stub
	}

	/**
	 * @see org.eclipse.pde.internal.core.AbstractModel#isEditable()
	 */
	public boolean isEditable() {
		return true;
	}

	/**
	 * @see org.eclipse.pde.internal.core.AbstractModel#isInSync()
	 */
	public boolean isInSync() {
		// ToDo Auto-generated method stub
		return false;
	}

   /**
    * @see org.eclipse.pde.core.IEditable#isDirty()
    * 
    * @uml.property name="isDirty"
    */
   public boolean isDirty() {
      return fIsDirty;
   }

   /**
    * @see org.eclipse.pde.core.IEditable#setDirty(boolean)
    * 
    * @uml.property name="isDirty"
    */
   public void setDirty(boolean dirty) {
      fIsDirty = dirty;
   }


   /**
    * @see org.eclipse.pde.internal.core.AbstractModel#load()
    */
   public void load() throws CoreException {
      // not supported
   }

	/**
	 * @see org.eclipse.pde.core.IEditable#save(java.io.PrintWriter)
	 */
	public void save(PrintWriter writer) {
      // output XML content
      writer.print(getXmlString());
      // reset dirty flag
      setDirty(false);
	}

   /**
    * @return Identifier the used design pattern
    * 
    * @uml.property name="modelID"
    */
   public String getModelID() {
      return fModelID;
   }

   /**
    * @return Original name of the underlying design patten
    * 
    * @uml.property name="designPatternName"
    */
   public String getDesignPatternName() {
      return fDesignPatternName;
   }


   public File getCodeTemplateFile() {
      String pluginPath = DesignPatternPlugin.getPluginPath();
      return new File(pluginPath + "/template.java/" + fModelID + ".xml");
   }

   public DesignPatternModel getDesignPatternModel() {
      return DesignPatternPool.getDesignPatternModel(fModelID);
   }

   /**
    * @return Underlying file input
    * 
    * @uml.property name="inputFile"
    */
   public IFile getInputFile() {
      return fInputFile;
   }

   /**
    * @see org.eclipse.pde.core.IModel#isReconcilingModel()
    */
   public boolean isReconcilingModel() {
      // TODO Auto-generated method stub
      return false;
   }

}