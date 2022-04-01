import java.io.File

class CompilationEngine(private val pathName: String) {

    fun compile() {
        File(pathName).walk().forEach {
            val fileName = it.name
            if (fileName.endsWith(".jack")) {
                println("Compiling $fileName")
                compileFile(it.absolutePath)
                println("Finishing compiling $fileName")
            }
        }
    }

    private fun compileFile(filePath: String) {
        val jackAnalyzer = JackAnalyzer(filePath)
        while (jackAnalyzer.hasMoreTokens()) {
            jackAnalyzer.advance()
        }
        jackAnalyzer.writeToXmlFile()
    }
}