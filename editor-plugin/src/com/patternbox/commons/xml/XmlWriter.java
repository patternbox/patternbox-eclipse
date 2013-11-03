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

import java.text.MessageFormat;
import java.util.*;

/**
 * This class creates well-formed XML documents.
 *
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class XmlWriter {

   private static final char EOL = '\n';
   private static final String SPACES = "                                                     ";
   private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"iso-8859-1\" ?>" + EOL;
   private static final String SYSTEM_DTD = "<!DOCTYPE {0} SYSTEM \"{1}\">" + EOL;
   private static final char[] SPECIAL_CHARS = {'<', '>', '&', '\'', '"'};
   private static final String[] ENTITIES = {"&lt;", "&gt;", "&amp;", "&apos;", "&quot;"};

   private StringBuffer fXmlBuffer = new StringBuffer(XML_HEADER);
   private ArrayList<String> fOpenElementList = new ArrayList<String>();

   /**
    * Insert the beginning of a new XML element fragment
    * @param tagName Tag name
    * @param atts List with element attributes
    * @param storeTagName Flag, decides whether tag name is stored in the open element list
    */
   private void appendElementFragment(String tagName, Map<String, String> atts, boolean storeTagName) {

      // ascertain idention
      final int ident = fOpenElementList.size();

      // append new tag to tag name list, if requested
      if (storeTagName) {
         fOpenElementList.add(tagName);
      }

      // write open tag fragment
      fXmlBuffer.append(spaces(ident));
      fXmlBuffer.append("<");
      fXmlBuffer.append(tagName);

      // check whether attributes are given
      if (atts != null) {

         // get key set iterator
         Iterator<String> it = atts.keySet().iterator();
         // declare key variable
         Object key;

         // append all attributes of the element
         while (it.hasNext()) {
            key = it.next();
            fXmlBuffer.append(' ');
            fXmlBuffer.append(key);
            fXmlBuffer.append("=\"");
            fXmlBuffer.append(atts.get(key));
            fXmlBuffer.append("\"");
         }  // while

      }  // if

   }

   /**
    * This method replaces the following characters by its entity definitions:
    *
    * @param value string which possibly includes special characters
    * @return given string with replaced entities
    */
   public static String replaceEntities(String value) {

      char aChar;
      int index = 0;
      // create a result string buffer
      StringBuffer result = new StringBuffer(value);
      // check each character whether it is a special character
      while (index < result.length()) {
         // read current character
         aChar = result.charAt(index);

         for (int i=0; i<SPECIAL_CHARS.length; i++) {
            if (aChar == SPECIAL_CHARS[i]) {
               result.replace(index, index+1, ENTITIES[i]);
               index += ENTITIES[i].length()-1;
               break;
            }
         }  // for

         // increment character index
         index++;
      }  // while

      return result.toString();
   }

   /**
    * Creates a new String filled with the given number of space characters
    * @param count number of spaces
    * @return String filled with space characters
    */
   private static String spaces(int count) {
      return SPACES.substring(0, count);
   }

   /**
    * Inserts a SYSTEM DTD
    * @param elem Name of the root element
    * @param uri URI of the DTD
    */
   public void setSystemDTD(String elem, String uri) {
      final String dtd = MessageFormat.format(SYSTEM_DTD, new Object[] {elem, uri});
      fXmlBuffer.insert(XML_HEADER.length(), dtd);
   }

   /**
    * Insert a new XML element. This opening tag must be closed later!
    * @param tagName Tag name
    * @param atts List with element attriutes
    * @param text Element text
    * @see XmlWriter#closeElement()
    */
   public void openElement(String tagName, Map<String, String> atts, String text) {

      appendElementFragment(tagName, atts, true);
      fXmlBuffer.append(">");
      fXmlBuffer.append(EOL);

      if (text != null && !text.trim().equals("")) {
         fXmlBuffer.append(text + EOL);
      }
   }

   /**
    * This method closes the last opened XML element. It should be used in combination
    * with the method "openElement". Indention will automaticly considered.
    *
    * @see XmlWriter#openElement(String, Map, String)
    */
   public void closeElement() {
      // get indention
      final int indent = fOpenElementList.size()-1;
      // get current tag name
      final String tagName = fOpenElementList.get(indent);
      // close current element
      fXmlBuffer.append(spaces(indent));
      fXmlBuffer.append("</");
      fXmlBuffer.append(tagName);
      fXmlBuffer.append(">");
      fXmlBuffer.append(EOL);
      // remove last tag from the list
      fOpenElementList.remove(indent);
   }

   /**
    * Insert a new complete XML element. All element content will be written in one line.
    * @param tagName Tag name
    * @param atts List with element attriutes
    * @param text Element text
    */
   public void addInLineElement(String tagName, Map<String, String> atts, String text) {

      // write open element fragment
      appendElementFragment(tagName, atts, false);

      // append text, if available
      if (text != null && !text.trim().equals("")) {
         // close opend element
         fXmlBuffer.append(">");
         fXmlBuffer.append(text);
         // close current element
         fXmlBuffer.append("</");
         fXmlBuffer.append(tagName);
         fXmlBuffer.append(">");
         fXmlBuffer.append(EOL);
      } else {
         fXmlBuffer.append("/>");
         fXmlBuffer.append(EOL);
      }

      // remove this tag name from the list
      fOpenElementList.remove(tagName);
   }

   /**
    * Insert a new empty (without tetx) XML element.
    * @param tagName Tag name
    * @param atts List with element attriutes
    */
   public void addEmptyElement(String tagName, Map<String, String> atts) {

      // we can append it as in-line tag
      addInLineElement(tagName, atts, null);
   }

   /**
    * @return complete XML document as string
    */
   public String toString() {
      return fXmlBuffer.toString();
   }

}
