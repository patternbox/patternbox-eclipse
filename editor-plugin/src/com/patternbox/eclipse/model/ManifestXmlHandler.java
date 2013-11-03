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
import java.text.MessageFormat;
import java.util.*;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import com.patternbox.commons.xml.*;
import com.patternbox.eclipse.editor.DesignPatternPlugin;

/**
 * XML-SAX-Handler for design pattern manifest parsing *  * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */

public class ManifestXmlHandler extends DefaultHandler {

   // ------------------------------------------------------------------------- Field Definitons

   private static final String ELEM_PATTERN = "pattern";
   private static final String ELEM_COMMENT = "comment";
   private static final String ELEM_LISTITEM = "li";
   private static final String ELEM_ROLE = "role";
   private static final String ELEM_DEPENDENCE = "dependence";

   private static final String ATTR_ID = "id";
   private static final String ATTR_NAME = "name";
   private static final String ATTR_TOP_CAT = "topCategorie";
   private static final String ATTR_SUB_CAT = "subCategorie";
   private static final String ATTR_AUTHOR_NAME = "authorName";
   private static final String ATTR_AUTHOR_WEBSITE = "authorWebsite";
   private static final String ATTR_MIN = "min";
   private static final String ATTR_MAX = "max";
   private static final String ATTR_ROLES = "roles";

   //private static final String AUTHOR_FORMAT = "@author <a href=\"mailto:{0}\">{1}</a>";
   private static final String AUTHOR_FORMAT = "@author {0}, <a href=\"http://{1}\">{1}</a>";

   private static SAXParser sXmlParser = null;

   private final StringBuffer fElementValue = new StringBuffer();
   private final LinkedList<RolePropertiesContainer> fRoleList = new LinkedList<RolePropertiesContainer>();
   private final HashMap<String, RolePropertiesContainer> fRoleMap = new HashMap<String, RolePropertiesContainer>();

   /**
    * 
    * @uml.property name="activeRoleProperties"
    * @uml.associationEnd multiplicity="(0 1)"
    */
   private RolePropertiesContainer fActiveRoleProperties;

   private boolean fInComment;
   //private boolean fInListItem;
   private int fNestingLevel;
   private String fModelID;
   private String fPatternName;
   private String fComment;
   private String fTopCategorie;
   private String fSubCategorie;
   private String fAuthorName;
   private String fAuthorWebsite;

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

   //------------------------------------------------------------------------- Private Methods

   /**
    * Normalize comment string
    * @param value Unnormalized comment
    * @return Normalized comment
    */
   private String formatComment(String value) {

      StringBuffer result = new StringBuffer();

      StringTokenizer tokenizer = new StringTokenizer(value.trim(), "\n\r\t\f");

      while (tokenizer.hasMoreTokens()) {
         // append comment fragment
         result.append(tokenizer.nextToken().trim());
         // append space character
         result.append(' ');
      }  // while

      return result.toString().trim();

   }

   //------------------------------------------------------------------------- Protected Methods

   protected LinkedList<RolePropertiesContainer> getRolePropertiesList() {
      return fRoleList;
   }

   protected RolePropertiesContainer getRoleProperties(String roleName) {
      return fRoleMap.get(roleName);
   }

   // ------------------------------------------------------------------------- XML Parser Handler Methods

   /**
    * @see org.xml.sax.ContentHandler#startDocument()
    */
   public void startDocument() throws SAXException {
      // remove old participants
      fRoleList.clear();
      // reset all instance fields
      fActiveRoleProperties = null;
      fInComment = false;
      //fInListItem = false;
      fNestingLevel = -1;
      fModelID = null;
      fPatternName = null;
      fTopCategorie = null;
      fSubCategorie = null;
   }

