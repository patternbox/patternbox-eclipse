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

//import java.util.Map;

import org.eclipse.jdt.core.*;
//import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.*;
//import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
//import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/** 
 * <h2>How to format a piece of code ?</h2>
 * <ul><li>Create an instance of <code>CodeFormatter</code>
 * <li>Use the method <code>void format(aString)</code>
 * on this instance to format <code>aString</code>.
 * It will return the formatted string.</ul>
 */
public class CodeFormatterImpl implements TerminalTokens {

   private final CodeFormatter sCodeFormatter = ToolFactory.createCodeFormatter(null);
	//private final Map options = JavaCore.getOptions();
	
	public String format(String string, int indentLevel, int[] positions, String lineSeparator) {
		//DefaultCodeFormatter defaultCodeFormatter = new DefaultCodeFormatter(newOptions);
		TextEdit textEdit = sCodeFormatter.format(org.eclipse.jdt.core.formatter.CodeFormatter.K_UNKNOWN, string, 0, string.length(), indentLevel, lineSeparator);
		if (positions != null && textEdit != null) {
			// update positions
			TextEdit[] edits = textEdit.getChildren();
			int textEditSize = edits.length;
			int editsIndex = 0;
			int delta = 0;
			int originalSourceLength = string.length() - 1;
			if (textEditSize != 0) {
				for (int i = 0, max = positions.length; i < max; i++) {
					int currentPosition = positions[i];
					if (currentPosition > originalSourceLength) {
						currentPosition = originalSourceLength;
					}
					ReplaceEdit currentEdit = (ReplaceEdit) edits[editsIndex];
					while (currentEdit.getOffset() <= currentPosition) {
						delta += currentEdit.getText().length() - currentEdit.getLength();
						editsIndex++;
						if (editsIndex < textEditSize) {
							currentEdit = (ReplaceEdit) edits[editsIndex];
						} else {
							break;
						}
					}
					positions[i] = currentPosition + delta;
				}
			}
		}
		return org.eclipse.jdt.internal.core.util.Util.editedString(string, textEdit);
	}	
}
