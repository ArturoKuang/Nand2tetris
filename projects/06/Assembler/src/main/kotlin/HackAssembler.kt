import java.io.File
import java.lang.StringBuilder

class HackAssembler(private val fileName: String) {

    val outputFileName: String =
        "${fileName.substringBeforeLast('.')}.hack"

    private val parser: Parser = Parser(fileName)
    private val coder: Coder = Coder()
    private val binaryStringBuilder: StringBuilder = StringBuilder()

    fun assemble() {
        println("------ Start Assembling: $fileName to $outputFileName")
        while (parser.hasMoreLines()) {
            parser.advance()
            if (parser.getCurrentInstructionType() == Parser.INSTRUCTION_TYPE.C_INSTRUCTION) {
                val dest = coder.dest(parser.dest())
                val comp = coder.comp(parser.comp())
                val jump = coder.jump(parser.jump())
                binaryStringBuilder.appendLine("111${comp}${dest}${jump}")
            } else if (parser.getCurrentInstructionType() == Parser.INSTRUCTION_TYPE.A_INSTRUCTION) {
                val symbolBinary = toBinary(parser.symbol().toInt(), 16)
                binaryStringBuilder.appendLine(symbolBinary)
            }
        }

        File(outputFileName).writeText(binaryStringBuilder.toString())
        println("------ Finished Assembling: $fileName to $outputFileName")
    }

    private fun toBinary(num: Int, len: Int): String {
        return String.format("%" + len + "s", num.toString(2))
            .replace(" ".toRegex(), "0")
            .replace("-", "")
    }

}