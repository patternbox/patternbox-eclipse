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

import java.io.*;
import java.util.*;

import javax.xml.parsers.*;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import com.patternbox.commons.xml.*;
import com.patternbox.eclipse.editor.DesignPatternPlugin;
import com.patternbox.eclipse.model.*;

/**
 * Creates a new java source file from XML template.
 * 
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class CodeTemplateXmlHandler extends DefaultHandler {

   // ------------------------------------------------------------------------- Field Definitons
   
   //private static final String ELEM_TEMPLATES = "templates";
   private static final String ELEM_ROLE = "role";
   private static final String ELEM_IMPORT = "import";
   private static final String ELEM_SUPERCLASS = "superclass";
   private static final String ELEM_INTERFACE = "interface";
   private static final String ELEM_FIELD = "field";
   private static final String ELEM_CONSTRUCTOR = "constructor";
   private static final String ELEM_METHOD = "method";
   private static final String ELEM_PARAMETER = "param";
   private static final String ELEM_COMMENT = "comment";
   private static final String ELEM_EXCEPTION = "exception";
   private static final String ELEM_CODE = "code";
   private static final String ELEM_REPLACE = "replace";
   
   private static final String ATTR_NAME = "name";
   private static final String ATTR_MODIFIERS = "modifiers";
   private static final String ATTR_TYPE = "type";
   private static final String ATTR_INITIAL = "initial";
   private static final String ATTR_RETURN = "return";
   private static final String ATTR_FOREACH = "foreach";
   private static final String ATTR_DEPENDENCE = "dependence";
   
   private static SAXParser sXmlParser = null;

   private final StringBuffer fElementValue = new StringBuffer();
   private final List<String> fImports = new LinkedList<String>(); 
   private final List<String> fInterfaces = new ArrayList<String>(); 
   private final List<MethodParam> fParamList = new ArrayList<MethodParam>();
   private final List<String> fExceptionList = new ArrayList<String>();
   private final Set<String> fDependenceSet = new HashSet<String>();
   private IProgressMonitor fMonitor;   
   private MemberCodeGenerator fCodeGenerator;
   private DesignPatternInstance fModel;
   private RoleNode fRoleNode;
   private String fMemberName;
   private String fSuperClass;
   //private IPackageFragmentRoot fPackageFragRoot;
   //private IPackageFragment fPackageFrag;
   private boolean fReqRoleActive;
   private boolean fCreationStarted;
   private boolean fIsClass;
   private int fNestingLevel;
   
   private int fModifiers;
   private String fFieldType;
   private String fReturnType;
   private String fName;
   private String fInitialValue;
   private String fCodeFrag;
   private String fComment;
   private String fParamType;
   private String fParamName;
   private String fForEachMember;

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
   
   // ------------------------------------------------------------------------- Private Methods
   
   private void startCreation() throws SAXException {
      
      String[] imports = new String[fImports.size()];
      fImports.toArray(imports);
      String[] interfaces = new String[fInterfaces.size()];
      fInterfaces.toArray(interfaces);
      
      try {
         
			fCodeGenerator.startTypeCreation(
				fMonitor,
				fIsClass,
				fModifiers,
				imports,
				fSuperClass,
				interfaces);
            
		} catch (CoreException e) {
         throw new SAXException(e);
		} catch (InterruptedException e) {
         throw new SAXException(e);
		}  // try - catch
      
      fCreationStarted = true;
   }
   
   private int convertModifiersAsInt(String modifiers) {
      
      // initialize result value
      int result = 0;
      
      result += (modifiers.indexOf("public") >= 0) ? Flags.AccPublic : 0;
      result += (modifiers.indexOf("private") >= 0) ? Flags.AccPrivate : 0;
      result += (modifiers.indexOf("protected") >= 0) ? Flags.AccProtected : 0;
      result += (modifiers.indexOf("static") >= 0) ? Flags.AccStatic : 0;
      result += (modifiers.indexOf("final") >= 0) ? Flags.AccFinal : 0;
      result += (modifiers.indexOf("synchronized") >= 0) ? Flags.AccSynchronized : 0;
      result += (modifiers.indexOf("volatile") >= 0) ? Flags.AccVolatile : 0;
      result += (modifiers.indexOf("transient") >= 0) ? Flags.AccTransient : 0;
      result += (modifiers.indexOf("native") >= 0) ? Flags.AccNative : 0;
      result += (modifiers.indexOf("interface") >= 0) ? Flags.AccInterface : 0;
      result += (modifiers.indexOf("abstract") >= 0) ? Flags.AccAbstract : 0;
      
      return result;
      
   }
   
   private void parseCodeTemplateFile(File templateFile) throws IOException, SAXException {

      // declare variables
      FileInputStream inStream;

      // create new XML reader for parsing the manifest file
      XMLReader reader = sXmlParser.getXMLReader();
      // set manifest handler as content and error handler 
      reader.setContentHandler(this);
      reader.setErrorHandler(this);
      
      // open manifest file as input stream
      inStream = new FileInputStream(templateFile);
      
      InputSource inpSrc = new InputSource(inStream);
      
      inpSrc.setSystemId(XmlDocValidator.getSystemURI(templateFile));
      
      // parse manifest file
      reader.parse(inpSrc);
      
      // close input stream
      inStream.close();
   }
   
   /**
    * Determines the number of iterations needed to cover the given type 
    * @param foreach Iteration base type
    * @return Number of needed iterations
    */
   private int getIterationCount(String foreach) {
      
      if (foreach == null || foreach.length() == 0) return 0;
      
      final int typeCount = fModel.getRole(foreach).getChildren().length;
      
      return Math.max(0, typeCount-1);
   }
   
   // ------------------------------------------------------------------------- XML Parser Handler Methods
   
   /**
    * @see org.xml.sax.ContentHandler#startDocument()
    */
   public void startDocument() throws SAXException {
      fNestingLevel = -1;
      fReqRoleActive = false;
      fCreationStarted = false;
      
      fSuperClass = null;
      fImports.clear();
      fInterfaces.clear();
      fDependenceSet.clear();
   }
   
   /**
    * @see org.xml.sax.ContentHandler#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
    */
   public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
      throws SAXException {
	   
      // clear old element value
      if (!qName.equals(ELEM_REPLACE)) fElementValue.setLength(0);
      
      // increment nesting level
		fNestingLevel++;
		// (re)set requested design pattern role flag
		fReqRoleActive = fReqRoleActive ||
         (qName.equals(ELEM_ROLE) && atts.getValue(ATTR_NAME).equals(fRoleNode.getName()));

		// check whether requested design pattern role is active, otherwise leave method
		if (!fReqRoleActive) return;

      if (qName.equals(ELEM_ROLE)) {
         
         final String type = SaxAttributeReader.getString(atts, ATTR_TYPE, null, true);
         fIsClass = type.equals("class");
         
         final String scope = SaxAttributeReader.getString(atts, ATTR_MODIFIERS, null, false);
         fModifiers = convertModifiersAsInt(scope);
         
      } else if (qName.equals(ELEM_IMPORT)) {
         
         fImports.add(SaxAttributeReader.getString(atts, ATTR_TYPE, null, true));
         
      } else if (qName.equals(ELEM_SUPERCLASS)) {
         
         fSuperClass = SaxAttributeReader.getString(atts, ATTR_TYPE, null, true);
         
      } else if (qName.equals(ELEM_INTERFACE)) {
         
         fInterfaces.add(SaxAttributeReader.getString(atts, ATTR_TYPE, null, true));
         
      } else if (qName.equals(ELEM_FIELD)) {
         
         // start type creation if is not already done
         if (!fCreationStarted) startCreation();
         // clear old comment
         fComment = null;

			// read field attributes
			final String scope = SaxAttributeReader.getString(atts, ATTR_MODIFIERS, null, false);
         fModifiers = convertModifiersAsInt(scope);
         fFieldType = SaxAttributeReader.getString(atts, ATTR_TYPE, null, true);
         fName = SaxAttributeReader.getString(atts, ATTR_NAME, null, true);
         fInitialValue = SaxAttributeReader.getString(atts, ATTR_INITIAL, null, false);
         fForEachMember = SaxAttributeReader.getString(atts, ATTR_FOREACH, null, false); 
         
		} else if (qName.equals(ELEM_METHOD) || qName.equals(ELEM_CONSTRUCTOR)) {

         // start type creation if is not already done
         if (!fCreationStarted) startCreation();
         // clear old comment
         fComment = null;
         // clear parameter list
         fParamList.clear();
         // clear exception list
         fExceptionList.clear(); 

			// read method attributes
         final boolean isCons = qName.equals(ELEM_CONSTRUCTOR);
         final String scope = SaxAttributeReader.getString(atts, ATTR_MODIFIERS, null, false);
         fModifiers = convertModifiersAsInt(scope);
         fReturnType = SaxAttributeReader.getString(atts, ATTR_RETURN, null, !isCons);
         fName = SaxAttributeReader.getString(atts, ATTR_NAME, fMemberName, !isCons);
         fForEachMember = SaxAttributeReader.getString(atts, ATTR_FOREACH, null, false); 

      } else if (qName.equals(ELEM_PARAMETER)) {
            
         fParamType = SaxAttributeReader.getString(atts, ATTR_TYPE, null, true);
         fParamName = SaxAttributeReader.getString(atts, ATTR_NAME, null, true);
            
      } else if (qName.equals(ELEM_EXCEPTION)) {
            
         fExceptionList.add(SaxAttributeReader.getString(atts, ATTR_TYPE, null, true));
            
      } else if (qName.equals(ELEM_REPLACE)) {
         
         fElementValue.append(SaxAttributeReader.getString(atts, ATTR_INITIAL, null, true));
         fElementValue.append(CodeReplacer.REPLACE_START);
         fElementValue.append(' ');
         
         final String dependence = SaxAttributeReader.getString(atts, ATTR_DEPENDENCE, null, true); 
         final RoleNode dependenceRole = fModel.getRole(dependence);
         if (dependenceRole != null && !dependenceRole.hasChildren()) {
            dependenceRole.addReplaceNode(fRoleNode);
         }
         
		}
      
	}

   /**
    * @see org.xml.sax.ContentHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
    */
   public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
      
      // decrement nesting level
      fNestingLevel--;
      // reset requested design pattern role flag
      fReqRoleActive = qName.equals(ELEM_ROLE) ? false : fReqRoleActive;
      
      // check whether requested design pattern role is active, otherwise leave method
      if (!fReqRoleActive) return;

      try {
         
         if (qName.equals(ELEM_FIELD)) {

            for (int index = 0; index <= getIterationCount(fForEachMember); index++) {
					fCodeGenerator.appendField(
						fModifiers,
						fFieldType,
						fName,
						fInitialValue,
						fComment,
						index,
						fForEachMember);
            }  // for 
         
         } else if (qName.equals(ELEM_PARAMETER)) {
            
            fParamList.add(new MethodParam(fParamType, fParamName, null));
            
         } else if (qName.equals(ELEM_METHOD) || qName.equals(ELEM_CONSTRUCTOR)) {
            
            final boolean isCons = qName.equals(ELEM_CONSTRUCTOR);
            
				for (int index = 0; index <= getIterationCount(fForEachMember); index++) {
					fCodeGenerator.appendMethod(
						fModifiers,
						fReturnType,
						fName,
						fParamList,
						fExceptionList,
						fCodeFrag,
						fComment,
                  index,
                  fForEachMember,
						isCons);
				}  // for
         
         } else if (qName.equals(ELEM_COMMENT)) {
            
            fComment = fElementValue.toString();
            
         } else if (qName.equals(ELEM_CODE)) {
            
            fCodeFrag = fElementValue.toString();
            
         } else if (qName.equals(ELEM_REPLACE)) {
            
            fElementValue.append(' ');
            fElementValue.append(CodeReplacer.REPLACE_END);
           
         }
         
      } catch (JavaModelException e) {
         throw new SAXException(e);
      }
   }

   /**
    * @see org.xml.sax.ContentHandler#characters(char[], int, int)
    */
   public void characters(char[] ch, int start, int length) throws SAXException {
      // append this chunck to current element value
      fElementValue.append (ch, start, length);
   }

   // ------------------------------------------------------------------------- Public Methods
   
   /**
    * Create a new design pattern participant instance
    * @param roleName Participant name
    * @param memberName Name of the participant instance
    * @param packFragRoot Location of the new java file  
    */
	public synchronized MemberCodeGenerator createRoleMember(
      DesignPatternInstance model, 
      RoleNode roleNode, 
		IProgressMonitor monitor,
		String memberName,
		IPackageFragmentRoot packFragRoot, 
      IPackageFragment packFrag)
		throws CoreException {
         
      DesignPatternPlugin.debug("ENTER: createRoleMember");         

      fModel = model; 
		fRoleNode = roleNode;
      fMemberName = memberName;
      //fPackageFragRoot = packFragRoot;
      //fPackageFrag = packFrag;

		fCodeGenerator = new MemberCodeGenerator(model, roleNode, memberName, packFragRoot, packFrag);

      fMonitor = (monitor != null) ? monitor : new NullProgressMonitor();  
      
      //ICompilationUnit createdWorkingCopy = null;
      
      try {
         
         try {
            
            File codeTemplate = fModel.getCodeTemplateFile();
             
            DesignPatternPlugin.debug("CODE-TEMPLATE: " + codeTemplate.toString());         

            // validate code template before applying
            XmlDocValidator.getInstance().validate(codeTemplate);
            // apply code template
				parseCodeTemplateFile(codeTemplate);
            
            DesignPatternPlugin.debug("Code creation level 1 ...");
            
            // start type creation if is not already done
            if (!fCreationStarted) startCreation();
            
			} catch (Exception e) {
				IStatus s =
					new Status(
						IStatus.ERROR,
						DesignPatternPlugin.getPluginId(),
						IStatus.ERROR,
						e.getMessage(),
						e);
            throw new CoreException(s);
			}
         
         DesignPatternPlugin.debug("Code creation level 2 ...");
            
         fCodeGenerator.finishTypeCreation(fMonitor);
            
         DesignPatternPlugin.debug("Code creation level 3 ...");
            
      } finally {
         /*if (createdWorkingCopy != null) {
            createdWorkingCopy.destroy();
         }*/
         monitor.done();
      }

      DesignPatternPlugin.debug("Code creation level 4 ...");
            
		return fCodeGenerator;
	}
   
}
