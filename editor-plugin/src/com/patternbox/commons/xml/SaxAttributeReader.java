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

import org.xml.sax.*;

/**
 * Helper class for reading XML-attribute values during SAX parsing.
 *
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class SaxAttributeReader {

   private static final String TRUE = "true";
   private static final String FALSE = "false";

   public static String getString(Attributes atts, String attrName, String defaultVal,
      boolean required) throws EXmlAttributeMissing {

      String result = atts.getValue(attrName);

      if (result == null) {
         if (required) {
            throw new EXmlAttributeMissing("Attribute '" + attrName + "' missing."); // Element '" + atts.getQName(0) + "'");
         }
         result = defaultVal;
      }  // if

      return result;

   }

   public static boolean getBool(Attributes atts, String attrName, boolean defaultVal,
      boolean required) throws EXmlAttributeMissing {

      //String strValue = getString(atts, attrName, Boolean.toString(defaultVal), required); not jdk 1.3 compatible

      String strValue;

      if (defaultVal) {
         strValue = getString(atts, attrName, TRUE, required);
      } else {
        strValue = getString(atts, attrName, FALSE, required);
      }

      return strValue.equalsIgnoreCase(TRUE);

   }

   public static int getInt(Attributes atts, String attrName, int defaultVal,
      boolean required) throws EXmlAttributeMissing {

      String strValue = getString(atts, attrName, String.valueOf(defaultVal), required);

      try {
         return Integer.parseInt(strValue);
      } catch (Exception e) {
         throw new EXmlAttributeMissing(e);
      }  // try - catch

   }

   public static long getLong(Attributes atts, String attrName, long defaultVal,
      boolean required) throws EXmlAttributeMissing {

      String strValue = getString(atts, attrName, String.valueOf(defaultVal), required);

      try {
         return Long.parseLong(strValue);
      } catch (Exception e) {
         throw new EXmlAttributeMissing(e);
      }  // try - catch

   }

}
