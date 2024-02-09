/*
 * Copyright 2003-2024 The IdeaVim authors
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE.txt file or at
 * https://opensource.org/licenses/MIT.
 */

package com.adgainai.springbootapifullpath

import com.intellij.ui.IconManager
import com.intellij.ui.RoundedIcon
import com.intellij.util.IconUtil
import com.intellij.util.ImageLoader
import com.intellij.util.ui.JBImageIcon
import javax.swing.Icon

public object InlayHintsIcons {
	public val web: Icon = load("/META-INF/icon/web.svg")



	private fun load(path: String): Icon {
		return IconManager.getInstance().getIcon(path, InlayHintsIcons::class.java.classLoader)
	}

	@Suppress("UnstableApiUsage")
  public fun loadRoundImageIcon(path: String): RoundedIcon {
		return RoundedIcon(ImageLoader.loadFromResource(path, InlayHintsIcons::class.java)!!,50.0)
	}

	public fun loadImageIcon(path: String): JBImageIcon {
		return JBImageIcon(ImageLoader.loadFromResource(path, InlayHintsIcons::class.java)!!)
	}

	public fun scaleIcon(icon: Icon, scale: Float): Icon {
		return IconUtil.scale(icon, null, scale)
	}
}