   /**
    * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
    */
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
		throws SAXException {

		// increment nesting level
		fNestingLevel++;
		// clear old element value
		fElementValue.setLength(0);
      // set in-comment-flag
      fInComment = fInComment && qName.equals(ELEM_COMMENT);
      // set in-list-item-flag
      //fInListItem = fInComment && qName.equals(ELEM_LISTITEM);

      if (qName.equals(ELEM_PATTERN) && fNestingLevel == 0) {

         // read pattern model identifier
         fModelID = SaxAttributeReader.getString(atts, ATTR_ID, null, true);
         // read design pattern name
         fPatternName = SaxAttributeReader.getString(atts, ATTR_NAME, null, true);
         // read top categorie
         fTopCategorie = SaxAttributeReader.getString(atts, ATTR_TOP_CAT, null, false);
         // read sub categorie
         fSubCategorie = SaxAttributeReader.getString(atts, ATTR_SUB_CAT, null, false);
         // read author attributes
			fAuthorName = SaxAttributeReader.getString(
					atts, ATTR_AUTHOR_NAME, DesignPatternPlugin.AUTHOR_NAME, false);
			fAuthorWebsite = SaxAttributeReader.getString(
					atts, ATTR_AUTHOR_WEBSITE, DesignPatternPlugin.AUTHOR_WEBSITE, false);

      } else if (qName.equals(ELEM_ROLE) && fNestingLevel == 1) {

         // create new role properties container
         fActiveRoleProperties = new RolePropertiesContainer();
         // read element attributes
         final int min = SaxAttributeReader.getInt(atts, ATTR_MIN, 1, false);
         final String sMax = SaxAttributeReader.getString(atts, ATTR_MAX, "n", false);
         // convert string as integer
         final int max = (sMax.equals("n")) ? Integer.MAX_VALUE : Integer.parseInt(sMax);
         // set role properties
         fActiveRoleProperties.setName(SaxAttributeReader.getString(atts, ATTR_NAME, null, true));
         fActiveRoleProperties.setMinOccurence(min);
         fActiveRoleProperties.setMaxOccurence(max);
         // store property container
         fRoleList.add(fActiveRoleProperties);
         fRoleMap.put(fActiveRoleProperties.getName(), fActiveRoleProperties);

      } else if (qName.equals(ELEM_DEPENDENCE) && fNestingLevel == 2) {

         fActiveRoleProperties.setDependence(
            SaxAttributeReader.getString(atts, ATTR_ROLES, "", false));

      }  // if - else

   }

	/**
	 * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void endElement(String namespaceURI, String localName, String qName) throws SAXException {

      if (qName.equals(ELEM_COMMENT) && fNestingLevel == 1) {

         // copy assoziated comment
         fComment = formatComment(fElementValue.toString());

      } else if (qName.equals(ELEM_LISTITEM)) {

         fActiveRoleProperties.addCommentItem(fElementValue.toString());

      } else if (qName.equals(ELEM_ROLE) && fNestingLevel == 1) {
         
         // not handled

      }  // if - else

      // reset in-comment-flag
      fInComment = !(fInComment && qName.equals(ELEM_COMMENT));
      // decrement nesting level
      fNestingLevel--;
	}

   /**
    * @see org.xml.sax.ContentHandler#characters(char[], int, int)
    */
   public void characters(char[] ch, int start, int length) throws SAXException {
      // append this chunck to current element value
      fElementValue.append (ch, start, length);
   }

   // ------------------------------------------------------------------------- Public Methods

	public void parseManifestFile(File manifestFile) throws IOException, SAXException {

		// declare variables
		FileInputStream inStream;

		// create new XML reader for parsing the manifest file
		XMLReader reader = sXmlParser.getXMLReader();
		// set manifest handler as content and error handler
		reader.setContentHandler(this);
		reader.setErrorHandler(this);

		// open manifest file as input stream
		inStream = new FileInputStream(manifestFile);

      InputSource inpSrc = new InputSource(inStream);
      inpSrc.setSystemId(XmlDocValidator.getSystemURI(manifestFile));

		// parse manifest file
		reader.parse(inpSrc);

      // close input stream
      inStream.close();
	}

   /**
    * This method returns the model identifier
    * @return String model identifier
    * 
    * @uml.property name="modelID"
    */
   public String getModelID() {
      return fModelID;
   }

   /**
    * 
    * @uml.property name="patternName"
    */
   public String getPatternName() {
      return fPatternName;
   }

   /**
    * 
    * @uml.property name="comment"
    */
   public String getComment() {
      return fComment;
   }

   /**
    * 
    * @uml.property name="topCategorie"
    */
   public String getTopCategorie() {
      return fTopCategorie;
   }

   /**
    * 
    * @uml.property name="subCategorie"
    */
   public String getSubCategorie() {
      return fSubCategorie;
   }

   public String getAuthor() {
      return MessageFormat.format(AUTHOR_FORMAT, new Object[] {fAuthorName, fAuthorWebsite});
  }
   
   public Set<String> getRoles() {
      return fRoleMap.keySet();
   }

}
