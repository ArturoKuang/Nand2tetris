import java.lang.IllegalArgumentException

class SymbolTableHack {

    enum class Kind {
        STATIC, FIELD, ARG, VAR, NO_KIND
    }

    data class SymbolTableColumn(
        val type: String,
        val kind: Kind,
        val count: Int
    )

    private val hackSymbolTable = mutableMapOf<String, SymbolTableColumn>()

    var staticCount = 0
    var fieldCount = 0
    var argCount = 0
    var varCount = 0

    fun reset() {
        staticCount = 0
        fieldCount = 0
        argCount = 0
        varCount = 0
        hackSymbolTable.clear()
    }

    fun define(name: String, type: String, kind: Kind) {
        when (kind) {
            Kind.STATIC -> {
                hackSymbolTable[name] = SymbolTableColumn(type, kind, staticCount)
                staticCount++
            }
            Kind.ARG -> {
                hackSymbolTable[name] = SymbolTableColumn(type, kind, argCount)
                argCount++
            }
            Kind.FIELD -> {
                hackSymbolTable[name] = SymbolTableColumn(type, kind, fieldCount)
                fieldCount++
            }
            Kind.VAR -> {
                hackSymbolTable[name] = SymbolTableColumn(type, kind, varCount)
                varCount++
            }
        }
    }

    fun varCount(kind: Kind): Int {
        return when (kind) {
            Kind.STATIC -> staticCount
            Kind.ARG -> argCount
            Kind.FIELD -> fieldCount
            Kind.VAR -> varCount
            else -> {
                throw IllegalArgumentException("NO KIND?")
            }
        }
    }

    fun kindOf(name: String): Kind {
        return hackSymbolTable[name]?.kind
            ?: throw NoSuchElementException("Symbol not in symbol table")
    }

    fun typeOf(name: String): String {
        return hackSymbolTable[name]?.type
            ?: throw NoSuchElementException("Symbol not in symbol table")
    }

    fun indexOf(name: String): Int {
        return hackSymbolTable[name]?.count
            ?: throw NoSuchElementException("Symbol not in symbol table")
    }

    override fun toString(): String {
        return hackSymbolTable.toString()
    }
}