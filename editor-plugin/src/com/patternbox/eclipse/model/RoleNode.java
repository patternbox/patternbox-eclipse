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

import java.text.MessageFormat;
import java.util.*;

//import org.eclipse.pde.internal.core.plugin.DocumentNode;

import com.patternbox.commons.model.DocumentNode;

/**
 * Implementation of a design pattern participant. *  * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */

public class RoleNode extends DocumentNode {

   // ------------------------------------------------------------------------- Field Defintions

   private static final MessageFormat sStrFormatter = new MessageFormat("{0} [{1}..{2}]");

   private final Set<RoleNode> fReplace = new HashSet<RoleNode>();
   private final String fFormatedString;

   /**
    * @uml.property name="properties"
    * @uml.associationEnd multiplicity="(0 1)"
    */
   private final RolePropertiesContainer fProperties;

   private final String fReplaceList;

   /**
    * @uml.property name="designPatternInstance"
    * @uml.associationEnd multiplicity="(0 1)"
    */
   private DesignPatternInstance fDesignPatternInstance = null;


   // ------------------------------------------------------------------------- Constructors

	/**
    * Default constructor
	 * @param parent Root node of the design pattern model
    * @param props Common properties of role node
    * @param replaceList List of names of role nodes
    * @see DesignPatternModel
	 */
	public RoleNode(DocumentNode parent, RolePropertiesContainer props, String replaceList) {
		super(parent);

      fProperties = props;
      fReplaceList = replaceList;

      // convert cardinality to string
      final String translated =
         (props.getMaxOccurence() == Integer.MAX_VALUE) ? "*" : String.valueOf(props.getMaxOccurence());

      // format string once for the 'toString()'-method
      final Object[] params = new Object[] {props.getName(),
         new Integer(props.getMinOccurence()), translated};

      fFormatedString = sStrFormatter.format(params);
	}

   // ------------------------------------------------------------------------- Protected Methods

   /**
    * @param instance Associated design pattern instance
    */
   protected void setDesignPatternInstance(DesignPatternInstance instance) {
      // assign design pattern instance
      fDesignPatternInstance = instance;
      // check replace list availability
      if (fReplaceList == null)
         return;
      // tokenize code replacement list
      final StringTokenizer tokenizer = new StringTokenizer(fReplaceList, " ");
      // get access of all role nodes
      DocumentNode[] roleList = getParent().getChildren();
      // declare role node variable
      String curRoleName;
      // iterate all tokens of our replacement list
      while (tokenizer.hasMoreTokens()) {
         // get current role node name
         curRoleName = tokenizer.nextToken();
         // compare current role name with each role
         for (int i = 0; i < roleList.length; i++) {
            if (((RoleNode) roleList[i]).getName().equalsIgnoreCase(curRoleName)) {
               // append role node for replacement
               addReplaceNode((RoleNode) roleList[i]);
               // leave for-loop
               break;
            } // if
         } // for
      } // while

   }

   // ------------------------------------------------------------------------- Public Methods

   /**
    * Returns the associated model
    */
//   public DesignPatternInstance getDesignPatternInstance() {
//      return fDesignPatternInstance;
//   }

   /**
    * @return
    */
   public int getMaxOccurence() {
      return fProperties.getMaxOccurence();
   }

   /**
    * @return
    */
   public int getMinOccurence() {
      return fProperties.getMinOccurence();
   }

   /**
    * @return
    */
   public String getName() {
      return fProperties.getName();
   }

   public String[] getDependenceRoles() {
      return fProperties.getDependence();
   }

   public List<String> getCommentItemList() {
      return fProperties.getCommentItemList();
   }

   /**
    * Add new role node for automatic replacement
    * @param roleNode Role node to add
    */
   public void addReplaceNode(RoleNode roleNode) {
      fReplace.add(roleNode);
   }

   /**
    * Remove a role nodes after automatic replacement
    */
   public void clearReplaceNodes() {
      fReplace.clear();
   }

   /**
    * Returns the set of role nodes for automatic replacement
    * @return Iterator of the role nodes set
    */
   public Set<RoleNode> getReplaceNodeSet() {
      return fReplace;
   }

   /**
    * Returns a list of names of all role nodes for automatic replacement
    * @return List of all role nodes if available, otherwise <code>null</code>
    */
   public String getReplaceNodesAsString() {

      if (fReplace.size() > 0) {

         final StringBuffer result = new StringBuffer();

         final Iterator<RoleNode> it = fReplace.iterator();

         // build role node list
         while (it.hasNext()) {
            result.append( (it.next()).getName() );
            result.append(' ');
         }  // while

         // remove last space character
         return result.toString().trim();

      }  // if
      
      return null;
   }

   /**
    * Checks whether participants can be added.
    * @return <code>true</code>, RoleNode is active
    */
   public boolean isActive() {

      // check member count
      boolean result = this.getChildren().length < this.getMaxOccurence();

      // check whether all depended role has got members
      if (result && fDesignPatternInstance != null) {
         // get dependence list
         final String[] roleList = this.getDependenceRoles();
         // iterate dependence list
         for (int i=0; roleList != null && i<roleList.length; i++) {
            result &= fDesignPatternInstance.getRole(roleList[i]).hasChildren();
         }  // for
      }  // if

      return result;

   }

   /**
    * @see java.lang.Object#toString()
    */
   public String toString() {
      return fFormatedString;
   }

}
