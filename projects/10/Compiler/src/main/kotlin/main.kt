fun main(args: Array<String>) {
    val path = "/Users/arturokuang/Downloads/nand2tetris/projects/10/Square/"
    val compilationEngine = JackAnalyzer(path)
    compilationEngine.compile()
}