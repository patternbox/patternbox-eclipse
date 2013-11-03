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

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import com.patternbox.commons.xml.SaxAttributeReader;
import com.patternbox.commons.model.DocumentNode;

/**
 * XML-SAX-Handler for design pattern template parsing *  * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */

public class PatternXmlHandler extends DefaultHandler {

   // ------------------------------------------------------------------------- Field Definitons

   public static final String ELEM_PATTERN = "pattern";
   public static final String ELEM_ROLE = "role";
   public static final String ELEM_MEMBER = "member";

   public static final String ATTR_ID = "id";
   public static final String ATTR_NAME = "name";
   public static final String ATTR_REPLACE = "replace";
   public static final String ATTR_PROJECT = "project";
   public static final String ATTR_PACKAGE = "package";

   private static SAXParser sXmlParser = null;

   /**
    * 
    * @uml.property name="patternModel"
    * @uml.associationEnd multiplicity="(0 1)"
    */
   private DesignPatternModel fPatternModel;

   /**
    * 
    * @uml.property name="rootNode"
    * @uml.associationEnd multiplicity="(0 1)"
    */
   private DocumentNode fRootNode;

   /**
    * 
    * @uml.property name="activeRoleNode"
    * @uml.associationEnd multiplicity="(0 1)"
    */
   private RoleNode fActiveRoleNode;


   private int fNestingLevel;
   private String fDefaultProject;
   private String fModelID;

   // ------------------------------------------------------------------------- Constructors

   static {
      SAXParserFactory factory = SAXParserFactory.newInstance();
      try {
         sXmlParser = factory.newSAXParser();
      } catch (Exception e) {
         // SAX parser can't be used
         sXmlParser = null;
      }
   }

   // ------------------------------------------------------------------------- XML Parser Handler Methods

   /**
    * @see org.xml.sax.ContentHandler#startDocument()
    */
   public void startDocument() throws SAXException {
      fNestingLevel = -1;
      fModelID = null;
   }

   /**
    * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
    */
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
		throws SAXException {

		// increment nesting level
		fNestingLevel++;

      String project, name, pack, replace;

      if (qName.equals(ELEM_PATTERN) && fNestingLevel == 0) {

         // read pattern model identifier
         fModelID = SaxAttributeReader.getString(atts, ATTR_ID, null, true);
         // get assoziated design pattern model
         fPatternModel = DesignPatternPool.getDesignPatternModel(fModelID);

      } else if (qName.equals(ELEM_ROLE) && fNestingLevel == 1) {

         // get participant name
         name = SaxAttributeReader.getString(atts, ATTR_NAME, null, true);
         // get automatic code replacement
         replace = SaxAttributeReader.getString(atts, ATTR_REPLACE, null, false);
         // create new role node
         fActiveRoleNode = new RoleNode(fRootNode, fPatternModel.getRoleProperties(name), replace);

      } else if (qName.equals(ELEM_MEMBER) && fNestingLevel == 2) {

         // get project name
         project = SaxAttributeReader.getString(atts, ATTR_PROJECT, fDefaultProject, false);
         // get participant name
         name = SaxAttributeReader.getString(atts, ATTR_NAME, null, true);
         // get package name
         pack = SaxAttributeReader.getString(atts, ATTR_PACKAGE, "", false);
         // create new role node
         new MemberNode(fActiveRoleNode, project, pack, name);

      }

   }

	/**
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

      // decrement nesting level
      fNestingLevel--;
	}

   // ------------------------------------------------------------------------- Public Methods

   /**
    * This method parses the incoming stream and creates an instance of a design
    * pattern model of it. The root node the model will be returned.
    * @param patternStream Stream of a XML document
    * @param defaultProject Name of the default project
    * @return Root node of the design pattern model
    */
	public DocumentNode parsePatternStream(InputStream patternStream, String defaultProject)
		throws IOException, SAXException {

      fDefaultProject = defaultProject;
      fRootNode = new DocumentNode(null);

		// create new XML reader for parsing the manifest file
		XMLReader reader = sXmlParser.getXMLReader();
		// set manifest handler as content and error handler
		reader.setContentHandler(this);
		reader.setErrorHandler(this);

		// parse manifest file
		reader.parse(new InputSource(patternStream));

      return fRootNode;
	}

   /**
    * @return Identifier of the underlying design pattern
    * 
    * @uml.property name="modelID"
    */
   public String getModelID() {
      return fModelID;
   }

}
