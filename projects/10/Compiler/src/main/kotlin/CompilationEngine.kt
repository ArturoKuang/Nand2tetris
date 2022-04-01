import java.io.File
import java.io.FileWriter
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter
import JackTokenizer.TokenType
import JackTokenizer.KeywordType

class CompilationEngine(private val inputFileName: String) {
    val outputFileName = "${inputFileName.substringBeforeLast(".")}(0).xml"

    private val outputFactory: XMLOutputFactory =
        XMLOutputFactory.newFactory()

    private var fileWriter: FileWriter? = null

    private var xmlStreamWriter: XMLStreamWriter? = null

    private var depth = 0

    private val jackTokenizer = JackTokenizer(inputFileName)

    init {
        File(outputFileName).createNewFile()
        fileWriter = FileWriter(outputFileName)
        xmlStreamWriter = outputFactory.createXMLStreamWriter(fileWriter)
    }


    fun compileClass() {
        advanceToken()
        writeStartElement("class")
        depth++
        processKeyword(setOf(KeywordType.CLASS))
        processIdentifier()
        processSymbol('{')
        while (isClassVarDec()) {
            compileClassVarDec()
        }
        while (isSubroutine()) {
            compileSubroutine()
        }
        depth--
//        processSymbol('}')
//        xmlStreamWriter?.writeEndElement()
    }

    fun compileClassVarDec() {
        writeStartElement("classVarDec")
        depth++
        processKeyword(setOf(KeywordType.FIELD, KeywordType.STATIC))
        compileDec()
        depth--
        writeEndElement()
    }

    fun compileDec() {
        compileTypeClassVar()
        processIdentifier() //var name
        while (jackTokenizer.tokenType() == TokenType.SYMBOL
            && jackTokenizer.symbol() == ','
        ) {
            processSymbol(',')
            processIdentifier() //var name
        }
        processSymbol(';')
    }

    private fun compileTypeClassVar() {
        if (jackTokenizer.tokenType() == TokenType.IDENTIFIER) {
            processIdentifier()
        } else {
            processKeyword(setOf(KeywordType.INT, KeywordType.CHAR, KeywordType.BOOLEAN))
        }
    }

    private fun isClassVarDec(): Boolean {
        val set = setOf(KeywordType.FIELD, KeywordType.STATIC)
        return jackTokenizer.tokenType() == TokenType.KEYWORD &&
                set.contains(jackTokenizer.keyWord())
    }

    private fun isSubroutine(): Boolean {
        val set = setOf(KeywordType.FUNCTION, KeywordType.CONSTRUCTOR, KeywordType.METHOD)
        return jackTokenizer.tokenType() == TokenType.KEYWORD &&
                set.contains(jackTokenizer.keyWord())
    }

    fun compileSubroutine() {
        depth++

        depth--
    }

    fun compileParameterList() {
        depth++

        depth--
    }

    fun compileSubroutineBody() {
        depth++

        depth--
    }

    fun compileVarDec() {
        depth++

        depth--
    }

    fun compileStatements() {
        depth++

        depth--
    }

    fun compileLet() {
        depth++

        depth--
    }

    fun compileIf() {
        depth++

        depth--
    }

    fun compileWhile() {
        depth++
        writeStartElement("whileStatement")
        processKeyword(setOf(KeywordType.WHILE))
        processSymbol('(')
        compileExpression()
        processSymbol(')')
        processSymbol('{')
        compileStatements()
        processSymbol('}')
        xmlStreamWriter?.writeEndElement()
        depth--
    }

    fun writeStartElement(str: String) {
        writeTab(depth)
        xmlStreamWriter?.writeStartElement(str)
        xmlStreamWriter?.writeCharacters(System.lineSeparator())
    }

    fun writeEndElement() {
        writeTab(depth)
        xmlStreamWriter?.writeEndElement()
    }

    fun compileDo() {
        depth++

        depth--
    }

    fun compileReturn() {
        depth++

        depth--
    }

