import java.io.File
import java.io.FileWriter
import java.lang.IllegalArgumentException
import java.lang.StringBuilder
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter
import JackTokenizer.TokenType
import JackTokenizer.KeywordType

class CompilationEngine(inputFileName: String) {
    private val outputFileName = "${inputFileName.substringBeforeLast(".")}(0).xml"

    private val outputFactory: XMLOutputFactory =
        XMLOutputFactory.newFactory()

    private var fileWriter: FileWriter? = null
    private var xmlStreamWriter: XMLStreamWriter? = null
    private var depth = 0
    private val jackTokenizer = JackTokenizer(inputFileName)
    private val classSymbolTable = SymbolTableHack()
    private val subroutineSymbolTable = SymbolTableHack()

    private var name: String = ""
    private var type: String = ""
    private var kind: SymbolTableHack.Kind = SymbolTableHack.Kind.NO_KIND

    private var className: String = ""

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
        className = jackTokenizer.getCurrentToken()
        processIdentifier()
        processSymbol('{')
        while (isClassVarDec()) {
            compileClassVarDec()
        }
        while (isSubroutine()) {
            compileSubroutine()
        }
        processSymbol('}')
        depth--
        xmlStreamWriter?.writeEndElement()
    }

    private fun compileClassVarDec() {
        writeStartElement("classVarDec")
        depth++

        val keyword = jackTokenizer.keyWord()
        if (keyword == KeywordType.STATIC) {
            kind = SymbolTableHack.Kind.STATIC
        } else if (keyword == KeywordType.FIELD) {
            kind = SymbolTableHack.Kind.FIELD
        }
        processKeyword(setOf(KeywordType.FIELD, KeywordType.STATIC))
        compileDec(kind)
        depth--
        writeEndElement()
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

    private fun compileSubroutine() {
        writeStartElement("subroutineDec")
        depth++
        subroutineSymbolTable.reset()
        val keywordType = jackTokenizer.keyWord()
        if (keywordType == KeywordType.METHOD) {
            subroutineSymbolTable.define("this", className, SymbolTableHack.Kind.ARG)
        }
        processKeyword(setOf(KeywordType.CONSTRUCTOR, KeywordType.METHOD, KeywordType.FUNCTION))
        if (jackTokenizer.tokenType() == TokenType.KEYWORD) {
            processKeyword(setOf(KeywordType.VOID))
        } else {
            compileType()
        }
//        val subroutineName = jackTokenizer.getCurrentToken()
        processIdentifier()
        processSymbol('(')
        compileParameterList()
        processSymbol(')')
        compileSubroutineBody()
        depth--
        writeEndElement()
//        println("$subroutineName symbol table $subroutineSymbolTable")
    }

    private fun compileType() {
        if (jackTokenizer.tokenType() == TokenType.IDENTIFIER) {
            processIdentifier()
        } else {
            processKeyword(setOf(KeywordType.INT, KeywordType.CHAR, KeywordType.BOOLEAN))
        }
    }

    private fun isType(): Boolean {
        return jackTokenizer.tokenType() == TokenType.IDENTIFIER ||
                setOf("int", "char", "boolean").contains(jackTokenizer.getCurrentToken())
    }

    private fun compileParameterList() {
        writeStartElement("parameterList")
        depth++
        kind = SymbolTableHack.Kind.ARG
        compileTypeList()
        depth--
        writeEndElement()
    }

    private fun compileTypeList() {
        if (!isType()) {
            return
        }
        type = jackTokenizer.getCurrentToken()
        compileType()
        processIdentifier() //var name
        subroutineSymbolTable.define(name, type, kind)
        while (jackTokenizer.tokenType() == TokenType.SYMBOL
            && jackTokenizer.symbol() == ','
        ) {
            processSymbol(',')
            type = jackTokenizer.getCurrentToken()
            compileType()
            name = jackTokenizer.getCurrentToken()
            processIdentifier() //var name=
            subroutineSymbolTable.define(name, type, kind)
        }
    }

    private fun compileSubroutineBody() {
        writeStartElement("subroutineBody")
        depth++
        processSymbol('{')
        while (jackTokenizer.tokenType() == TokenType.KEYWORD &&
            jackTokenizer.keyWord() == KeywordType.VAR
        ) {
            compileVarDec()
        }
        compileStatements()
        processSymbol('}')
        depth--
        writeEndElement()
    }

    private fun compileVarDec() {
        writeStartElement("varDec")
        depth++
        processKeyword(setOf(KeywordType.VAR))
        compileDec(SymbolTableHack.Kind.VAR)
        depth--
        writeEndElement()
    }


    private fun compileDec(kind: SymbolTableHack.Kind) {
        var symbols = classSymbolTable
        if (kind == SymbolTableHack.Kind.ARG || kind == SymbolTableHack.Kind.VAR) {
            symbols = subroutineSymbolTable
        }

        type = jackTokenizer.getCurrentToken()
        compileType()
        name = jackTokenizer.getCurrentToken()
        processIdentifier() //var name
        symbols.define(name, type, kind)

        while (jackTokenizer.tokenType() == TokenType.SYMBOL
            && jackTokenizer.symbol() == ','
        ) {
            processSymbol(',')
            name = jackTokenizer.getCurrentToken()
            processIdentifier() //var name
            symbols.define(name, type, kind)
        }
        println(classSymbolTable.toString())
        processSymbol(';')
    }

    private fun compileStatements() {
        writeStartElement("statements")
        depth++
        while (isStatement()) {
            when (jackTokenizer.keyWord()) {
                KeywordType.LET -> compileLet()
                KeywordType.IF -> compileIf()
                KeywordType.WHILE -> compileWhile()
                KeywordType.DO -> compileDo()
                KeywordType.RETURN -> compileReturn()
                else -> {
                    println("Syntax error statement not support")
                }
            }
        }
        depth--
        writeEndElement()
    }

    private fun isStatement(): Boolean {
        val statementKeywordSet = setOf(
            KeywordType.LET,
            KeywordType.IF,
            KeywordType.WHILE,
            KeywordType.DO,
            KeywordType.RETURN
        )

        return jackTokenizer.tokenType() == TokenType.KEYWORD &&
                statementKeywordSet.contains(jackTokenizer.keyWord())
    }

    private fun compileLet() {
        writeStartElement("letStatement")
        depth++
        processKeyword(setOf(KeywordType.LET))
        processIdentifier()
        if (jackTokenizer.getCurrentToken() == "[") {
            processSymbol('[')
            compileExpression()
            processSymbol(']')
        }
        processSymbol('=')
        compileExpression()
        processSymbol(';')
        depth--
        writeEndElement()
    }

    private fun compileIf() {
        writeStartElement("ifStatement")
        depth++
        processKeyword(setOf(KeywordType.IF))
        processSymbol('(')
        compileExpression()
        processSymbol(')')
        processSymbol('{')
        compileStatements()
        processSymbol('}')
        var elseCount = 0
        while (isElse()) {
            if (elseCount > 1) {
                println("Syntax error. Else statement can only appear 0 or 1 times")
                return
            }

            processKeyword(setOf(KeywordType.ELSE))
            processSymbol('{')
            compileStatements()
            processSymbol('}')
            elseCount++
        }
        depth--
        writeEndElement()
    }

    private fun isElse(): Boolean =
        jackTokenizer.getCurrentToken() == "else"

    private fun compileWhile() {
        writeStartElement("whileStatement")
        depth++
        processKeyword(setOf(KeywordType.WHILE))
        processSymbol('(')
        compileExpression()
        processSymbol(')')
        processSymbol('{')
        compileStatements()
        processSymbol('}')
        depth--
        writeEndElement()
    }

    private fun compileDo() {
        writeStartElement("doStatement")
        depth++
        processKeyword(setOf(KeywordType.DO))
        compileSubroutineCall()
        processSymbol(';')
        depth--
        writeEndElement()
    }

    private fun compileArraySubscript() {
        processSymbol('[')
        compileExpression()
        processSymbol(']')
    }

    private fun compileSubroutineCall() {
        processIdentifier()
        if (isSymbol('.')) {
            processSymbol('.')
            processIdentifier()
            processSymbol('(')
            compileExpressionList()
            processSymbol(')')
        } else {
            processSymbol('(')
            compileExpressionList()
            processSymbol(')')
        }
    }

    private fun compileSubroutineCallTerm() {
        if (isSymbol('.')) {
            processSymbol('.')
            processIdentifier()
        }
        processSymbol('(')
        compileExpressionList()
        processSymbol(')')

    }

    private fun compileReturn() {
        writeStartElement("returnStatement")
        depth++
        processKeyword(setOf(KeywordType.RETURN))
        compileExpression()
        processSymbol(';')
        depth--
        writeEndElement()
    }

    private fun compileExpression() {
        if (!isTerm()) {
            return
        }

        writeStartElement("expression")
        depth++
        compileTerm()
        while (isOp()) {
            processSymbol(
                setOf(
                    '+',
                    '-',
                    '*',
                    '/',
                    '&',
                    '|',
                    '<',
                    '>',
                    '='
                )
            )
            compileTerm()
        }
        depth--
        writeEndElement()
    }

    private fun compileTerm() {
        writeStartElement("term")
        depth++
        val tokenType = jackTokenizer.tokenType()
        when {
            tokenType == TokenType.STRING_CONST -> {
                processStringVal()
            }
            tokenType == TokenType.INT_CONST -> {
                processIntVal()
            }
            isKeywordConstant(tokenType) -> {
                processKeyword(
                    setOf(
                        KeywordType.TRUE,
                        KeywordType.FALSE,
                        KeywordType.NULL,
                        KeywordType.THIS
                    )
                )
            }
            isTermExpression(tokenType) -> {
                processSymbol('(')
                compileExpression()
                processSymbol(')')
            }
            isUnaryOp(tokenType) -> {
                processSymbol(setOf('-', '~'))
                compileTerm()
            }
            isVarName() -> {
                processIdentifier()
                val currentToken = jackTokenizer.getCurrentToken()
                if (currentToken == "[") {
                    compileArraySubscript()
                } else if (currentToken == "(" || currentToken == ".") {
                    compileSubroutineCallTerm()
                }
            }
        }

        depth--
        writeEndElement()
    }

    private fun isVarName(): Boolean {
        val tokenType = jackTokenizer.tokenType()
        return tokenType == TokenType.IDENTIFIER
    }

    private fun isOp(): Boolean {
        if (jackTokenizer.tokenType() != TokenType.SYMBOL)
            return false

        val symbol = jackTokenizer.symbol()
        return symbol == '+' || symbol == '+' ||
                symbol == '-' ||
                symbol == '*' ||
                symbol == '/' ||
                symbol == '&' ||
                symbol == '|' ||
                symbol == '<' ||
                symbol == '>' ||
                symbol == '='
    }

    private fun compileExpressionList() {
        writeStartElement("expressionList")
        depth++
        if (isTerm()) {
            compileExpression()
            while (isSymbol(',')) {
                processSymbol(',')
                compileExpression()
            }
        }
        depth--
        writeEndElement()
    }

    private fun isTerm(): Boolean {
        val tokenType = jackTokenizer.tokenType()
        return tokenType == TokenType.INT_CONST ||
                tokenType == TokenType.STRING_CONST ||
                isKeywordConstant(tokenType) ||
                tokenType == TokenType.IDENTIFIER ||
                isTermExpression(tokenType) ||
                isUnaryOp(tokenType)
    }

    private fun isTermExpression(tokenType: TokenType): Boolean {
        return tokenType == TokenType.SYMBOL &&
                jackTokenizer.symbol() == '('
    }

    private fun isUnaryOp(tokenType: TokenType): Boolean {
        if (tokenType != TokenType.SYMBOL)
            return false

        val symbol = jackTokenizer.symbol()
        return symbol == '-' || symbol == '~'
    }

    private fun isKeywordConstant(tokenType: TokenType): Boolean {
        if (tokenType != TokenType.KEYWORD)
            return false

        val keyWord = jackTokenizer.keyWord()
        return keyWord == KeywordType.TRUE ||
                keyWord == KeywordType.FALSE ||
                keyWord == KeywordType.NULL ||
                keyWord == KeywordType.THIS
    }

    private fun writeStartElement(str: String) {
        writeTab(depth)
        xmlStreamWriter?.writeStartElement(str)
        xmlStreamWriter?.writeCharacters(System.lineSeparator())
    }

    private fun writeEndElement() {
        writeTab(depth)
        xmlStreamWriter?.writeEndElement()
        xmlStreamWriter?.writeCharacters(System.lineSeparator())
    }

    private fun isSymbol(symbol: Char) =
        jackTokenizer.tokenType() == TokenType.SYMBOL &&
                jackTokenizer.symbol() == symbol

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

    private fun processSymbol(symbolSet: Set<Char>) {
        if (jackTokenizer.tokenType() == TokenType.SYMBOL &&
            symbolSet.contains(jackTokenizer.symbol())
        ) {
            writeTokenToFile(TokenType.SYMBOL, jackTokenizer.symbol().toString())
        } else {
            printSyntaxError(symbolSet.toString())
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

    private fun processIntVal() {
        if (jackTokenizer.tokenType() == TokenType.INT_CONST) {
            writeTokenToFile(TokenType.INT_CONST, jackTokenizer.intVal().toString())
        } else {
            printSyntaxError("int value")
        }
        advanceToken()
    }

    private fun processStringVal() {
        if (jackTokenizer.tokenType() == TokenType.STRING_CONST) {
            writeTokenToFile(TokenType.STRING_CONST, jackTokenizer.stringVal())
        } else {
            printSyntaxError("string value")
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

    private fun writeTokenToFile(tokenType: TokenType, token: String) {
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
        val sb = StringBuilder()
        repeat(depth) {
            sb.append("  ")
        }
        xmlStreamWriter?.writeCharacters(sb.toString())
    }

    fun writeToFile() {
        xmlStreamWriter?.flush()
        xmlStreamWriter?.close()
        println("Compiled parse tree to $outputFileName")
        jackTokenizer.writeToXmlFile()
    }
}

fun main() {
    val compilationEngine =
        CompilationEngine("/Users/arturokuang/Downloads/nand2tetris/projects/10/Square/Main.jack")
    compilationEngine.compileClass()
    compilationEngine.writeToFile()
}