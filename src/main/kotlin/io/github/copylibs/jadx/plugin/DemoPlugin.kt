package io.github.copylibs.jadx.plugin

import jadx.api.plugins.JadxPlugin
import jadx.api.plugins.JadxPluginContext
import jadx.api.plugins.JadxPluginInfo
import jadx.api.plugins.JadxPluginInfoBuilder

class DemoPlugin : JadxPlugin {
	companion object {
		const val PLUGIN_ID = "Jadx-DemoPlugin"
	}

	override fun getPluginInfo(): JadxPluginInfo {
		return JadxPluginInfoBuilder.pluginId(PLUGIN_ID)
			.name("Jadx DemoPlugin")
			.description("Jadx DemoPlugin")
			.build()
	}

	override fun init(context: JadxPluginContext) {
	}
}
