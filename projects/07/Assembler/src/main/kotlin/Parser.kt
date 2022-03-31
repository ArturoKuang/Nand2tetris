import java.io.File
import java.lang.IllegalStateException

class Parser(inputFileName: String) {

    enum class CommandType {
        C_ARITHMETIC, C_PUSH, C_POP, C_LABEL,
        C_GOTO, C_IF, C_FUNCTION, C_RETURN, C_CALL, C_NOT_SUPPORTED
    }

    private val lines = File(inputFileName).readLines()

    var currentCommandType: CommandType = CommandType.C_NOT_SUPPORTED
    private var currentLineNum: Int = 0
    private var currentWordList: List<String> = emptyList()

    fun advance() {
        parseLine(lines[currentLineNum])

        if (hasMoreLines()) {
            currentLineNum++
        }
    }

    private fun parseLine(line: String) {
        if (line.isBlank() || line.isEmpty() || line.startsWith("//")) {
            return
        }

        currentWordList = line.split(" ")
        currentCommandType = when (currentWordList[0]) {
            "push" -> CommandType.C_PUSH
            "pop" -> CommandType.C_POP
            "add" -> CommandType.C_ARITHMETIC
            "sub" -> CommandType.C_ARITHMETIC
            "neg" -> CommandType.C_ARITHMETIC
            "eq" -> CommandType.C_ARITHMETIC
            "gt" -> CommandType.C_ARITHMETIC
            "lt" -> CommandType.C_ARITHMETIC
            "and" -> CommandType.C_ARITHMETIC
            "or" -> CommandType.C_ARITHMETIC
            "not" -> CommandType.C_ARITHMETIC
            else -> CommandType.C_NOT_SUPPORTED
        }
    }

    fun hasMoreLines(): Boolean {
        return currentLineNum < lines.size
    }

    fun arg1(): String {
        if (currentCommandType == CommandType.C_RETURN) {
            throw IllegalStateException("arg1 can not be called when command type is set to return")
        }

        return currentWordList[1]
    }

    fun arg2(): String {
        when (currentCommandType) {
            CommandType.C_PUSH -> {
                return currentWordList[2]
            }
            CommandType.C_POP -> {
                return currentWordList[2]
            }
            CommandType.C_FUNCTION -> {
                return currentWordList[2]
            }
            CommandType.C_CALL -> {
                return currentWordList[2]
            }
            else -> {
                throw IllegalStateException("arg2 can only be called for push, pop, function, call commands")
            }
        }
    }


}