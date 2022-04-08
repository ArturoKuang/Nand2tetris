import java.io.File
import java.lang.StringBuilder

class VMWriter(outputFileName: String) {

    private val file = File(outputFileName)
    private val stringBuilder = StringBuilder()

    enum class Segment {
        CONSTANT, ARGUMENT, LOCAL, STATIC,
        THIS, THAT, POINTER, TEMP
    }

    enum class ArithmeticCommand {
        ADD, SUB, NEG, EQ,
        GT, LT, AND, OR, NOT
    }

    fun writePush(segment: Segment, index: Int) {
        stringBuilder.appendLine("push ${segmentToString(segment)} $index")
    }

    fun writePop(segment: Segment, index: Int) {
        stringBuilder.appendLine("pop ${segmentToString(segment)} $index")
    }

    fun writeArithmetic(command: ArithmeticCommand) {
        when (command) {
            ArithmeticCommand.ADD -> stringBuilder.appendLine("add")
            ArithmeticCommand.SUB -> stringBuilder.appendLine("sub")
            ArithmeticCommand.NEG -> stringBuilder.appendLine("neg")
            ArithmeticCommand.EQ -> stringBuilder.appendLine("eq")
            ArithmeticCommand.GT -> stringBuilder.appendLine("gt")
            ArithmeticCommand.LT -> stringBuilder.appendLine("lt")
            ArithmeticCommand.AND -> stringBuilder.appendLine("and")
            ArithmeticCommand.OR -> stringBuilder.appendLine("or")
            ArithmeticCommand.NOT -> stringBuilder.appendLine("not")
        }
    }

    fun writeLabel(label: String) {
        stringBuilder.appendLine("label $label")
    }

    fun writeGoto(label: String) {
        stringBuilder.appendLine("goto $label")
    }

    fun writeIf(label: String) {
        stringBuilder.appendLine("if-goto $label")
    }

    fun writeCall(name: String, nArgs: Int) {
        stringBuilder.appendLine("call $name $nArgs")
    }

    fun writeFunction(name: String, nVars: Int) {
        stringBuilder.appendLine("function $name $nVars")
    }

    fun writeReturn() {
        stringBuilder.appendLine("return")
    }

    fun close() {
        file.writeText(stringBuilder.toString())
    }

    private fun segmentToString(segment: Segment): String {
        return when (segment) {
            Segment.CONSTANT -> "constant"
            Segment.ARGUMENT -> "argument"
            Segment.LOCAL -> "local"
            Segment.STATIC -> "static"
            Segment.THIS -> "this"
            Segment.THAT -> "that"
            Segment.POINTER -> "pointer"
            Segment.TEMP -> "temp"
        }
    }
}