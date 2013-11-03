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

import java.net.URL;

import org.eclipse.jface.resource.*;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.*;

import com.patternbox.eclipse.model.*;

/**
 * The class <code>ViewLabelProvider</code>represents the layout of a TreeViewer object
 *
 * @author Dirk Ehms, <a href="http://www.patternbox.com">www.patternbox.com</a>
 */
public class ImageProvider extends LabelProvider {

   private static final String NAME_PREFIX = DesignPatternPlugin.getPluginId() + ".";

   private final static URL BASE_URL = DesignPatternPlugin.getPluginPathAsURL();

   private static ImageRegistry PLUGIN_REGISTRY;

   private final static String ICONS_PATH = "icons/";

   private static final ImageDescriptor DESC_FOLDER = create(ICONS_PATH, "fldr_obj.gif");
   private static final ImageDescriptor DESC_ROLE_FOLDER = create(ICONS_PATH, "role_folder.gif");
   private static final ImageDescriptor DESC_DISABLED_ROLE_FOLDER = create(ICONS_PATH, "role_folder2.gif");
   private static final ImageDescriptor DESC_MEMBER = create(ICONS_PATH, "jcu_obj.gif");
   private static final ImageDescriptor DESC_DISABLED_MEMBER = create(ICONS_PATH, "jcu_obj2.gif");

   public static final String IMG_FOLDER = NAME_PREFIX + "IMG_FOLDER";
   public static final String IMG_ROLE_FOLDER = NAME_PREFIX + "IMG_ROLE_FOLDER";
   public static final String IMG_DISABLED_ROLE_FOLDER = NAME_PREFIX + "IMG_DISABLED_ROLE_FOLDER";
   public static final String IMG_MEMBER = NAME_PREFIX + "IMG_MEMBER";
   public static final String IMG_DISABLED_MEMBER = NAME_PREFIX + "IMG_DISABLED_MEMBER";

   private static final void initialize() {
      PLUGIN_REGISTRY = new ImageRegistry();
      manage(IMG_FOLDER, DESC_FOLDER);
      manage(IMG_ROLE_FOLDER, DESC_ROLE_FOLDER);
      manage(IMG_DISABLED_ROLE_FOLDER, DESC_DISABLED_ROLE_FOLDER);
      manage(IMG_MEMBER, DESC_MEMBER);
      manage(IMG_DISABLED_MEMBER, DESC_DISABLED_MEMBER);
   }

   private static ImageDescriptor create(String prefix, String name) {
      return ImageDescriptor.createFromURL(makeImageURL(prefix, name));
   }

   private static Image manage(String key, ImageDescriptor desc) {
      Image image = desc.createImage();
      PLUGIN_REGISTRY.put(key, image);
      return image;
   }

   private static URL makeImageURL(String prefix, String name) {
      String path = prefix + name;
      URL url = null;
      try {
         url = new URL(BASE_URL, path);
      } catch (Exception e) {
         return null;
      }
      return url;
   }

   private static Image get(String key) {
      if (PLUGIN_REGISTRY == null)
         initialize();
      return PLUGIN_REGISTRY.get(key);
   }

	/**
	 * Returns an image representation for a given object type
	 *
	 * @param obj  object to identify pattern or category
	 * @return image for pattern or category
	 */
	public Image getImage(Object obj) {

      if (obj instanceof CategorieNode) {
         return get(IMG_FOLDER);
      } else if (obj instanceof RoleNode && ((RoleNode) obj).isActive()) {
         return get(IMG_ROLE_FOLDER);
      } else if (obj instanceof RoleNode) {
         return get(IMG_DISABLED_ROLE_FOLDER);
      } else if (obj instanceof MemberNode && ((MemberNode) obj).isResourceAvail()) {
         return get(IMG_MEMBER);
      } else if (obj instanceof MemberNode) {
         return get(IMG_DISABLED_MEMBER);
      } else {
         return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
      }

	}

}