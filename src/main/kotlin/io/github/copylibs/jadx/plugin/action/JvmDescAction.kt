package io.github.copylibs.jadx.plugin.action

import jadx.api.*
import jadx.api.metadata.ICodeAnnotation
import jadx.api.metadata.ICodeNodeRef
import jadx.api.plugins.gui.JadxGuiContext
import jadx.api.plugins.options.JadxPluginOptions
import jadx.core.dex.instructions.args.ArgType
import jadx.core.utils.exceptions.JadxRuntimeException

class JvmDescAction(
	private val guiContext: JadxGuiContext,
	private val decompiler: JadxDecompiler,
	private val options: JadxPluginOptions
) {
	fun initMenu() {
		guiContext.addPopupMenuAction(
			"复制为 Jvm 签名",
			{ nodeRef: ICodeNodeRef ->
				when (nodeRef.annType) {
					ICodeAnnotation.AnnType.CLASS, ICodeAnnotation.AnnType.METHOD, ICodeAnnotation.AnnType.FIELD -> true
					else -> false
				}
			},
			null,
			{ nodeRef: ICodeNodeRef ->
				val node = decompiler.getJavaNodeByRef(nodeRef)
				val code = getJvmDesc(node)
				guiContext.copyToClipboard(code)
			}
		)
	}

	private fun getJvmDesc(javaNode: JavaNode?): String {
		return when (javaNode) {
			is JavaClass -> getClassDesc(javaNode)
			is JavaMethod -> getMethodDesc(javaNode)
			is JavaField -> getFieldDesc(javaNode)
			else -> ""
		}
	}

	private fun getClassDesc(javaNode: JavaClass): String {
		val node = javaNode.classNode
		val classRawName = node.rawName
		return getTypeDesc(classRawName)
	}

	private fun getMethodDesc(javaNode: JavaMethod): String {
		val node = javaNode.methodNode
		val classRawName = node.declaringClass.rawName
		val methodReturnType = node.returnType
		val methodName = node.name
		val methodArgTypes = node.argTypes
		return if (node.isConstructor) {
			buildString {
				append(getTypeDesc(classRawName))
				append("->")
				append("<init>")
				append(buildString {
					append("(")
					append(methodArgTypes.joinToString("") { type -> getTypeDesc(type) })
					append(")V")
				})
			}
		} else {
			buildString {
				append(getTypeDesc(classRawName))
				append("->")
				append(methodName)
				append(buildString {
					append("(")
					append(methodArgTypes.joinToString("") { type -> getTypeDesc(type) })
					append(")")
					append(getTypeDesc(methodReturnType))
				})
			}
		}
	}

	private fun getFieldDesc(javaNode: JavaField): String {
		val node = javaNode.fieldNode
		val classRawName = node.declaringClass.rawName
		val fieldName = node.name
		val fieldType = node.type
		return buildString {
			append(getTypeDesc(classRawName))
			append("->")
			append(fieldName)
			append(":")
			append(getTypeDesc(fieldType))
		}
	}

	private fun getTypeDesc(className: String): String {
		return "L" + className.replace('.', '/') + ";"
	}

	private fun getTypeDesc(type: ArgType): String {
		return when {
			type.isPrimitive -> when (type) {
				ArgType.BOOLEAN -> "Z"
				ArgType.BYTE -> "B"
				ArgType.CHAR -> "C"
				ArgType.SHORT -> "S"
				ArgType.INT -> "I"
				ArgType.FLOAT -> "F"
				ArgType.LONG -> "J"
				ArgType.DOUBLE -> "D"
				ArgType.VOID -> "V"
				else -> throw JadxRuntimeException("Unknown primitive type: $type")
			}

			type.isArray -> "[" + getTypeDesc(type.arrayElement)

			type.isObject && type.isGeneric -> getTypeDesc(ArgType.`object`(type.`object`))

			type.isObject && type.isGenericType -> getTypeDesc(Object::class.java.name)

			type.isObject -> getTypeDesc(type.`object`)

			else -> throw JadxRuntimeException("Unsupported type: $type")
		}
	}
}