    fun compileExpression() {
        depth++

        depth--
    }

    fun compileTerm() {
        depth++

        depth--
    }

    fun compileExpressionList() {
        depth++

        depth--
    }

    private fun processKeyword(keywordTypeSet: Set<KeywordType>) {
        if (jackTokenizer.tokenType() == TokenType.KEYWORD &&
            keywordTypeSet.contains(jackTokenizer.keyWord())
        ) {
            writeTokenToFile(TokenType.KEYWORD, jackTokenizer.getCurrentToken())
        } else {
            printSyntaxError(keywordTypeSet.toString())
        }
        advanceToken()
    }

    private fun processSymbol(symbol: Char) {
        if (jackTokenizer.tokenType() == TokenType.SYMBOL &&
            jackTokenizer.symbol() == symbol
        ) {
            writeTokenToFile(TokenType.SYMBOL, symbol.toString())
        } else {
            printSyntaxError(symbol.toString())
        }
        advanceToken()
    }

    private fun processIdentifier() {
        if (jackTokenizer.tokenType() == TokenType.IDENTIFIER) {
            writeTokenToFile(TokenType.IDENTIFIER, jackTokenizer.identifier())
        } else {
            printSyntaxError("identifier")
        }
        advanceToken()
    }

    private fun processIntVal(intVal: Int) {
        if (jackTokenizer.tokenType() == TokenType.INT_CONST &&
            jackTokenizer.intVal() == intVal
        ) {
            writeTokenToFile(TokenType.INT_CONST, intVal.toString())
        } else {
            printSyntaxError(intVal.toString())
        }
        advanceToken()
    }

    private fun processStringVal(stringVal: String) {
        if (jackTokenizer.tokenType() == TokenType.STRING_CONST &&
            jackTokenizer.stringVal() == stringVal
        ) {
            writeTokenToFile(TokenType.STRING_CONST, stringVal)
        } else {
            printSyntaxError(stringVal)
        }
        advanceToken()
    }

    private fun printSyntaxError(str: String) {
        println(
            "Syntax error. token: ${jackTokenizer.getCurrentToken()}," +
                    " type: ${jackTokenizer.tokenType()}" +
                    " expected $str"
        )
    }

    private fun advanceToken() {
        if (jackTokenizer.hasMoreTokens()) {
            jackTokenizer.advance()
        } else {
            println("Syntax error. Ran out of tokens")
        }
    }

    private fun writeTokenToFile(tokenType: JackTokenizer.TokenType, token: String) {
        when (tokenType) {
            TokenType.KEYWORD -> writeToElementXml("keyword", token)
            TokenType.IDENTIFIER -> writeToElementXml("identifier", token)
            TokenType.SYMBOL -> writeToElementXml("symbol", token)
            TokenType.INT_CONST -> writeToElementXml("integerConstant", token)
            TokenType.STRING_CONST -> writeToElementXml("stringConstant", token)
            else -> {
                throw  IllegalArgumentException("token type not supported $tokenType")
            }
        }
    }

    private fun writeToElementXml(elementName: String, data: String) {
        writeTab(depth)
        xmlStreamWriter?.writeStartElement(elementName)
        xmlStreamWriter?.writeCharacters(" $data ")
        xmlStreamWriter?.writeEndElement()
        xmlStreamWriter?.writeCharacters(System.lineSeparator())
    }

    private fun writeTab(depth: Int) {
        var sb = StringBuilder()
        repeat(depth) {
            sb.append("\t")
        }
        xmlStreamWriter?.writeCharacters(sb.toString())
    }

    fun writeToFile() {
        xmlStreamWriter?.flush()
        xmlStreamWriter?.close()
        println("Writing xml to $outputFileName")
        jackTokenizer.writeToXmlFile()
    }
}

fun main() {
    val compilationEngine = CompilationEngine("/Users/arturokuang/Downloads/nand2tetris/projects/10/Square/Square.jack")
    compilationEngine.compileClass()
    compilationEngine.writeToFile()
}