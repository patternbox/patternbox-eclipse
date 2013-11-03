package com.patternbox.commons.xml;

import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

import com.patternbox.eclipse.model.ManifestXmlHandler;

public class FormatAuthorTest {

	@Test
	public void testGetAuthor() throws Exception {
		File manifestFile = new File(
				"D:/java.patternbox/eclipse-plugin/patternbox/pattern.mf/abstractfactory.xml");
		ManifestXmlHandler handler = new ManifestXmlHandler();
		handler.parseManifestFile(manifestFile);
		assertNotNull(handler.getAuthor());
	}
}
