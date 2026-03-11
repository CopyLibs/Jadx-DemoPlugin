package io.github.copylibs.jadx.plugin.action

import jadx.api.*
import jadx.api.metadata.ICodeAnnotation
import jadx.api.metadata.ICodeNodeRef
import jadx.api.plugins.gui.JadxGuiContext
import jadx.api.plugins.options.JadxPluginOptions
import jadx.core.dex.instructions.args.ArgType
import jadx.core.dex.instructions.args.PrimitiveType
import jadx.core.utils.exceptions.JadxRuntimeException

class KavaRefCodeAction(
	private val guiContext: JadxGuiContext,
	private val decompiler: JadxDecompiler,
	private val options: JadxPluginOptions
) {
	fun initMenu() {
		guiContext.addPopupMenuAction(
			"复制为 KavaRef 片段",
			{ nodeRef: ICodeNodeRef ->
				when (nodeRef.annType) {
					ICodeAnnotation.AnnType.CLASS, ICodeAnnotation.AnnType.METHOD, ICodeAnnotation.AnnType.FIELD -> true
					else -> false
				}
			},
			null,
			{ nodeRef: ICodeNodeRef ->
				val node = decompiler.getJavaNodeByRef(nodeRef)
				val code = getKavaRefCode(node)
				guiContext.copyToClipboard(code)
			}
		)
	}

	private fun getKavaRefCode(javaNode: JavaNode?): String {
		return when (javaNode) {
			is JavaClass -> getClassCode(javaNode)
			is JavaMethod -> getMethodCode(javaNode)
			is JavaField -> getFieldCode(javaNode)
			else -> ""
		}
	}

	private fun getClassCode(javaNode: JavaClass): String {
		val node = javaNode.classNode
		val classRawName = node.rawName
		return """
			"$classRawName".toClass()
		""".trimIndent()
	}

	private fun getMethodCode(javaNode: JavaMethod): String {
		val node = javaNode.methodNode
		val methodReturnType = node.returnType
		val methodName = node.name
		val methodArgTypes = node.argTypes
		return if (node.isConstructor) {
			"""
				firstConstructor {
					${if (methodArgTypes.isEmpty()) "emptyParameters()" else "parameters(${methodArgTypes.joinToString(", ") { type -> getKavaRefType(type) }})"}
				}
			""".trimIndent()
		} else {
			"""
				firstMethod {
					returnType = ${getKavaRefType(methodReturnType)}
					name = "$methodName"
					${if (methodArgTypes.isEmpty()) "emptyParameters()" else "parameters(${methodArgTypes.joinToString(", ") { type -> getKavaRefType(type) }})"}
				}
			""".trimIndent()
		}
	}

	private fun getFieldCode(javaNode: JavaField): String {
		val node = javaNode.fieldNode
		val fieldType = node.type
		val fieldName = node.name
		return """
			firstField {
				type = ${getKavaRefType(fieldType)}
				name = "$fieldName"
			}
		""".trimIndent()
	}

	private fun getKavaRefType(type: ArgType): String {
		return when {
			type.isPrimitive -> when (type) {
				ArgType.BOOLEAN -> "Boolean::class"
				ArgType.BYTE -> "Byte::class"
				ArgType.CHAR -> "Char::class"
				ArgType.SHORT -> "Short::class"
				ArgType.INT -> "Int::class"
				ArgType.FLOAT -> "Float::class"
				ArgType.LONG -> "Long::class"
				ArgType.DOUBLE -> "Double::class"
				ArgType.VOID -> "Void.TYPE"
				else -> throw JadxRuntimeException("Unknown primitive type: $type")
			}

			type.isArray -> when (type.arrayElement) {
				ArgType.BOOLEAN -> "BooleanArray::class"
				ArgType.BYTE -> "ByteArray::class"
				ArgType.CHAR -> "CharArray::class"
				ArgType.SHORT -> "ShortArray::class"
				ArgType.INT -> "IntArray::class"
				ArgType.FLOAT -> "FloatArray::class"
				ArgType.LONG -> "LongArray::class"
				ArgType.DOUBLE -> "DoubleArray::class"
				else -> "ArrayClass(${getKavaRefType(type.arrayElement)})"
			}

			type.isObject && type.isGeneric -> getKavaRefType(ArgType.`object`(type.`object`))

			type.isObject && type.isGenericType -> "Any::class"

			type.isObject -> when (type) {
				PrimitiveType.BOOLEAN.boxType -> "JBoolean::class"
				PrimitiveType.BYTE.boxType -> "JByte::class"
				PrimitiveType.CHAR.boxType -> "JCharacter::class"
				PrimitiveType.SHORT.boxType -> "JShort::class"
				PrimitiveType.INT.boxType -> "JInteger::class"
				PrimitiveType.FLOAT.boxType -> "JFloat::class"
				PrimitiveType.LONG.boxType -> "JLong::class"
				PrimitiveType.DOUBLE.boxType -> "JDouble::class"
				PrimitiveType.VOID.boxType -> "JVoid::class"

				ArgType.OBJECT -> "Any::class"
				ArgType.CLASS -> "Class::class"
				ArgType.STRING -> "String::class"
				ArgType.ENUM -> "Enum::class"
				ArgType.THROWABLE -> "Throwable::class"
				ArgType.ERROR -> "Error::class"
				ArgType.EXCEPTION -> "Exception::class"
				ArgType.RUNTIME_EXCEPTION -> "RuntimeException::class"

				ArgType.`object`(List::class.java.name) -> "List::class"
				ArgType.`object`(Map::class.java.name) -> "Map::class"

				else -> "\"$type\""
			}

			else -> throw JadxRuntimeException("Unsupported type: $type")
		}
	}
}
