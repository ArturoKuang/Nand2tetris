import java.io.File
import java.io.FileWriter
import java.lang.IllegalArgumentException
import java.util.regex.Pattern
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter

class JackTokenizer(inputFileName: String) {
    val outputFileName = "${inputFileName.substringBeforeLast(".")}T(0).xml"
    var isOutputFile = true

    private val inputLines: List<String>
    private val charList: List<Char>

    private var currentCharNum = 0
    private var currentChar: Char = ' '
    private var currentToken = ""
    private var currentType: TokenType = TokenType.NO_TYPE

    private val outputFactory: XMLOutputFactory =
        XMLOutputFactory.newFactory()

    private var fileWriter: FileWriter? = null

    private var xmlStreamWriter: XMLStreamWriter? = null

    private val PATTERN_NUMBER = Pattern.compile("[0-9]+")
    private val PATTERN_IDENTIFIER_AZ = Pattern.compile("[a-zA-Z]")

    private val symbolTableHack: SymbolTableHack = SymbolTableHack()

    enum class TokenType {
        KEYWORD, SYMBOL, INT_CONST, STRING_CONST, IDENTIFIER, NO_TYPE
    }

    enum class KeywordType {
        CLASS, CONSTRUCTOR, FUNCTION, METHOD, FIELD, STATIC, VAR, INT, CHAR, BOOLEAN,
        VOID, TRUE, FALSE, NULL, THIS, LET, DO, IF, ELSE, WHILE, RETURN,
    }

    private val keywordLut = mapOf(
        "class" to KeywordType.CLASS,
        "constructor" to KeywordType.CONSTRUCTOR,
        "function" to KeywordType.FUNCTION,
        "method" to KeywordType.METHOD,
        "field" to KeywordType.FIELD,
        "static" to KeywordType.STATIC,
        "var" to KeywordType.VAR,
        "int" to KeywordType.INT,
        "char" to KeywordType.CHAR,
        "boolean" to KeywordType.BOOLEAN,
        "void" to KeywordType.VOID,
        "true" to KeywordType.TRUE,
        "false" to KeywordType.FALSE,
        "null" to KeywordType.NULL,
        "this" to KeywordType.THIS,
        "let" to KeywordType.LET,
        "do" to KeywordType.DO,
        "if" to KeywordType.IF,
        "else" to KeywordType.ELSE,
        "while" to KeywordType.WHILE,
        "return" to KeywordType.RETURN
    )

    private val symbolLut = setOf(
        '{',
        '}',
        '(',
        ')',
        '[',
        ']',
        '.',
        ',',
        ';',
        '+',
        '-',
        '*',
        '/',
        '&',
        '|',
        '<',
        '>',
        '=',
        '~',
    )

    init {
        val inputText = File(inputFileName).readText()
        inputLines = removeComments(inputText).lines()
            .map { it.trim() }
            .filter { it.isNotBlank() && it.isNotEmpty() }

        charList = inputLines.flatMap { it.toList() }
        currentChar = charList[currentCharNum]
        if (isOutputFile) {
            File(outputFileName).createNewFile()
            fileWriter = FileWriter(outputFileName)
            xmlStreamWriter = outputFactory.createXMLStreamWriter(fileWriter)

            xmlStreamWriter?.writeStartElement("tokens")
            xmlStreamWriter?.writeCharacters(System.lineSeparator())
        }
    }

    fun advance() {
        currentToken = ""
        var shouldAdvanceNextChar = true
        while (currentChar == ' ') {
            advanceChar()
        }

        when {
            PATTERN_NUMBER.matcher(currentChar.toString()).matches() -> {
                do {
                    currentToken += currentChar
                    advanceChar()
                } while (PATTERN_NUMBER.matcher(currentChar.toString()).matches())
                writeTokenToFile(TokenType.INT_CONST, currentToken)
                currentType = TokenType.INT_CONST
                shouldAdvanceNextChar = false
            }
            symbolLut.contains(currentChar) -> {
                currentToken = currentChar.toString()
                writeTokenToFile(TokenType.SYMBOL, currentToken)
                currentType = TokenType.SYMBOL
            }
            currentChar == '"' -> {
                advanceChar()
                while (currentChar != '"' && hasMoreTokens()) {
                    currentToken += currentChar
                    advanceChar()
                }
                writeTokenToFile(TokenType.STRING_CONST, currentToken)
                currentType = TokenType.STRING_CONST
            }
            PATTERN_IDENTIFIER_AZ.matcher(currentChar.toString()).matches() -> {
                do {
                    currentToken += currentChar
                    advanceChar()
                } while (PATTERN_IDENTIFIER_AZ.matcher(currentChar.toString()).matches())

                val keyword = keywordLut[currentToken]
                currentType = if (keyword != null) {
                    TokenType.KEYWORD
                } else {
                    TokenType.IDENTIFIER
                }

                writeTokenToFile(currentType, currentToken)
                shouldAdvanceNextChar = false
            }
        }

        if (shouldAdvanceNextChar)
            advanceChar()
    }

    private fun advanceChar() {
        currentCharNum++
        if (currentCharNum < charList.size - 1)
            currentChar = charList[currentCharNum]
    }

    fun getCurrentToken(): String {
        return currentToken
    }

    fun hasMoreTokens(): Boolean {
        return currentCharNum < charList.size
    }

    fun tokenType(): TokenType {
        return currentType
    }

    fun keyWord(): KeywordType {
        throwIncorrectTokenType(TokenType.KEYWORD)
        return keywordLut[currentToken]
            ?: throw IllegalArgumentException("Keyword type is not supported $currentToken")
    }

    fun symbol(): Char {
        throwIncorrectTokenType(TokenType.SYMBOL)
        return currentToken[0]
    }

    fun identifier(): String {
        throwIncorrectTokenType(TokenType.IDENTIFIER)
        return currentToken
    }

    fun intVal(): Int {
        throwIncorrectTokenType(TokenType.INT_CONST)
        return currentToken.toInt()
    }

    fun stringVal(): String {
        throwIncorrectTokenType(TokenType.STRING_CONST)
        return currentToken
    }

    private fun throwIncorrectTokenType(tokenType: TokenType) {
        if (currentType != tokenType) {
            throw IllegalAccessException("Can only get keyword if current type is $tokenType")
        }
    }

    fun writeToXmlFile() {
        xmlStreamWriter?.writeEndElement()
        xmlStreamWriter?.flush()
        xmlStreamWriter?.close()
        println("Writing tokens to XML output $outputFileName")
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
        xmlStreamWriter?.writeStartElement(elementName)
        xmlStreamWriter?.writeCharacters(" $data ")
        xmlStreamWriter?.writeEndElement()
        xmlStreamWriter?.writeCharacters(System.lineSeparator())
    }

    private fun removeComments(text: String): String {
        val regex = Regex("""(//[^\n]*|/\*.*?\*/)""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE))
        return regex.replace(text, "")
    }
}


fun main() {
    val jackTokenizer = JackTokenizer("/Users/arturokuang/Downloads/nand2tetris/projects/10/Square/Square.jack")
    while (jackTokenizer.hasMoreTokens()) {
        jackTokenizer.advance()
        println("${jackTokenizer.tokenType()}, ${jackTokenizer.getCurrentToken()}")
    }

    jackTokenizer.writeToXmlFile()
}