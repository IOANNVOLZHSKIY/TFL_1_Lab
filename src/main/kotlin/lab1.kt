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

fun parser(part: String, variables: List<String>) {
    val constructorLevel = mutableMapOf<String, Int>() //Словарь размерности конструктора
    val constructorTerms = mutableMapOf<String, String>() //Словарь для термов внутри конструктора
    val constructorLinear = mutableMapOf<String, List<String>>() //Словарь для линейных разложений

    var mem = Stack()
    var temp: String

    for (i in 0..(part.length - 1)) {
        if (part[i] == '(') {

            constructorLevel[mem.peek().toString()] = 0 // Если скобка открылась, то создаем нулевое значение уровня конструктора

        } else if (part[i].toString() in variables) {

            if (constructorTerms[mem.peek().toString()] != null){ // Если попалась переменная, то сразу вносим в термы конструктора
                constructorTerms[mem.peek().toString()] += part[i].toString()
            } else {
                constructorTerms[mem.peek().toString()] = part[i].toString()
            }

            constructorLevel[mem.peek().toString()] = constructorLevel[mem.peek().toString()]!! + 1

        } else if ((part[i].toString() == ",") || (part[i].toString() == " ")) {

            continue

        } else if (part[i] == ')') {
            temp = mem.pop().toString() // Если скобка закрылась, то смотрим последний конструктор в стеке, а дальше варианты

            if ((constructorLevel[temp] == 2) &&
                (constructorTerms[temp]?.get(0).toString() in variables) &&
                (constructorTerms[temp]?.get(1).toString() in variables)) { // Арность - 2. Если оба терма - переменные

                constructorLinear[temp] = listOf(
                    temp + "_0*" + constructorTerms[temp]?.get(0).toString(),
                    temp + "_1*" + constructorTerms[temp]?.get(1).toString(),
                    temp + "_2")

            } else if ((constructorLevel[temp] == 2) &&
                ((constructorTerms[temp]?.get(0).toString() in variables) &&
                        !(constructorTerms[temp]?.get(1).toString() in variables))) { // Арность - 2. Если первая - переменная, а вторая - нет

                constructorLinear[temp] = listOf(
                    temp + "_0*" + constructorTerms[temp]?.get(0).toString())

                constructorLinear[constructorTerms[temp]?.get(1).toString()]?.forEach { element ->
                    var str = temp
                    str += "_1*"
                    str += element
                    constructorLinear[temp] = constructorLinear[temp]!! + str
                }

                constructorLinear[temp] = constructorLinear[temp]!! + (temp + "_2")

            } else if ((constructorLevel[temp] == 2) &&
                ((constructorTerms[temp]?.get(1).toString() in variables) &&
                        !(constructorTerms[temp]?.get(0).toString() in variables))){ // Арность - 2. Если вторая - переменная, а первая - нет

                constructorLinear[temp] = listOf()

                constructorLinear[constructorTerms[temp]?.get(0).toString()]?.forEach { element ->
                    var str = temp
                    str += "_0*"
                    str += element
                    constructorLinear[temp] = constructorLinear[temp]!! + str
                }

                constructorLinear[temp] = constructorLinear[temp]!! + (temp + "_1*" + constructorTerms[temp]?.get(1).toString()) + (temp + "_2")

            } else if ((constructorLevel[temp] == 2) &&
                (!(constructorTerms[temp]?.get(0).toString() in variables) &&
                        !(constructorTerms[temp]?.get(1).toString() in variables))){

                constructorLinear[temp] = listOf()

                constructorLinear[constructorTerms[temp]?.get(0).toString()]?.forEach { element ->
                    var str = temp
                    str += "_0*"
                    str += element
                    constructorLinear[temp] = constructorLinear[temp]!! + str
                }

                constructorLinear[constructorTerms[temp]?.get(1).toString()]?.forEach { element ->
                    var str = temp
                    str += "_1*"
                    str += element
                    constructorLinear[temp] = constructorLinear[temp]!! + str
                }

                constructorLinear[temp] = constructorLinear[temp]!! + (temp + "_2")

            } else if ((constructorLevel[temp] == 1) && (constructorTerms[temp]?.get(0).toString() in variables)) { // Арность - 1. Первый терм - переменая

                constructorLinear[temp] = listOf(
                    temp + "_0*" + constructorTerms[temp]?.get(0).toString(),
                    temp + "_1")

            } else if ((constructorLevel[temp] == 1) && !(constructorTerms[temp]?.get(0).toString() in variables)) { // Арность - 1. Первый терм - не переменая

                constructorLinear[temp] = listOf()

                // Здесь мы собираем строку, где temp_0 умножается на каждый элемент из вложенного разложенного конструктора в цикле.

                constructorLinear[constructorTerms[temp]?.get(0).toString()]?.forEach { element ->
                    var str = temp
                    str += "_0*"
                    str += element
                    constructorLinear[temp] = constructorLinear[temp]!! + str
                }

                constructorLinear[temp] = constructorLinear[temp]!! + (temp + "_1")

            } else if ((constructorLevel[temp] == 0)) { // Арность - 0

                constructorLinear[temp] = listOf(
                    temp + "_0")

            }
        } else {

            if (constructorLevel[mem.peek().toString()] != null) {
                constructorLevel[mem.peek().toString()] = constructorLevel[mem.peek().toString()]!! + 1
            }

            if ((mem.peek() != null) && (constructorTerms[mem.peek().toString()] != null)) {
                constructorTerms[mem.peek().toString()] += part[i].toString()
            } else if ((mem.peek() != null) && (constructorTerms[mem.peek().toString()] == null)) {
                constructorTerms[mem.peek().toString()] = part[i].toString()
            }

            mem.push(part[i])
        }
    }

    println(constructorLinear[part[0].toString()])
}

fun main() {
    val input0 = "variables = x, y"
    val input1 = "f(g(x, y)) -> g(h(y), x)"
    val input2 = "h(f(x)) -> f(x)"

    var parts = input0.split(" = ")[1].split(", ")
    val variables = parts.toMutableList()

    parts = input1.split(" -> ")
    val leftPart_1 = parts[0].trim()
    val rightPart_1 = parts[1].trim()

    parts = input2.split(" -> ")
    val leftPart_2 = parts[0].trim()
    val rightPart_2 = parts[1].trim()

    parser(rightPart_1, variables)

}