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
package com.patternbox.eclipse.editor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.patternbox.eclipse.model.DesignPatternPool;

/**
 * Design pattern plugin manifest class
 * 
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class DesignPatternPlugin extends AbstractUIPlugin {

	public final static String DEFAULT_FILE_EXT = ".xdp"; //$NON-NLS-1$

	private final static String PLUGIN_MANIFEST = "plugin.xml"; //$NON-NLS-1$

	private final static String PLUGIN_ID = "com.patternbox.eclipse.editor"; //$NON-NLS-1$

	public final static String AUTHOR_NAME = "Dirk Ehms"; //$NON-NLS-1$

	public final static String AUTHOR_WEBSITE = "www.patternbox.com"; //$NON-NLS-1$

	// Shared instance
	private static DesignPatternPlugin sPluginInstance;

	// Resource bundle
	private ResourceBundle fResourceBundle;

	// Shared image provider
	private final LabelProvider fImageProvider;

	public DesignPatternPlugin() {
		sPluginInstance = this;
		fImageProvider = new ImageProvider();
		try {
			fResourceBundle = ResourceBundle
					.getBundle("com.patternbox.eclipse.editor.PatternboxEditorMessages"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			fResourceBundle = null;
		}
	}

	/* package */static IPath getInstallLocation() {
		return new Path(getDefault().getDescriptor().getInstallURL().getFile());
	}

	public static IWorkbenchPage getActivePage() {
		return getDefault().internalGetActivePage();
	}

	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}

	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static DesignPatternPlugin getDefault() {
		return sPluginInstance;
	}

	public static String getFormattedMessage(String key, String[] args) {
		final String text = getResourceString(key);
		return java.text.MessageFormat.format(text, (Object[]) args);
	}

	public static String getFormattedMessage(String key, String arg) {
		final String text = getResourceString(key);
		return java.text.MessageFormat.format(text, new Object[] { arg });
	}

	public static String getPluginId() {
		return PLUGIN_ID;
	}

	public ResourceBundle getResourceBundle() {
		return fResourceBundle;
	}

	public static String getResourceString(String key) {
		ResourceBundle bundle = DesignPatternPlugin.getDefault().getResourceBundle();
		if (bundle != null) {
			try {
				String bundleString = bundle.getString(key);
				// return "$"+bundleString;
				return bundleString;
			} catch (MissingResourceException e) {
				// default actions is to return key, which is OK
			}
		}
		return key;
	}

	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	private IWorkbenchPage internalGetActivePage() {
		return getWorkbench().getActiveWorkbenchWindow().getActivePage();
	}

	public static void log(IStatus status) {
		ResourcesPlugin.getPlugin().getLog().log(status);
	}

	public static void log(String message) {
		log(new Status(IStatus.INFO, getPluginId(), IStatus.INFO, message, null));
	}

	public static void debug(String message) {
		// log(new Status(IStatus.INFO, getPluginId(), IStatus.INFO, message, null));
	}

	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getPluginId(), IStatus.ERROR, message, null));
	}

	public static void logException(Throwable e, final String title, String message) {
		if (e instanceof InvocationTargetException) {
			e = ((InvocationTargetException) e).getTargetException();
		}
		IStatus status = null;
		if (e instanceof CoreException)
			status = ((CoreException) e).getStatus();
		else {
			if (message == null)
				message = e.getMessage();
			if (message == null)
				message = e.toString();
			status = new Status(IStatus.ERROR, getPluginId(), IStatus.OK, message, e);
		}
		ResourcesPlugin.getPlugin().getLog().log(status);
	}

	public static void logException(Throwable e) {
		logException(e, null, null);
	}

	public static void log(Throwable e) {
		if (e instanceof InvocationTargetException)
			e = ((InvocationTargetException) e).getTargetException();
		IStatus status = null;
		if (e instanceof CoreException)
			status = ((CoreException) e).getStatus();
		else
			status = new Status(IStatus.ERROR, getPluginId(), IStatus.OK, e.getMessage(), e);
		log(status);
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		sPluginInstance = this;
		String pluginPath = getPluginPath();
		log("Patternbox-Plugin-Path: " + pluginPath);
		DesignPatternPool.initialize(pluginPath);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		sPluginInstance = null;
		super.stop(context);
	}

	public static File getFileInPlugin(IPath path) {
		try {
			URL installURL = new URL(getDefault().getBundle().getEntry("/"), path.toString()); //$NON-NLS-1$
			URL localURL = Platform.asLocalURL(installURL);
			return new File(localURL.getFile());
		} catch (IOException e) {
			return null;
		}
	}

	public LabelProvider getImageProvider() {
		return fImageProvider;
	}

	public static URL getPluginPathAsURL() {
		return DesignPatternPlugin.getDefault().getDescriptor().getInstallURL();
	}

	public static URL getInstallURL() {
		return getDefault().getBundle().getEntry("/"); //$NON-NLS-1$
	}

	/**
	 * Returns the installation path of the plugin
	 * 
	 * @return String Plugin path
	 */
	public static String getPluginPath() {
		try {
			URL installURL = getPluginPathAsURL();
			URL pluginURL = new URL(installURL, PLUGIN_MANIFEST);
			String pluginFile = Platform.asLocalURL(pluginURL).getFile();
			String path = pluginFile.substring(0, pluginFile.length() - PLUGIN_MANIFEST.length());
			return path;
		} catch (IOException e) {
			return null;
		}
	}
}