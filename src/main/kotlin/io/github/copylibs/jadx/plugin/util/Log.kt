package io.github.copylibs.jadx.plugin.util

import org.slf4j.LoggerFactory

object Log {
	private val log = LoggerFactory.getLogger(Log::class.java)

	fun logD(msg: String) = log.debug(msg)

	fun logI(msg: String) = log.info(msg)

	fun logW(msg: String) = log.warn(msg)

	fun logE(msg: String) = log.error(msg)
}
