import java.util.stream.IntStream.range

class Stack{
    val elements: MutableList<Any> = mutableListOf()

    fun isEmpty() = elements.isEmpty()
    fun size() = elements.size
    fun push(item: Any) = elements.add(item)

    fun pop() : Any? {
        val item = elements.lastOrNull()
        if (!isEmpty()){
            elements.removeAt(elements.size -1)
        }
        return item
    }

    fun peek() : Any? = elements.lastOrNull()
    override fun toString(): String = elements.toString()
}

fun main() {
    val input0 = "variables = x, y"
    val input1 = "f(g(x, y)) -> g(h(y), x)"
    val input2 = "h(f(x)) -> f(x)"

    var parts = input0.split(" = ")[1].split(", ")
    val variables = parts.toMutableList()
    val constructorLevel = mutableMapOf<String, Int>() //Словарь размерности конструктора
    var mem = Stack()

    parts = input1.split(" -> ")
    val leftPart_1 = parts[0].trim()
    val rightPart_1 = parts[1].trim()

    parts = input2.split(" -> ")
    val leftPart_2 = parts[0].trim()
    val rightPart_2 = parts[1].trim()
}