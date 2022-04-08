import java.io.File

class JackAnalyzer(private val pathName: String) {

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
        val compilationEngine = CompilationEngine(filePath)
        compilationEngine.compileClass()
        compilationEngine.writeToFile()
    }
}

