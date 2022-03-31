import java.io.File
import java.lang.IndexOutOfBoundsException

class Parser(fileName: String) {

    enum class INSTRUCTION_TYPE {
        A_INSTRUCTION, C_INSTRUCTION, L_INSTRUCTION, NO_INSTRUCTION
    }

    private lateinit var currentInstructionType: INSTRUCTION_TYPE
    private var currentLineNumber = 0
    private var currentSymbolNumber = 0
    private var currentLine = ""
    private val symbolMap = SymbolMap()
    private val lineList: List<String> = File(fileName).readLines()

    init {
        fillSymbolTableLabels()
    }

    private fun fillSymbolTableLabels() {
        currentSymbolNumber = 0

        for (line in lineList) {
            val trimmedLine = line.trim()
//            println("?? $trimmedLine, symbol num: $currentSymbolNumber")

            when {
                isAInstruction(trimmedLine) -> {
                    currentSymbolNumber++
                }
                isCInstruction(trimmedLine) -> {
                    currentSymbolNumber++
                }
                isLInstruction(trimmedLine) -> {
                    val symbol = trimmedLine.removePrefix("(").removeSuffix(")")
                    symbolMap.addEntry(symbol, currentSymbolNumber)
                    continue
                }
            }
        }

//        println("fillSymbolTableLabels(): ${symbolMap.toString()}")
        currentSymbolNumber = 16
    }

    fun hasMoreLines(): Boolean {
        return currentLineNumber < lineList.size
    }

    fun advance() {
        parseLine()

        if (hasMoreLines()) {
            currentLineNumber++
        }
    }

    private fun parseLine() {
        currentLine = lineList[currentLineNumber].trim()
//        println("line: $currentLine, line number: $currentLineNumber")
        currentInstructionType = when {
            isCInstruction(currentLine) -> {
                currentLine = currentLine.substringBeforeLast("//").trim()
                INSTRUCTION_TYPE.C_INSTRUCTION
            }
            isAInstruction(currentLine) -> {
                currentLine = currentLine.substringBeforeLast("//").trim()
                INSTRUCTION_TYPE.A_INSTRUCTION
            }
            isLInstruction(currentLine) -> {
                INSTRUCTION_TYPE.L_INSTRUCTION
            }
            else -> {
                INSTRUCTION_TYPE.NO_INSTRUCTION
            }
        }
    }

    fun getCurrentInstructionType(): INSTRUCTION_TYPE {
        return currentInstructionType
    }

    fun symbol(): String {
        val symbol = currentLine.substringAfterLast("@")
        val symbolToNum = convertToNum(symbol)
        if (symbolToNum != null) {
            return symbolToNum.toString()
        }

        var address = symbolMap.getAddress(symbol)
        if (address == null) {
            address = currentSymbolNumber
            currentSymbolNumber++

            symbolMap.addEntry(symbol, address)
        }

        if (address > 32767) {
            throw IndexOutOfBoundsException("MEMORY OUT OF BOUNDS FOR HACK CPU")
        }

//        println("CURRENT SYMBOL: $symbol, address: $address")
        return address.toString()
    }

    private fun convertToNum(word: String): Int? {
        for (ch in word) {
            if (!ch.isDigit()) {
                return null
            }
        }

        return word.toInt()
    }

    fun dest(): String {
        return currentLine.substringBeforeLast("=", "")
    }

    fun comp(): String {
        return currentLine.substringAfterLast("=").substringBeforeLast(";")
    }

    fun jump(): String {
        return currentLine.substringAfterLast(";", "")
    }

    private fun isCInstruction(line: String): Boolean {
        return !isAInstruction(line) &&
                !isLInstruction(line) &&
                !line.startsWith("//") &&
                line.isNotEmpty()
    }

    private fun isAInstruction(line: String): Boolean {
        return line.startsWith('@')
    }

    private fun isLInstruction(line: String): Boolean {
        return line.startsWith("(") && line.endsWith(")")
    }
}