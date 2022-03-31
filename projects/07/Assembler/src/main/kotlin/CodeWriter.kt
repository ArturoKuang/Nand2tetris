import java.io.File

class CodeWriter(private val inputFileName: String) {

    /**
     * Types of segments
     * argument - ARG
     * local - LCL
     * static - fileName.static#
     * constant - no memory (stack space)
     * this - THIS
     * that - THAT
     * pointer - push pointer 0 -> stack = memory[THIS],
     *           pop pointer 0 -> memory[THIS] = stack
     *           push pointer 1 -> stack = memory[THAT],
     *           pop pointer 1 -> memory[THAT] = stack
     * temp - memory[5+i], mapped from 5-12
     */
    val outputFileName = "${inputFileName.substringBeforeLast(".")}.asm"
    private var count: Int = 0;
    private val asmOutputSb = StringBuilder()
    private val arithmeticAsmMap = mutableMapOf(
        "add" to "+",
        "sub" to "-",
        "neg" to "-",
        "eq" to "=",
    )

    fun writeArithmetic(command: String) {
        println("arithmetic: $command")
    }
m
    private fun EQ(): String? {
        val n: String = nextCount()
        return """
            @SP
            AM=M-1
            D=M
            A=A-1
            D=M-D
            @EQ.true.$n
            D;JEQ
            @SP
            A=M-1
            M=0
            @EQ.after.$n
            0;JMP
            (EQ.true.$n)
            @SP
            A=M-1
            M=-1
            (EQ.after.$n)
            
            """.trimIndent()
    }

    private fun GT(): String? {
        val n: String = nextCount()
        return """
            @SP
            AM=M-1
            D=M
            A=A-1
            D=M-D
            @GT.true.$n
            
            D;JGT
            @SP
            A=M-1
            M=0
            @GT.after.$n
            0;JMP
            (GT.true.$n)
            @SP
            A=M-1
            M=-1
            (GT.after.$n)
            
            """.trimIndent()
    }

    private fun LT(): String? {
        val n: String = nextCount()
        return """
            @SP
            AM=M-1
            D=M
            A=A-1
            D=M-D
            @LT.true.$n
            D;JLT
            @SP
            A=M-1
            M=0
            @LT.after.$n
            0;JMP
            (LT.true.$n)
            @SP
            A=M-1
            M=-1
            (LT.after.$n)
            
            """.trimIndent()
    }

    private fun nextCount(): String {
        count++
        return count.toString()
    }

    fun writePushPop(command: String, segment: String, index: Int) {
        println("$command $segment[$index]")
        when (command) {
            "push" -> {
                asmOutputSb.append(pushAsm(segment, index))
            }
            "pop" -> {
                asmOutputSb.append(popAsm(segment, index))
            }
            else -> {
                throw IllegalArgumentException("writePushPop(): command is not push or pop")
            }
        }
    }

    fun writeToFile() {
        File(outputFileName).writeText(asmOutputSb.toString())
    }

    private fun pushAsm(segment: String, index: Int): String {
        val sb = StringBuilder()
        sb.appendLine("//push $segment[$index]")
        sb.appendLine(readFromSegmentAsm(segment, index))
        sb.appendLine(
            "@SP\n" +
                    "A=M\n" +
                    "M=D\n" +
                    "@SP\n" +
                    "M=M+1"
        )

        return sb.toString()
    }

    private fun popAsm(segment: String, index: Int): String {
        val sb = StringBuilder()
        sb.appendLine("//pop $segment $index")
        sb.appendLine(
            "@SP\n" +
                    "A=M\n" +
                    "D=M"
        )
        sb.appendLine(
            "@SP\n+" +
                    "M=M-1"
        )
        sb.appendLine(writeToSegmentAsm(segment, index))
        return sb.toString()
    }

