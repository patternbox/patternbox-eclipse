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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.text.edits.TextEdit;

import com.patternbox.commons.model.DocumentNode;
import com.patternbox.eclipse.model.DesignPatternInstance;
import com.patternbox.eclipse.model.MemberNode;
import com.patternbox.eclipse.model.RoleNode;

/**
 * Generates the code of a new participant member.
 * 
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class MemberCodeGenerator {

   // ------------------------------------------------------------------------- Field Definitions

   private static final CodeFormatterImpl sCodeFormatter = new CodeFormatterImpl();

   public static final String EOL = System.getProperty("line.separator", "\n");

   private final DesignPatternInstance fModel;

   private final RoleNode fRoleNode;

   private final IPackageFragmentRoot fCurrRoot;

   private final CodeReplacer fCodeReplacer;

   private IPackageFragment fCurrPackage;

   private String fMemberName;

   private boolean fIsClass = true;

   private IType fCreatedType = null;

   private IImportsManager fImports;

   //private Collection fTypeImports;

   // ------------------------------------------------------------------------- Constructors

   public MemberCodeGenerator(DesignPatternInstance model, RoleNode roleNode, String memberName,
            IPackageFragmentRoot packFragRoot, IPackageFragment packFrag) {

      fModel = model;
      fRoleNode = roleNode;
      fCurrRoot = packFragRoot;
      fCurrPackage = packFrag;
      fMemberName = memberName;
      fCodeReplacer = new CodeReplacer(this);
   }

   // ------------------------------------------------------------------------- Private Methods

   private void writeSuperClass(StringBuffer buf, String superClassName) {
      if (fIsClass && superClassName.length() > 0 && !"java.lang.Object".equals(superClassName)) { //$NON-NLS-1$
         buf.append(" extends "); //$NON-NLS-1$
         buf.append(fCodeReplacer.substituteJavaType(superClassName, 0, null, fImports));
      }
   }

   private void writeSuperInterfaces(StringBuffer buf, String[] interfaces) {

      if (interfaces != null && interfaces.length > 0) {

         if (fIsClass) {
            buf.append(" implements "); //$NON-NLS-1$
         } else {
            buf.append(" extends "); //$NON-NLS-1$
         }

         final int last = interfaces.length - 1;
         for (int i = 0; i <= last; i++) {
            buf.append(fCodeReplacer.substituteJavaType(interfaces[i], 0, null, fImports));
            if (i < last) {
               buf.append(',');
            } // if
         } // for

      } // if
   }

   /**
    * Called from createType to construct the source for this type
    */
   private String constructTypeStub(int modifiers, String superClassName, String[] interfaces) {
      StringBuffer buf = new StringBuffer();

      buf.append(Flags.toString(modifiers));

      if (modifiers != 0) {
         buf.append(' ');
      }

      buf.append(fIsClass ? "class " : "interface "); //$NON-NLS-2$ //$NON-NLS-1$
      buf.append(fMemberName);

      if (superClassName != null) {
         writeSuperClass(buf, superClassName);
      }

      writeSuperInterfaces(buf, interfaces);
      buf.append('{');
      buf.append(EOL);
      buf.append(EOL);
      buf.append('}');
      buf.append(EOL);
      return buf.toString();
   }

   private String getTypeComment() {

      final StringBuffer result = new StringBuffer();

      // 1. line
      result.append("/**");
      result.append(EOL);

      // participant name
      result.append(" * PatternBox: \"");
      result.append(fRoleNode.getName());
      result.append("\" implementation.");
      result.append(EOL);

      // append comment item list
      if (!fRoleNode.getCommentItemList().isEmpty()) {
         result.append(" * <ul>");
         result.append(EOL);

         for (int i = 0; i < fRoleNode.getCommentItemList().size(); i++) {
            result.append(" *   <li>");
            result.append(fRoleNode.getCommentItemList().get(i));
            result.append("</li>");
            result.append(EOL);
         } // for

         result.append(" * </ul>");
         result.append(EOL);
         result.append(" * ");
         result.append(EOL);
      } // if

      // author line
      result.append(" * ");
      result.append(fModel.getDesignPatternModel().getAuthor());
      result.append(EOL);

      // second author line
      final String user = System.getProperty("user.name");
      if (user != null && !user.trim().equals("")) {
         result.append(" * @author ");
         result.append(user);
         result.append(EOL);
      }

      // last line
      result.append(" */");
      // result.append(EOL);

      return result.toString();

   }

   private void addImportsToTargetUnit(final ICompilationUnit targetUnit, final IProgressMonitor monitor) throws CoreException,
            JavaModelException {
      
      monitor.beginTask("", 2); //$NON-NLS-1$
      
      try {
         ImportRewrite rewrite = StubUtility.createImportRewrite(targetUnit, true);

         String qualifiedImport;
         for (final Iterator<String> iterator = fImports.getImports().iterator(); iterator.hasNext();) {
            qualifiedImport = iterator.next();
            rewrite.addImport(qualifiedImport);
         }

         TextEdit edits = rewrite.rewriteImports(new SubProgressMonitor(monitor, 1));
         JavaModelUtil.applyEdit(targetUnit, edits, false, new SubProgressMonitor(monitor, 1));
      } finally {
         monitor.done();
      }
   }

   /**
    * Uses the New Java file template from the code template page to generate a compilation unit with the given type content.
    * 
    * @param cu
    *           The new created compilation unit
    * @param typeContent
    *           The content of the type, including signature and type body.
    * @param lineDelimiter
    *           The line delimiter to be used.
    * @return String Returns the result of evaluating the new file template with the given type content.
    * @throws CoreException
    */
   private String constructCUContent(IProgressMonitor monitor, ICompilationUnit cu, String typeContent) throws CoreException {

      StringBuffer typeQualifiedName = new StringBuffer();
      typeQualifiedName.append(fMemberName);
      final String typeComment = getTypeComment();

      try {
         monitor.beginTask("", 2); //$NON-NLS-1$
         final String content = CodeGeneration.getCompilationUnitContent(cu, typeComment, typeContent, EOL);
         cu.getBuffer().setContents(content);
      } finally {
         monitor.done();
      }
      return cu.getSource();

   }

   /**
    * Returns the <code>IPackageFragmentRoot</code> that corresponds to the current value of the source folder field.
    * 
    * @return the IPackageFragmentRoot or <code>null</code> if the current source folder value is not a valid package fragment
    *         root
    * 
    */
   private IPackageFragmentRoot getPackageFragmentRoot() {
      return fCurrRoot;
   }

   /**
    * Converts comment into single lines
    * 
    * @param comment
    *           Read comment from template file
    * @return Comment splitted into lines
    */
   private String[] formatComment(String comment) {

      // create new string tokenizer to split up all lines
      final StringTokenizer tokenizer = new StringTokenizer(comment, "\n");
      // create list for storing comment lines
      final List<String> lines = new ArrayList<String>();
      // copy tokens as lines
      while (tokenizer.hasMoreTokens()) {
         lines.add(tokenizer.nextToken().trim());
      }

      // remove leading empty lines
      while (!lines.isEmpty()) {
         // check empty line
         if (!lines.get(0).toString().equals(""))
            break;
         // remove empty line
         lines.remove(0);
      } // while

      // remove trailing empty lines
      int last;
      while (!lines.isEmpty()) {
         // get last line index
         last = lines.size() - 1;
         // check empty line
         if (!lines.get(last).toString().equals(""))
            break;
         // remove empty line
         lines.remove(last);
      } // while

      // create result array
      String[] result = new String[lines.size()];
      // copy lines as string array
      lines.toArray(result);

      return result;

   }

   // ------------------------------------------------------------------------- Package Methods

   /* package */DesignPatternInstance getModel() {
      return fModel;
   }

   /* package */RoleNode getRoleNode() {
      return fRoleNode;
   }

   /* package */String getMemberName() {
      return fMemberName;
   }

   /* package */IPackageFragment getCurrentPackage() {
      return fCurrPackage;
   }

   // ------------------------------------------------------------------------- Public Methods

   /**
    * Append a new field.
    * 
    * @param modifiers
    *           Field modifiers
    * @param type
    *           Field type
    * @param name
    *           Field name
    * @param initial
    *           Initial field value, can be <code>null</code>
    * @param comment
    *           Field comment, can be <code>null</code>
    * @param index
    *           This parameter is required for type iteration
    * @param foreachType
    *           Name of type for for-each-iteration
    * @throws JavaModelException
    */
   public void appendField(int modifiers, String type, String name, String initial, String comment, int index, String foreachType)
            throws JavaModelException {

      StringBuffer buf = new StringBuffer();

      if (comment != null && comment.length() > 0) {
         buf.append("/** ");
         // get comment spitted into lines
         final String[] lines = formatComment(comment);
         // write comment to code buffer
         if (lines.length > 0)
            buf.append(lines[0]);
         for (int i = 1; i < lines.length; i++) {
            buf.append(" * ");
            buf.append(lines[i]);
            buf.append(EOL);
         }
         // close comment
         buf.append(" */");
         buf.append(EOL);
      }

      buf.append(Flags.toString(modifiers));
      if (modifiers == 0) {
         buf.append("/* package */");
      }

      buf.append(' ');
      buf.append(fCodeReplacer.substituteJavaType(type, index, foreachType, fImports));
      buf.append(' ');
      buf.append(fCodeReplacer.replaceWildcards(name, index, foreachType, fImports));

      if (initial != null && initial.length() > 0) {
         buf.append(" = ");
         buf.append(fCodeReplacer.replaceWildcards(initial, index, foreachType, fImports));
      }

      buf.append(';');
      buf.append(EOL);
      buf.append(EOL);
      fCreatedType.createField(buf.toString(), null, false, null);

   }

   /**
    * Append a new method.
    * 
    * @param modifiers
    *           Method modifiers
    * @param returnType
    *           Return type of method
    * @param name
    *           Method name
    * @param params
    *           Method parameters
    * @param codeFrag
    *           Contains the method code fragment
    * @param comment
    *           Method comment, can be <code>null</code>
    * @param index
    *           This parameter is required for type iteration
    * @param foreachType
    *           Name of type for for-each-iteration
    * @param isConstructor
    *           Is current method a Java class constructor
    * @throws JavaModelException
    */
   public void appendMethod(int modifiers, String returnType, String name, List<MethodParam> params, List<String> exceptions, String codeFrag,
            String comment, int index, String foreachType, boolean isConstructor) throws JavaModelException {

      StringBuffer buf = new StringBuffer();

      if (comment != null && comment.length() > 0) {
         buf.append("/** ");
         buf.append(EOL);
         // get comment spitted into lines and store them
         final String[] lines = formatComment(comment);
         for (int i = 0; i < lines.length; i++) {
            buf.append(" * ");
            buf.append(lines[i]);
            buf.append(EOL);
         }
         // close comment
         buf.append(" */");
         buf.append(EOL);
      }

      buf.append(Flags.toString(modifiers));
      if (modifiers == 0) {
         buf.append("/* package */");
      }

      if (!isConstructor) {
         buf.append(' ');
         buf.append(fCodeReplacer.substituteJavaType(returnType, index, foreachType, fImports));
      } // if

      buf.append(' ');
      buf.append(fCodeReplacer.replaceWildcards(name, index, foreachType, fImports));
      buf.append('(');

      MethodParam param;
      int count = params.size();
      for (int i = 0; i < count; i++) {
         // assign next parameter
         param = params.get(i);
         // append parameter
         buf.append(fCodeReplacer.substituteJavaType(param.getType(), index, foreachType, fImports));
         buf.append(' ');
         buf.append(param.getName());
         // check whether current parameter is the last one
         if (i < count - 1) {
            buf.append(", ");
         } // if
      } // for

      // close parameter list
      buf.append(')');

      // check exception list
      if ((count = exceptions.size()) > 0) {

         buf.append(" throws ");

         for (int i = 0; i < count; i++) {
            buf.append(fCodeReplacer.substituteJavaType(exceptions.get(i).toString(), 0, null, fImports));
            // check whether current exception is the last one
            if (i < count - 1) {
               buf.append(", ");
            } // if
         } // for

      } // if

      if (fIsClass && !Flags.isAbstract(modifiers)) {
         buf.append(" {");
         buf.append(fCodeReplacer.replaceWildcards(codeFrag, index, foreachType, fImports));
         buf.append('}');
      } else {
         buf.append(';');
      }

      buf.append(EOL);
      buf.append(EOL);

      fCreatedType.createMethod(buf.toString(), null, false, null);

   }

   // ---- creation ----------------

   public ICompilationUnit startTypeCreation(IProgressMonitor monitor, boolean isClass, int modifiers, String[] imports,
            String superClass, String[] interfaces) throws CoreException, InterruptedException {

      monitor.beginTask("create member", 10); //$NON-NLS-1$

      fIsClass = isClass;

      ICompilationUnit createdWorkingCopy = null;

      IPackageFragmentRoot root = getPackageFragmentRoot();

      if (!fCurrPackage.exists()) {
         String packName = fCurrPackage.getElementName();
         fCurrPackage = root.createPackageFragment(packName, true, null);
      }

      monitor.worked(1);

      ICompilationUnit parentCU = fCurrPackage.createCompilationUnit(
               fMemberName + ".java", "", false, new SubProgressMonitor(monitor, 2)); //$NON-NLS-1$ //$NON-NLS-2$

      // create a working copy with a new owner
      createdWorkingCopy = parentCU.getWorkingCopy(null);

      // fImports = new ImportsManager(createdWorkingCopy); v3.2
      fImports = new ImportsManager();

      // add an import that will be removed again. Having this import solves 14661
      // fImports.addImport(JavaModelUtil.concatenateName(fCurrPackage.getElementName(), fMemberName));
      // fImports.fImportsStructure.addImport(JavaModelUtil.concatenateName(fCurrPackage.getElementName(), fMemberName)); v3.2

      String typeContent = constructTypeStub(modifiers, superClass, interfaces);

      // fImports.create(false, new SubProgressMonitor(monitor, 1));

      fCreatedType = createdWorkingCopy.getType(fMemberName);

      String cuContent = constructCUContent(monitor, parentCU, typeContent);

      createdWorkingCopy.getBuffer().setContents(cuContent);

      // fCreatedType = createdWorkingCopy.getType(fMemberName);

      if (monitor.isCanceled()) {
         throw new InterruptedException();
      }

      ICompilationUnit cu = fCreatedType.getCompilationUnit();
      //boolean needsSave = !cu.isWorkingCopy();

      // add imports for superclass/interfaces, so types can be resolved correctly
      // fImports.create(needsSave, new SubProgressMonitor(monitor, 1), false); v3.2

      JavaModelUtil.reconcile(cu);

      if (monitor.isCanceled()) {
         throw new InterruptedException();
      }

      // set up again
      // fImports = new ImportsManager(fImports.getCompilationUnit(), fImports.getAddedTypes()); v3.2
      fImports = new ImportsManager(fImports.getImports());

      for (int i = 0; i < imports.length; i++) {
         fCodeReplacer.substituteJavaType(imports[i], 0, null, fImports);
      } // for

      return createdWorkingCopy;

   }

   public void finishTypeCreation(IProgressMonitor monitor) throws CoreException {

      // add imports
      // fImports.create(false, new SubProgressMonitor(monitor, 1), true); v3.2

      ICompilationUnit cu = fCreatedType.getCompilationUnit();
      
      addImportsToTargetUnit(cu, new SubProgressMonitor(monitor, 1));

      JavaModelUtil.reconcile(cu);

      ISourceRange range = fCreatedType.getSourceRange();

      IBuffer buf = cu.getBuffer();
      final String original = buf.getText(range.getOffset(), range.getLength());
      final String formattedCode = sCodeFormatter.format(original, 0, null, EOL);

      buf.replace(range.getOffset(), range.getLength(), formattedCode);

      cu.commitWorkingCopy(false, new SubProgressMonitor(monitor, 1));

   }

   /**
    * @return
    */
   public IType getCreatedType() {
      return fCreatedType;
   }

   /**
    * Returns a list with all members nodes which includes placeholders for automatic replacement.
    * 
    * @return List with member nodes
    */
   public List<MemberNode> getAutomaticReplaceList() {

      final List<MemberNode> result = new LinkedList<MemberNode>();

      for (RoleNode role: fRoleNode.getReplaceNodeSet()) {
    	  for (DocumentNode member: role.getChildrenCollection()) {
    		  if (member instanceof MemberNode) {
    			  result.add((MemberNode) member); 
    		  }
    	  }
      }
      
      return result;

   }

   /**
    * Replaces all placeholders by its Java types
    */
   public void handleAutomaticReplace(List<MemberNode> memberList) {

      Iterator<MemberNode> memberSet = memberList.iterator();
      while (memberSet.hasNext()) {
         fCodeReplacer.replacePlaceholders(memberSet.next());
      } // while

      // remove role node entries
      fRoleNode.clearReplaceNodes();

   }

}
