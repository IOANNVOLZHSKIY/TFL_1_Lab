import java.io.File

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

fun parser(part: String, variables: List<String>): List<String>? {
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

    return constructorLinear[part[0].toString()]
}

fun main() {
    val input0 = "variables = x, y"
    val input1 = "f(g(x, y)) -> g(h(y), x)"
    val input2 = "h(f(x)) -> f(x)"

    /* 2. Сделать приведение выражений (если равная переменная типа z*x + t*x = (z+t)x
    * 3. Составление выражений в smt2
    * 4. Сделать ввод из консоли
    * 5. Запуск и работа z3*/

    var parts = input0.split(" = ")[1].split(", ")
    val variables = parts.toMutableList()

    parts = input1.split(" -> ")
    val leftPart_1 = parts[0].trim()
    val rightPart_1 = parts[1].trim()

    parts = input2.split(" -> ")
    val leftPart_2 = parts[0].trim()
    val rightPart_2 = parts[1].trim()

    val leftRes_1 = parser(leftPart_1, variables)
    val rightRes_1 = parser(rightPart_1, variables)
    val leftRes_2 = parser(leftPart_2, variables)
    val rightRes_2 = parser(rightPart_2, variables)

    val multipliers = mutableMapOf<String, String>() // Множетели у переменных и свободного члена. Ключ - переменная или "free", если свободная
    val multipliersCount = mutableMapOf<String, Int>() // Количество множетелей у переменной
    val termsWithVar = mutableMapOf<Char, List<String>>() // Мапа термов при переменных у конструкторов, после разложения в линейные
    val termsWithoutVar = mutableMapOf<Char, List<String>>() // Мапа термов при свободных членах у конструкторов, после разложения в линейные

    if (leftRes_1 != null) {
        for (i in 0..(leftRes_1.size - 1)) {

            if (leftRes_1[i].takeLast(1) in variables) { // если слагаемое содержит на конце переменную

                var temp = ""
                var temp_terms = ""

                for (t in 0 .. (leftRes_1[i].length - 2)) {

                    if (leftRes_1[i][t] != '*') { // до знака умножения идет один множитель
                        temp += leftRes_1[i][t]
                        temp_terms += leftRes_1[i][t]
                    } else {

                        if ((termsWithVar[temp_terms[0]] != null) && (termsWithVar[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                            termsWithVar[temp_terms[0]] = termsWithVar[temp_terms[0]]!! + temp_terms
                        } else if (termsWithVar[temp_terms[0]] == null) {
                            termsWithVar[temp_terms[0]] = listOf(temp_terms)
                        }

                        temp_terms = ""

                        if (multipliersCount[leftRes_1[i].takeLast(1)] != null) {
                            multipliersCount[leftRes_1[i].takeLast(1)] = multipliersCount[leftRes_1[i].takeLast(1)]!! + 1
                        } else {
                            multipliersCount[leftRes_1[i].takeLast(1)] = 1
                        }

                        if (!(leftRes_1[i][t + 1].toString() in variables)) {
                            temp += " " // пробел для записи другого множителя
                        } else {

                            if ((multipliers[leftRes_1[i].takeLast(1)] == null) && (multipliersCount[leftRes_1[i].takeLast(1)]!! > 1)) {
                                multipliers[leftRes_1[i].takeLast(1)] = "(* " + temp + ")"
                                multipliersCount[leftRes_1[i].takeLast(1)] = 0
                            } else if ((multipliers[leftRes_1[i].takeLast(1)] == null) && (multipliersCount[leftRes_1[i].takeLast(1)]!! == 1)) {
                                multipliers[leftRes_1[i].takeLast(1)] = temp
                            } else if ((multipliers[leftRes_1[i].takeLast(1)] != null) && (multipliersCount[leftRes_1[i].takeLast(1)]!! > 1)){
                                multipliers[leftRes_1[i].takeLast(1)] = multipliers[leftRes_1[i].takeLast(1)] + " (* " + temp + ")"
                                multipliersCount[leftRes_1[i].takeLast(1)] = 0
                            } else if ((multipliers[leftRes_1[i].takeLast(1)] != null) && (multipliersCount[leftRes_1[i].takeLast(1)]!! == 1)){
                                multipliers[leftRes_1[i].takeLast(1)] = multipliers[leftRes_1[i].takeLast(1)] + " " + temp
                            }

                            temp = ""
                        }
                    }
                }
            } else { // если слагаемое НЕ содержит на конце переменную аkа свободный член

                var temp = ""
                var temp_terms = ""
                var statusOfMultiply = true

                for (t in 0 .. (leftRes_1[i].length - 1)) {

                    if ((leftRes_1[i][t] != '*') && (!(leftRes_1[i][t].toString() == leftRes_1[i].takeLast(1)))) { // до знака умножения идет один множитель
                        temp += leftRes_1[i][t]
                        temp_terms += leftRes_1[i][t]
                    } else if (leftRes_1[i][t].toString() == leftRes_1[i].takeLast(1)) {

                        temp += leftRes_1[i][t]
                        temp_terms += leftRes_1[i][t]

                        if (multipliersCount["free"] != null) {
                            multipliersCount["free"] = multipliersCount["free"]!! + 1
                        } else {
                            multipliersCount["free"] = 1
                        }

                        if (statusOfMultiply == true) {
                            if ((termsWithoutVar[temp_terms[0]] != null) && (termsWithVar[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                                termsWithoutVar[temp_terms[0]] = termsWithoutVar[temp_terms[0]]!! + temp_terms
                            } else if (termsWithoutVar[temp_terms[0]] == null) {
                                termsWithoutVar[temp_terms[0]] = listOf(temp_terms)
                            }
                        } else {
                            if ((termsWithVar[temp_terms[0]] != null) && (termsWithVar[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                                termsWithVar[temp_terms[0]] = termsWithVar[temp_terms[0]]!! + temp_terms
                            } else if (termsWithVar[temp_terms[0]] == null) {
                                termsWithVar[temp_terms[0]] = listOf(temp_terms)
                            }
                        }

                        if ((multipliers["free"] == null) && (multipliersCount["free"]!! > 1)) {
                            multipliers["free"] = "(* " + temp + ")"
                            multipliersCount["free"] = 0
                        } else if ((multipliers["free"] == null) && (multipliersCount["free"]!! == 1)) {
                            multipliers["free"] = temp
                        } else if ((multipliers["free"] != null) && (multipliersCount["free"]!! > 1)){
                            multipliers["free"] = multipliers["free"] + " (* " + temp + ")"
                            multipliersCount["free"] = 0
                        } else if ((multipliers["free"] != null) && (multipliersCount["free"]!! == 1)){
                            multipliers["free"] = multipliers["free"] + " " + temp
                        }

                        temp = ""

                    } else { // попалось умножение

                        statusOfMultiply = false

                        if ((termsWithVar[temp_terms[0]] != null) && (termsWithVar[temp_terms[0]]?.contains(temp_terms) == false)) {
                            // заносим в мапу термов, если его еще нет внутри
                            termsWithVar[temp_terms[0]] = termsWithVar[temp_terms[0]]!! + temp_terms
                        } else if (termsWithVar[temp_terms[0]] == null) {
                            termsWithVar[temp_terms[0]] = listOf(temp_terms)
                        }

                        temp_terms = ""

                        if (multipliersCount["free"] != null) {
                            multipliersCount["free"] = multipliersCount["free"]!! + 1
                        } else {
                            multipliersCount["free"] = 1
                        }

                        temp += " " // пробел для записи другого множителя
                    }
                }

            }
        }
    }

    println(leftRes_1)

    println(termsWithVar)
    println(termsWithoutVar)
    println(multipliers)

    /*val fileName = "solver.smt2"
var file = File(fileName)

val isNewFileCreated :Boolean = file.createNewFile()

if(isNewFileCreated){
println("$fileName is created successfully.")
} else{
println("$fileName already exists.")
}

file.writeText("(set-logic QF_NIA)")*/

    }