    private fun writeToSegmentAsm(segment: String, index: Int): String {
        if (index < 0) {
            throw IndexOutOfBoundsException("index has to be positive..$index")
        }
        return when (segment) {
            "argument" -> {
                "${argumentAsm()}\n" +
                        writeToSegmentIndexAsm(index)
            }
            "local" -> {
                "${localAsm()}\n" +
                        writeToSegmentIndexAsm(index)
            }
            "static" -> {
                "${staticAsm(index)}\n" +
                        writeToSegmentIndexAsm(0)
            }
            "this" -> {
                "${thisAsm()}\n" +
                        writeToSegmentIndexAsm(index)
            }
            "that" -> {
                "${thatAsm()}\n" +
                        writeToSegmentIndexAsm(index)
            }
            "pointer" -> {
                "${pointerAsm(index)}\n" +
                        writeToSegmentIndexAsm(0)
            }
            "temp" -> {
                "${tempAsm(index)}\n" +
                        writeToSegmentIndexAsm(0)
            }
            else -> throw IllegalArgumentException("Not a valid segment $segment")
        }
    }

    private fun writeToSegmentIndexAsm(index: Int): String {
        val gotoAddressAsm = if (index == 0) {
            ""
        } else {
            "A=A+$index\n"
        }

        return gotoAddressAsm + "M=D"
    }

    fun readFromSegmentAsm(segment: String, index: Int): String {
        if (index < 0) {
            throw IndexOutOfBoundsException("index has to be positive..$index")
        }

        return when (segment) {
            "argument" -> {
                "${argumentAsm()}\n" +
                        readFromSegmentIndexAsm(index)
            }
            "local" -> {
                "${localAsm()}\n" +
                        readFromSegmentIndexAsm(index)
            }
            "static" -> {
                "${staticAsm(index)}\n" +
                        "D=M"
            }
            "constant" -> {
                println(constantAsm(index))
                "${constantAsm(index)}\n" +
                        "D=A"
            }
            "this" -> {
                "${thisAsm()}\n" +
                        readFromSegmentIndexAsm(index)
            }
            "that" -> {
                "${thatAsm()}\n" +
                        readFromSegmentIndexAsm(index)
            }
            "pointer" -> {
                "${pointerAsm(index)}\n" +
                        readFromSegmentIndexAsm(0)
            }
            "temp" -> {
                "${tempAsm(index)}\n" +
                        readFromSegmentIndexAsm(0)
            }
            else -> throw IllegalArgumentException("Not a valid segment $segment")
        }
    }

    private fun readFromSegmentIndexAsm(index: Int): String {
        val gotoAddressAsm = if (index == 0) {
            ""
        } else {
            "A=A+$index\n"
        }

        return gotoAddressAsm + "D=M"
    }

    private fun argumentAsm() = "@ARG"
    private fun localAsm() = "@LCL"
    private fun constantAsm(index: Int) = "@$index"
    private fun thisAsm() = "@THIS"
    private fun thatAsm() = "@THAT"
    private fun tempAsm(index: Int): String {
        if (index < 0 || index > 7) {
            throw IndexOutOfBoundsException("temp index out of bound $index. 0..7")
        }
        return "@${5 + index}"
    }

    private fun pointerAsm(index: Int): String {
        return when (index) {
            0 -> {
                thisAsm()
            }
            1 -> {
                thatAsm()
            }
            else -> {
                throw IllegalArgumentException("pointer can be 0 or 1. $index")
            }
        }
    }

    private fun staticAsm(index: Int): String {
        return "@${inputFileName.substringBeforeLast(".")}.$index"
    }
}

fun main() {
    val codeWriter = CodeWriter("Foo.vm")
    println(codeWriter.readFromSegmentAsm("argument", 0))
    println(codeWriter.readFromSegmentAsm("constant", 1))
    println(codeWriter.readFromSegmentAsm("pointer", 1))
    println(codeWriter.readFromSegmentAsm("static", 3))
    println(codeWriter.readFromSegmentAsm("temp", 0))

}
