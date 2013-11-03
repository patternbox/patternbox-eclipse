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

package com.patternbox.eclipse.codegen;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

import com.patternbox.eclipse.editor.DesignPatternPlugin;
import com.patternbox.eclipse.model.DesignPatternInstance;
import com.patternbox.eclipse.model.MemberNode;
import com.patternbox.eclipse.model.RoleNode;

/**
 * This class handles automatic code replacement of placeholders in generated 
 * design pattern members.
 * 
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class CodeReplacer {
   
   //------------------------------------------------------------------------- Field Definitions
   
   private static final CodeFormatterImpl sCodeFormatter = new CodeFormatterImpl();
   private static final String DELI = "$";
   
   private final MemberCodeGenerator fCodeGenerator;
   private final DesignPatternInstance fModel;
   private final RoleNode fRoleNode;

   public static final String REPLACE_START = "/* AUTO-REPLACE";
   public static final String REPLACE_END = "AUTO-REPLACE */";
   
   //------------------------------------------------------------------------- Constructors
   
	/**
	 * Default constructor 
	 */
   public CodeReplacer(MemberCodeGenerator codeGenerator) {
      fCodeGenerator = codeGenerator;
      fRoleNode = codeGenerator.getRoleNode();
      fModel = codeGenerator.getModel();
	}
   
   //------------------------------------------------------------------------- Private Methods
   
   private MemberNode getMemberNode(String roleName, int index, String foreachType) {
      
      // get member node
      if (foreachType != null && foreachType.equals(roleName)) {
         return fModel.getMember(roleName, index);
      }
      
      return fModel.getMember(roleName, 0);
   }
   
   //------------------------------------------------------------------------- Package Methods
   
	/* package */ void replacePlaceholders(MemberNode memberNode) {

		int firstPos, startPos, endPos, handledPos;
		IBuffer buffer;
		String original, placeholder;

		final IType member = memberNode.getMemberType();
      //final ICompilationUnit cu = JavaModelUtil.toOriginal(member.getCompilationUnit());
      final ICompilationUnit origCU = ((IMember) member.getPrimaryElement()).getCompilationUnit(); // JavaModelUtil.toOriginal(member.getCompilationUnit());
		final IWorkbenchPage activePage = DesignPatternPlugin.getActivePage();
		final IProgressMonitor monitor = new NullProgressMonitor();
		final StringBuffer resultBuffer = new StringBuffer();
		final List<String> importList = new LinkedList<String>();
      
		final IImportsManager imports = new IImportsManager() {

			public String addImport(String qualifiedTypeName) {

            // set dot position start value
            int dotPos = qualifiedTypeName.length() - 1;
            while (dotPos > 0 && qualifiedTypeName.charAt(dotPos) != '.') {
               dotPos--;
            } // while
            
            final String packageName = qualifiedTypeName.substring(0, dotPos);
            
            if (!member.getPackageFragment().getElementName().equals(packageName)) {
               importList.add(qualifiedTypeName);
            }  // if
            
				// return reduced type name => pure class name 
				return qualifiedTypeName.substring(dotPos + 1);
			}

			public Set<String> getImports() {
				// TODO Auto-generated method stub
				return null;
			}

		};

      IEditorPart editor = null;
		try {
			editor = JavaUI.openInEditor(origCU);
		} catch (PartInitException e) {
         DesignPatternPlugin.log(e);
         return;
		} catch (JavaModelException e) {
         DesignPatternPlugin.log(e);
         return;
		}
      
      final IEditorInput input = editor.getEditorInput();
      final IWorkingCopyManager manager = JavaUI.getWorkingCopyManager();
              
		if (activePage != null) {

         try {
				manager.connect(input);
			} catch (CoreException e) {
            DesignPatternPlugin.log(e);
            return;
			}
         
			try {

            final ICompilationUnit workingCopy = manager.getWorkingCopy(input);
            
				buffer = workingCopy.getBuffer();
				original = buffer.getContents();
				handledPos = 0;

				while ((startPos = original.indexOf(REPLACE_START, handledPos)) > 0) {
					// get position range
					endPos = original.indexOf(REPLACE_END, handledPos);
					firstPos = startPos - 1;
					// go back to the previous word 
					for (firstPos = startPos - 2; firstPos > 0; firstPos--) {
						if (original.charAt(firstPos) == ' '
							|| original.charAt(firstPos) == '('
							|| original.charAt(firstPos) == '\t') {
							break;
						}
					}

					// append leading code fragment
					resultBuffer.append(original.substring(handledPos, firstPos + 1));

					// replace and append placeholder
					placeholder = original.substring(startPos + REPLACE_START.length(), endPos).trim();
					resultBuffer.append(replaceWildcards(placeholder, 0, null, imports));
					resultBuffer.append(' ');

					// compute position until code replacement is handled
					handledPos = endPos + REPLACE_END.length();

				} // while

				// append trailing code fragment
				resultBuffer.append(original.substring(handledPos, original.length()));

            // format changed code
		      final String formattedCode = sCodeFormatter.format(resultBuffer.toString(), 0, null, MemberCodeGenerator.EOL);
		      
            buffer.setContents(formattedCode);
				buffer.save(monitor, false);
				buffer.close();

				// import Java type
				try {
					final Iterator<String> it = importList.iterator();
					while (it.hasNext()) {
                  workingCopy.createImport(it.next().toString(), null, monitor);
					} // while
               
				} catch (JavaModelException e) {
					DesignPatternPlugin.logException(e);
				}

            // trigger reconciliation        
         	JavaModelUtil.reconcile(workingCopy);

			} catch (JavaModelException e) {
				DesignPatternPlugin.log(e);
         } finally {
             manager.disconnect(input);
			} // try - catch - finally

		} // if
	}

   /**
    * Substitutes given Java type by its normalized form. If an design pattern 
    * participant member is not available, it will replaced by a special place 
    * holder. 
    * @param typeName Fully qualified name of a Java class or name of the design 
    *   pattern role
    * @param index Role member index
    * @param foreachType Name of type for for-each-iteration   
    * @return Reduced class name of the fully qualified class or an 
    *   auto-replacement-placeholder.
    */
   /* package */ String substituteJavaType(String typeName, int index, String foreachType, IImportsManager importMgr) {
      
      if (typeName.startsWith(DELI) && typeName.endsWith(DELI)) {
         // extract role name
         final String roleName = typeName.substring(1, typeName.length()-1);
         // get role node
         final RoleNode roleNode = fModel.getRole(roleName);
         // fall back
         if (roleNode == null) {
            DesignPatternPlugin.logErrorMessage("Unknown Java type '" + typeName + "'");
            return typeName;
         }
         
         // check whether the requested type references itself
         if (fRoleNode.equals(roleNode)) {
            return fCodeGenerator.getMemberName();
         }
         
         // get member node
         final MemberNode memberNode = getMemberNode(roleName, index, foreachType);
         
         if (memberNode != null) {
            return importMgr.addImport(memberNode.toString());
         }

         if (!roleNode.hasChildren()) {
            roleNode.addReplaceNode(fRoleNode);
         }

         return "Object " + REPLACE_START + " " + DELI + roleName + DELI + " " + REPLACE_END;   
         
      }  // if
      
      return importMgr.addImport(typeName);
   }
   
   /**
    * Replaces all wildcards by its types
    * @param input Input string including wildcards
    * @param isJavaType Is input string a pure Java type
    * @param index Role member index
    * @param foreachType Name of type for for-each-iteration   
    * @return Valid Java code fragment 
    */
   /* package */ String replaceWildcards(String input, int index, String foreachType, IImportsManager importsMgr) {
      
      // tokenizer throws an exception if input is null
      if (input == null) return "";
      
      // create string buffer as result 
      StringBuffer result = new StringBuffer();
      // create new tokenizer
      final StringTokenizer tokenizer = new StringTokenizer(input, DELI, true);
      String token;
      // set flag start value
      boolean inWildcard = false;
      // iterate all tokens
      while (tokenizer.hasMoreTokens()) {
         
         // read next token
         token = tokenizer.nextToken();
         
         // check whether token is equal to the delimiter
         if (token.equals(DELI)) {
            // set in wildcard flag
            inWildcard = !inWildcard;
            // read next token
            continue;
         }
         
         if (inWildcard) {
            
            // get member node
            MemberNode node = getMemberNode(token, index, foreachType);
            
            // check node availability
            if (node != null) {
                
               // strip package name   
               result.append(importsMgr.addImport(node.toString()));
                
            } else if (fRoleNode.getName().equals(token)) {
               
               // use new type as deputy 
               final String packName = fCodeGenerator.getCurrentPackage().getElementName();
					final String fullName =
						(packName.length() > 0)
							? packName + '.' + fCodeGenerator.getMemberName()
							: fCodeGenerator.getMemberName();
               // strip package name   
               result.append(importsMgr.addImport(fullName));
               
            } else {
               // fall back
               result.append(DELI);
               result.append(token);
               result.append(DELI);
            }
            
         } else {
            result.append(token);
         }

      }  // while

      return result.toString();
   }
   
}
