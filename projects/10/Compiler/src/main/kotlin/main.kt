import java.io.File

fun main(args: Array<String>) {
    val path = "/Users/arturokuang/Downloads/nand2tetris/projects/10/ArrayTest/"
    val compilationEngine = CompilationEngine(path)
    compilationEngine.compile()
}