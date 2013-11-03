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

package com.patternbox.commons.xml;

import java.io.*;

import javax.xml.parsers.*;

import org.xml.sax.*;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class checks XML document validity
 *
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class XmlDocValidator {

   private static final XmlDocValidator fInstance = new XmlDocValidator();

   private XmlDocValidator() {
      super();
   }

   public static XmlDocValidator getInstance() {
      return fInstance;
   }

   public static String getSystemURI(File xmlFile) {
      final String filename = xmlFile.getAbsolutePath().replace('\\', '/');
      return (filename.charAt(0) == '/') ? "file://" + filename : "file:///" + filename;
   }

   public void validate(File xmlFile) throws EInvalidXmlDocument {
      try {
         StringBuffer errorBuff = new StringBuffer();
         InputSource input = new InputSource(new FileInputStream(xmlFile));
         // Set systemID so parser can find the dtd with a relative URL in the source document.
         input.setSystemId(getSystemURI(xmlFile));
         SAXParserFactory spfact = SAXParserFactory.newInstance();

         spfact.setValidating(true);
         spfact.setNamespaceAware(true);

         SAXParser parser = spfact.newSAXParser();
         XMLReader reader = parser.getXMLReader();

         //Instantiate inner-class error and lexical handler.
         Handler handler = new Handler(xmlFile.toString(), errorBuff);
         reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
         parser.parse(input, handler);

         if (!handler.containsDTD || handler.errorOrWarning) {
            throw new EInvalidXmlDocument(xmlFile, errorBuff.toString());
         }

      } catch (Exception e) {
         throw new EInvalidXmlDocument(xmlFile, e.getMessage());
      }
   }

	// Catch any errors or warnings, and verify presence of doctype statement.
	class Handler extends DefaultHandler implements LexicalHandler {
		boolean errorOrWarning;
		boolean containsDTD;
		String sourceFile;
		StringBuffer errorBuff;

		Handler(String sourceFile, StringBuffer errorBuff) {
			super();
			this.sourceFile = sourceFile;
			this.errorBuff = errorBuff;
			errorOrWarning = false;
			containsDTD = false;
		}

		public void error(SAXParseException exc) {
			errorBuff.append(sourceFile + " Error: " + exc.getMessage() + "\n");
			errorOrWarning = true;
		}
		public void warning(SAXParseException exc) {
			errorBuff.append(sourceFile + " Warning:" + exc.getMessage() + "\n");
			errorOrWarning = true;
		}

		// LexicalHandler methods; all no-op except startDTD().

		// Set containsDTD to true when startDTD event occurs.
		public void startDTD(String name, String publicId, String systemId) throws SAXException {
			containsDTD = true;
		}

		public void endDTD() throws SAXException {
		   // we do not handle this callback
		}

		public void startEntity(String name) throws SAXException {
		   // we do not handle this callback
		}

		public void endEntity(String name) throws SAXException {
		   // we do not handle this callback
		}

		public void startCDATA() throws SAXException {
		   // we do not handle this callback
		}

		public void endCDATA() throws SAXException {
		   // we do not handle this callback
		}

		public void comment(char ch[], int start, int length) throws SAXException {
		   // we do not handle this callback
		}
	}
}