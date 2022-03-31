import java.lang.IllegalStateException

class VMTranslator(private val fileName: String) {

    private val parser: Parser = Parser(fileName)
    private val codeWriter: CodeWriter = CodeWriter(fileName)

    fun translate() {
        println("Translating $fileName to ${codeWriter.outputFileName}")
        while(parser.hasMoreLines()) {
            parser.advance()
            when(parser.currentCommandType) {
                Parser.CommandType.C_ARITHMETIC -> {
                    codeWriter.writeArithmetic(parser.arg1())
                }
                Parser.CommandType.C_PUSH -> {
                    codeWriter.writePushPop("push", parser.arg1(), parser.arg2().toInt())
                }
                Parser.CommandType.C_POP -> {
                    codeWriter.writePushPop("pop", parser.arg1(), parser.arg2().toInt())
                }
                Parser.CommandType.C_NOT_SUPPORTED -> {
                    println("Command not supported")
                }
            }

        }

        println("Done translating $fileName to ${codeWriter.outputFileName}")
        codeWriter.writeToFile()
    }
}