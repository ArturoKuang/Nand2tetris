fun main(args: Array<String>) {
    //val inputFileName = args[0]
    val vmTranslator = VMTranslator("/Users/arturokuang/Downloads/nand2tetris/projects/07/StackArithmetic/SimpleAdd/SimpleAdd.vm")
    vmTranslator.translate()
}