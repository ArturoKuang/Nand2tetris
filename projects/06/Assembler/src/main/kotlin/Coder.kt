import java.lang.IllegalArgumentException
import java.lang.StringBuilder

class Coder {
    private val jumpMap = mapOf(
        "JGT" to "001",
        "JEQ" to "010",
        "JGE" to "011",
        "JLT" to "100",
        "JNE" to "101",
        "JLE" to "110",
        "JMP" to "111"
    )

    private val compMap = mapOf(
        "0" to "0101010",
        "1" to "0111111",
        "-1" to "0111010",
        "D" to "0001100",
        "A" to "0110000",
        "M" to "1110000",
        "!D" to "0001101",
        "!A" to "0110001",
        "!M" to "1110001",
        "-D" to "0001111",
        "-A" to "0110011",
        "-M" to "1110011",
        "D+1" to "0011111",
        "A+1" to "0110111",
        "M+1" to "1110111",
        "D-1" to "0001110",
        "A-1" to "0110010",
        "M-1" to "1110010",
        "D+A" to "0000010",
        "D+M" to "1000010",
        "D-A" to "0010011",
        "D-M" to "1010011",
        "A-D" to "0000111",
        "M-D" to "1000111",
        "D&A" to "0000000",
        "D&M" to "1000000",
        "D|A" to "0010101",
        "D|M" to "1010101"
    )

    fun dest(line: String): String {
        val sb = StringBuilder("000")
        for (ch in line) {
            when (ch) {
                'M' -> sb[2] = '1'
                'D' -> sb[1] = '1'
                'A' -> sb[0] = '1'
            }
        }

        return sb.toString()
    }

    fun comp(line: String): String {
        return compMap[line] ?: throw IllegalArgumentException("Not a valid comp: $line")
    }

    fun jump(line: String): String {
        return jumpMap[line] ?: "000"
    }
}







