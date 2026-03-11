package io.github.copylibs.jadx.plugin

import jadx.api.plugins.options.impl.BasePluginOptionsBuilder

class PluginOptions : BasePluginOptionsBuilder() {
	var isEnabled: Boolean = true

	override fun registerOptions() {
		boolOption(DemoPlugin.PLUGIN_ID + ".enabled")
			.description("启用插件")
			.defaultValue(true)
			.setter { v: Boolean -> isEnabled = v }
	}
}
