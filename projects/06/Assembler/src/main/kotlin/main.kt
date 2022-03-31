import java.io.File

fun main(args: Array<String>) {
//    val hackAssembler = HackAssembler(args[0])
    testFileWithInput("/Users/arturokuang/Downloads/nand2tetris/projects/06/max/MaxLCom.hack",
        "/Users/arturokuang/Downloads/nand2tetris/projects/06/max/MaxL.asm")

    testFileWithInput("/Users/arturokuang/Downloads/nand2tetris/projects/06/max/MaxComp.hack",
        "/Users/arturokuang/Downloads/nand2tetris/projects/06/max/Max.asm")

    testFileWithInput("/Users/arturokuang/Downloads/nand2tetris/projects/06/add/AddComp.hack",
        "/Users/arturokuang/Downloads/nand2tetris/projects/06/add/Add.asm")

    testFileWithInput("/Users/arturokuang/Downloads/nand2tetris/projects/06/rect/RectComp.hack",
        "/Users/arturokuang/Downloads/nand2tetris/projects/06/rect/Rect.asm")

    testFileWithInput("/Users/arturokuang/Downloads/nand2tetris/projects/06/pong/PongComp.hack",
        "/Users/arturokuang/Downloads/nand2tetris/projects/06/pong/Pong.asm")
}

fun testFileWithInput(inputFileName: String, asmFileName: String) {
    val inputFile = File(inputFileName).readBytes()

    val hackAssembler = HackAssembler(asmFileName)
    hackAssembler.assemble()

    val outputFile = File(hackAssembler.outputFileName).readBytes()

    println("$inputFileName == ${hackAssembler.outputFileName}: ${inputFile.contentEquals(outputFile)}")
}