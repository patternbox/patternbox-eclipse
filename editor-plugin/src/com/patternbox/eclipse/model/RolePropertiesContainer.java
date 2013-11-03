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

import java.util.*;

/**
 * This class is a container for all properties of a design pattern role.
 *
 * @see com.patternbox.eclipse.model.ManifestXmlHandler
 *
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class RolePropertiesContainer {

   private final List<String> fCommentItemList = new LinkedList<String>();
   private String fName;
   private int fMinOccurence = -1;
   private int fMaxOccurence = -1;
   private String[] fDependenceList = null;

   /**
    * @return Returns name of the role
    * 
    * @uml.property name="name"
    */
   public String getName() {
      return fName;
   }

   /**
    * @param name Name of the role
    * 
    * @uml.property name="name"
    */
   public void setName(String name) {
      fName = name;
   }

   /**
    * @return
    * 
    * @uml.property name="minOccurence"
    */
   public int getMinOccurence() {
      return fMinOccurence;
   }

   /**
    * @param min
    * 
    * @uml.property name="minOccurence"
    */
   public void setMinOccurence(int min) {
      fMinOccurence = min;
   }

   /**
    * @return
    * 
    * @uml.property name="maxOccurence"
    */
   public int getMaxOccurence() {
      return fMaxOccurence;
   }

   /**
    * @param max
    * 
    * @uml.property name="maxOccurence"
    */
   public void setMaxOccurence(int max) {
      fMaxOccurence = max;
   }


   /**
    * @return Array of role names
    */
   public String[] getDependence() {
      return fDependenceList;
   }

   /**
    * @param roleNameList Enumeration of role names
    */
   public void setDependence(String roleNameList) {

      StringTokenizer tokenizer = new StringTokenizer(roleNameList, ", ");

      fDependenceList = new String[tokenizer.countTokens()];

      for (int i=0; i<fDependenceList.length; i++) {
         fDependenceList[i] = tokenizer.nextToken();
      }  // for

   }

   /**
    * Adds a new comment item. Comments are organized as list items!
    * @param listItem New comment item to add.
    */
   public void addCommentItem(String listItem) {
      fCommentItemList.add(listItem);
   }

   /**
    * Returns a list with a collected comment items.
    * @return Comment item list.
    * 
    * @uml.property name="commentItemList"
    */
   public List<String> getCommentItemList() {
      return fCommentItemList;
   }

}
