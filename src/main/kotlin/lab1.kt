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
    println("Enter variables (for example, variables = x, y)")
    val input0 = readLine().toString()
    println("Enter TRS (for example, f(g(x, y)) -> g(h(y), x))")
    val input1 = readLine().toString()
    println("Enter TRS (for example, h(f(x)) -> f(x))")
    val input2 = readLine().toString()

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

    val multipliers_1 = mutableMapOf<String, String>() // Множетели у переменных и свободного члена. Ключ - переменная или "free", если свободная
    val multipliersCount_1 = mutableMapOf<String, Int>() // Количество множетелей у переменной
    val termsWithVar_1 = mutableMapOf<Char, List<String>>() // Мапа термов при переменных у конструкторов, после разложения в линейные
    val termsWithoutVar_1 = mutableMapOf<Char, List<String>>() // Мапа термов при свободных членах у конструкторов, после разложения в линейные
    val termsFirst_1 = mutableMapOf<Char, String>()

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

                        if ((termsWithVar_1[temp_terms[0]] != null) && (termsWithVar_1[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                            termsWithVar_1[temp_terms[0]] = termsWithVar_1[temp_terms[0]]!! + temp_terms
                        } else if (termsWithVar_1[temp_terms[0]] == null) {
                            termsWithVar_1[temp_terms[0]] = listOf(temp_terms)
                        }

                        if (multipliersCount_1[leftRes_1[i].takeLast(1)] != null) {
                            multipliersCount_1[leftRes_1[i].takeLast(1)] = multipliersCount_1[leftRes_1[i].takeLast(1)]!! + 1
                        } else {
                            multipliersCount_1[leftRes_1[i].takeLast(1)] = 1
                        }

                        if (!(leftRes_1[i][t + 1].toString() in variables)) {
                            temp += " " // пробел для записи другого множителя
                        } else {
                            termsFirst_1[leftRes_1[i][t + 1]] = temp_terms
                            if ((multipliers_1[leftRes_1[i].takeLast(1)] == null) && (multipliersCount_1[leftRes_1[i].takeLast(1)]!! > 1)) {
                                multipliers_1[leftRes_1[i].takeLast(1)] = "(* " + temp + ")"
                                multipliersCount_1[leftRes_1[i].takeLast(1)] = 0
                            } else if ((multipliers_1[leftRes_1[i].takeLast(1)] == null) && (multipliersCount_1[leftRes_1[i].takeLast(1)]!! == 1)) {
                                multipliers_1[leftRes_1[i].takeLast(1)] = temp
                            } else if ((multipliers_1[leftRes_1[i].takeLast(1)] != null) && (multipliersCount_1[leftRes_1[i].takeLast(1)]!! > 1)){
                                multipliers_1[leftRes_1[i].takeLast(1)] = multipliers_1[leftRes_1[i].takeLast(1)] + " (* " + temp + ")"
                                multipliersCount_1[leftRes_1[i].takeLast(1)] = 0
                            } else if ((multipliers_1[leftRes_1[i].takeLast(1)] != null) && (multipliersCount_1[leftRes_1[i].takeLast(1)]!! == 1)){
                                multipliers_1[leftRes_1[i].takeLast(1)] = multipliers_1[leftRes_1[i].takeLast(1)] + " " + temp
                            }

                            temp = ""
                        }
                        temp_terms = ""
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

                        if (multipliersCount_1["free"] != null) {
                            multipliersCount_1["free"] = multipliersCount_1["free"]!! + 1
                        } else {
                            multipliersCount_1["free"] = 1
                        }

                        if (statusOfMultiply == true) {
                            if ((termsWithoutVar_1[temp_terms[0]] != null) && (termsWithVar_1[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                                termsWithoutVar_1[temp_terms[0]] = termsWithoutVar_1[temp_terms[0]]!! + temp_terms
                            } else if (termsWithoutVar_1[temp_terms[0]] == null) {
                                termsWithoutVar_1[temp_terms[0]] = listOf(temp_terms)
                            }
                        } else {
                            if ((termsWithVar_1[temp_terms[0]] != null) && (termsWithVar_1[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                                termsWithVar_1[temp_terms[0]] = termsWithVar_1[temp_terms[0]]!! + temp_terms
                            } else if (termsWithVar_1[temp_terms[0]] == null) {
                                termsWithVar_1[temp_terms[0]] = listOf(temp_terms)
                            }
                        }

                        if ((multipliers_1["free"] == null) && (multipliersCount_1["free"]!! > 1)) {
                            multipliers_1["free"] = "(* " + temp + ")"
                            multipliersCount_1["free"] = 0
                        } else if ((multipliers_1["free"] == null) && (multipliersCount_1["free"]!! == 1)) {
                            multipliers_1["free"] = temp
                        } else if ((multipliers_1["free"] != null) && (multipliersCount_1["free"]!! > 1)){
                            multipliers_1["free"] = multipliers_1["free"] + " (* " + temp + ")"
                            multipliersCount_1["free"] = 0
                        } else if ((multipliers_1["free"] != null) && (multipliersCount_1["free"]!! == 1)){
                            multipliers_1["free"] = multipliers_1["free"] + " " + temp
                        }

                        temp = ""

                    } else { // попалось умножение

                        statusOfMultiply = false

                        if ((termsWithVar_1[temp_terms[0]] != null) && (termsWithVar_1[temp_terms[0]]?.contains(temp_terms) == false)) {
                            // заносим в мапу термов, если его еще нет внутри
                            termsWithVar_1[temp_terms[0]] = termsWithVar_1[temp_terms[0]]!! + temp_terms
                        } else if (termsWithVar_1[temp_terms[0]] == null) {
                            termsWithVar_1[temp_terms[0]] = listOf(temp_terms)
                        }

                        temp_terms = ""

                        if (multipliersCount_1["free"] != null) {
                            multipliersCount_1["free"] = multipliersCount_1["free"]!! + 1
                        } else {
                            multipliersCount_1["free"] = 1
                        }

                        temp += " " // пробел для записи другого множителя
                    }
                }

            }
        }
    }

    val multipliers_2 = mutableMapOf<String, String>() // Множетели у переменных и свободного члена. Ключ - переменная или "free", если свободная
    val multipliersCount_2 = mutableMapOf<String, Int>() // Количество множетелей у переменной
    val termsWithVar_2 = mutableMapOf<Char, List<String>>() // Мапа термов при переменных у конструкторов, после разложения в линейные
    val termsWithoutVar_2 = mutableMapOf<Char, List<String>>() // Мапа термов при свободных членах у конструкторов, после разложения в линейные
    val termsFirst_2 = mutableMapOf<Char, String>()

    if (rightRes_1 != null) {
        for (i in 0..(rightRes_1.size - 1)) {

            if (rightRes_1[i].takeLast(1) in variables) { // если слагаемое содержит на конце переменную

                var temp = ""
                var temp_terms = ""

                for (t in 0 .. (rightRes_1[i].length - 2)) {

                    if (rightRes_1[i][t] != '*') { // до знака умножения идет один множитель
                        temp += rightRes_1[i][t]
                        temp_terms += rightRes_1[i][t]
                    } else {

                        if ((termsWithVar_2[temp_terms[0]] != null) && (termsWithVar_2[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                            termsWithVar_2[temp_terms[0]] = termsWithVar_2[temp_terms[0]]!! + temp_terms
                        } else if (termsWithVar_2[temp_terms[0]] == null) {
                            termsWithVar_2[temp_terms[0]] = listOf(temp_terms)
                        }


                        if (multipliersCount_2[rightRes_1[i].takeLast(1)] != null) {
                            multipliersCount_2[rightRes_1[i].takeLast(1)] = multipliersCount_2[rightRes_1[i].takeLast(1)]!! + 1
                        } else {
                            multipliersCount_2[rightRes_1[i].takeLast(1)] = 1
                        }

                        if (!(rightRes_1[i][t + 1].toString() in variables)) {
                            temp += " " // пробел для записи другого множителя
                        } else {
                            termsFirst_2[rightRes_1[i][t + 1]] = temp_terms
                            if ((multipliers_2[rightRes_1[i].takeLast(1)] == null) && (multipliersCount_2[rightRes_1[i].takeLast(1)]!! > 1)) {
                                multipliers_2[rightRes_1[i].takeLast(1)] = "(* " + temp + ")"
                                multipliersCount_2[rightRes_1[i].takeLast(1)] = 0
                            } else if ((multipliers_2[rightRes_1[i].takeLast(1)] == null) && (multipliersCount_2[rightRes_1[i].takeLast(1)]!! == 1)) {
                                multipliers_2[rightRes_1[i].takeLast(1)] = temp
                            } else if ((multipliers_2[rightRes_1[i].takeLast(1)] != null) && (multipliersCount_2[rightRes_1[i].takeLast(1)]!! > 1)){
                                multipliers_2[rightRes_1[i].takeLast(1)] = multipliers_2[rightRes_1[i].takeLast(1)] + " (* " + temp + ")"
                                multipliersCount_2[rightRes_1[i].takeLast(1)] = 0
                            } else if ((multipliers_2[rightRes_1[i].takeLast(1)] != null) && (multipliersCount_2[rightRes_1[i].takeLast(1)]!! == 1)){
                                multipliers_2[rightRes_1[i].takeLast(1)] = multipliers_2[rightRes_1[i].takeLast(1)] + " " + temp
                            }

                            temp = ""
                        }
                        temp_terms = ""
                    }
                }
            } else { // если слагаемое НЕ содержит на конце переменную аkа свободный член

                var temp = ""
                var temp_terms = ""
                var statusOfMultiply = true

                for (t in 0 .. (rightRes_1[i].length - 1)) {

                    if ((rightRes_1[i][t] != '*') && (!(rightRes_1[i][t].toString() == rightRes_1[i].takeLast(1)))) { // до знака умножения идет один множитель
                        temp += rightRes_1[i][t]
                        temp_terms += rightRes_1[i][t]
                    } else if (rightRes_1[i][t].toString() == rightRes_1[i].takeLast(1)) {

                        temp += rightRes_1[i][t]
                        temp_terms += rightRes_1[i][t]

                        if (multipliersCount_2["free"] != null) {
                            multipliersCount_2["free"] = multipliersCount_2["free"]!! + 1
                        } else {
                            multipliersCount_2["free"] = 1
                        }

                        if (statusOfMultiply == true) {
                            if ((termsWithoutVar_2[temp_terms[0]] != null) && (termsWithVar_2[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                                termsWithoutVar_2[temp_terms[0]] = termsWithoutVar_2[temp_terms[0]]!! + temp_terms
                            } else if (termsWithoutVar_2[temp_terms[0]] == null) {
                                termsWithoutVar_2[temp_terms[0]] = listOf(temp_terms)
                            }
                        } else {
                            if ((termsWithVar_2[temp_terms[0]] != null) && (termsWithVar_2[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                                termsWithVar_2[temp_terms[0]] = termsWithVar_2[temp_terms[0]]!! + temp_terms
                            } else if (termsWithVar_2[temp_terms[0]] == null) {
                                termsWithVar_2[temp_terms[0]] = listOf(temp_terms)
                            }
                        }

                        if ((multipliers_2["free"] == null) && (multipliersCount_2["free"]!! > 1)) {
                            multipliers_2["free"] = "(* " + temp + ")"
                            multipliersCount_2["free"] = 0
                        } else if ((multipliers_2["free"] == null) && (multipliersCount_2["free"]!! == 1)) {
                            multipliers_2["free"] = temp
                        } else if ((multipliers_2["free"] != null) && (multipliersCount_2["free"]!! > 1)){
                            multipliers_2["free"] = multipliers_2["free"] + " (* " + temp + ")"
                            multipliersCount_2["free"] = 0
                        } else if ((multipliers_2["free"] != null) && (multipliersCount_2["free"]!! == 1)){
                            multipliers_2["free"] = multipliers_2["free"] + " " + temp
                        }

                        temp = ""

                    } else { // попалось умножение

                        statusOfMultiply = false

                        if ((termsWithVar_2[temp_terms[0]] != null) && (termsWithVar_2[temp_terms[0]]?.contains(temp_terms) == false)) {
                            // заносим в мапу термов, если его еще нет внутри
                            termsWithVar_2[temp_terms[0]] = termsWithVar_2[temp_terms[0]]!! + temp_terms
                        } else if (termsWithVar_2[temp_terms[0]] == null) {
                            termsWithVar_2[temp_terms[0]] = listOf(temp_terms)
                        }

                        temp_terms = ""

                        if (multipliersCount_2["free"] != null) {
                            multipliersCount_2["free"] = multipliersCount_2["free"]!! + 1
                        } else {
                            multipliersCount_2["free"] = 1
                        }

                        temp += " " // пробел для записи другого множителя
                    }
                }

            }
        }
    }

    val multipliers_3 = mutableMapOf<String, String>() // Множетели у переменных и свободного члена. Ключ - переменная или "free", если свободная
    val multipliersCount_3 = mutableMapOf<String, Int>() // Количество множетелей у переменной
    val termsWithVar_3 = mutableMapOf<Char, List<String>>() // Мапа термов при переменных у конструкторов, после разложения в линейные
    val termsWithoutVar_3 = mutableMapOf<Char, List<String>>() // Мапа термов при свободных членах у конструкторов, после разложения в линейные
    val termsFirst_3 = mutableMapOf<Char, String>()

    if (leftRes_2 != null) {
        for (i in 0..(leftRes_2.size - 1)) {

            if (leftRes_2[i].takeLast(1) in variables) { // если слагаемое содержит на конце переменную

                var temp = ""
                var temp_terms = ""

                for (t in 0 .. (leftRes_2[i].length - 2)) {

                    if (leftRes_2[i][t] != '*') { // до знака умножения идет один множитель
                        temp += leftRes_2[i][t]
                        temp_terms += leftRes_2[i][t]
                    } else {

                        if ((termsWithVar_3[temp_terms[0]] != null) && (termsWithVar_3[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                            termsWithVar_3[temp_terms[0]] = termsWithVar_3[temp_terms[0]]!! + temp_terms
                        } else if (termsWithVar_3[temp_terms[0]] == null) {
                            termsWithVar_3[temp_terms[0]] = listOf(temp_terms)
                        }

                        if (multipliersCount_3[leftRes_2[i].takeLast(1)] != null) {
                            multipliersCount_3[leftRes_2[i].takeLast(1)] = multipliersCount_3[leftRes_2[i].takeLast(1)]!! + 1
                        } else {
                            multipliersCount_3[leftRes_2[i].takeLast(1)] = 1
                        }

                        if (!(leftRes_2[i][t + 1].toString() in variables)) {
                            temp += " " // пробел для записи другого множителя
                        } else {
                            termsFirst_3[leftRes_2[i][t + 1]] = temp_terms
                            if ((multipliers_3[leftRes_2[i].takeLast(1)] == null) && (multipliersCount_3[leftRes_2[i].takeLast(1)]!! > 1)) {
                                multipliers_3[leftRes_2[i].takeLast(1)] = "(* " + temp + ")"
                                multipliersCount_3[leftRes_2[i].takeLast(1)] = 0
                            } else if ((multipliers_3[leftRes_2[i].takeLast(1)] == null) && (multipliersCount_3[leftRes_2[i].takeLast(1)]!! == 1)) {
                                multipliers_3[leftRes_2[i].takeLast(1)] = temp
                            } else if ((multipliers_3[leftRes_2[i].takeLast(1)] != null) && (multipliersCount_3[leftRes_2[i].takeLast(1)]!! > 1)){
                                multipliers_3[leftRes_2[i].takeLast(1)] = multipliers_3[leftRes_2[i].takeLast(1)] + " (* " + temp + ")"
                                multipliersCount_3[leftRes_2[i].takeLast(1)] = 0
                            } else if ((multipliers_3[leftRes_2[i].takeLast(1)] != null) && (multipliersCount_3[leftRes_2[i].takeLast(1)]!! == 1)){
                                multipliers_3[leftRes_2[i].takeLast(1)] = multipliers_3[leftRes_2[i].takeLast(1)] + " " + temp
                            }

                            temp = ""
                        }

                        temp_terms = ""
                    }
                }
            } else { // если слагаемое НЕ содержит на конце переменную аkа свободный член

                var temp = ""
                var temp_terms = ""
                var statusOfMultiply = true

                for (t in 0 .. (leftRes_2[i].length - 1)) {

                    if ((leftRes_2[i][t] != '*') && (!(leftRes_2[i][t].toString() == leftRes_2[i].takeLast(1)))) { // до знака умножения идет один множитель
                        temp += leftRes_2[i][t]
                        temp_terms += leftRes_2[i][t]
                    } else if (leftRes_2[i][t].toString() == leftRes_2[i].takeLast(1)) {

                        temp += leftRes_2[i][t]
                        temp_terms += leftRes_2[i][t]

                        if (multipliersCount_3["free"] != null) {
                            multipliersCount_3["free"] = multipliersCount_3["free"]!! + 1
                        } else {
                            multipliersCount_3["free"] = 1
                        }

                        if (statusOfMultiply == true) {
                            if ((termsWithoutVar_3[temp_terms[0]] != null) && (termsWithVar_3[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                                termsWithoutVar_3[temp_terms[0]] = termsWithoutVar_3[temp_terms[0]]!! + temp_terms
                            } else if (termsWithoutVar_3[temp_terms[0]] == null) {
                                termsWithoutVar_3[temp_terms[0]] = listOf(temp_terms)
                            }
                        } else {
                            if ((termsWithVar_3[temp_terms[0]] != null) && (termsWithVar_3[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                                termsWithVar_3[temp_terms[0]] = termsWithVar_3[temp_terms[0]]!! + temp_terms
                            } else if (termsWithVar_3[temp_terms[0]] == null) {
                                termsWithVar_3[temp_terms[0]] = listOf(temp_terms)
                            }
                        }

                        if ((multipliers_3["free"] == null) && (multipliersCount_3["free"]!! > 1)) {
                            multipliers_3["free"] = "(* " + temp + ")"
                            multipliersCount_3["free"] = 0
                        } else if ((multipliers_3["free"] == null) && (multipliersCount_3["free"]!! == 1)) {
                            multipliers_3["free"] = temp
                        } else if ((multipliers_3["free"] != null) && (multipliersCount_3["free"]!! > 1)){
                            multipliers_3["free"] = multipliers_3["free"] + " (* " + temp + ")"
                            multipliersCount_3["free"] = 0
                        } else if ((multipliers_3["free"] != null) && (multipliersCount_3["free"]!! == 1)){
                            multipliers_3["free"] = multipliers_3["free"] + " " + temp
                        }

                        temp = ""

                    } else { // попалось умножение

                        statusOfMultiply = false

                        if ((termsWithVar_3[temp_terms[0]] != null) && (termsWithVar_3[temp_terms[0]]?.contains(temp_terms) == false)) {
                            // заносим в мапу термов, если его еще нет внутри
                            termsWithVar_3[temp_terms[0]] = termsWithVar_3[temp_terms[0]]!! + temp_terms
                        } else if (termsWithVar_3[temp_terms[0]] == null) {
                            termsWithVar_3[temp_terms[0]] = listOf(temp_terms)
                        }

                        temp_terms = ""

                        if (multipliersCount_3["free"] != null) {
                            multipliersCount_3["free"] = multipliersCount_3["free"]!! + 1
                        } else {
                            multipliersCount_3["free"] = 1
                        }

                        temp += " " // пробел для записи другого множителя
                    }
                }

            }
        }
    }

    val multipliers_4 = mutableMapOf<String, String>() // Множетели у переменных и свободного члена. Ключ - переменная или "free", если свободная
    val multipliersCount_4 = mutableMapOf<String, Int>() // Количество множетелей у переменной
    val termsWithVar_4 = mutableMapOf<Char, List<String>>() // Мапа термов при переменных у конструкторов, после разложения в линейные
    val termsWithoutVar_4 = mutableMapOf<Char, List<String>>() // Мапа термов при свободных членах у конструкторов, после разложения в линейные
    val termsFirst_4 = mutableMapOf<Char, String>()

    if (rightRes_2 != null) {
        for (i in 0..(rightRes_2.size - 1)) {

            if (rightRes_2[i].takeLast(1) in variables) { // если слагаемое содержит на конце переменную

                var temp = ""
                var temp_terms = ""

                for (t in 0 .. (rightRes_2[i].length - 2)) {

                    if (rightRes_2[i][t] != '*') { // до знака умножения идет один множитель
                        temp += rightRes_2[i][t]
                        temp_terms += rightRes_2[i][t]
                    } else {

                        if ((termsWithVar_4[temp_terms[0]] != null) && (termsWithVar_4[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                            termsWithVar_4[temp_terms[0]] = termsWithVar_4[temp_terms[0]]!! + temp_terms
                        } else if (termsWithVar_4[temp_terms[0]] == null) {
                            termsWithVar_4[temp_terms[0]] = listOf(temp_terms)
                        }

                        if (multipliersCount_4[rightRes_2[i].takeLast(1)] != null) {
                            multipliersCount_4[rightRes_2[i].takeLast(1)] = multipliersCount_4[rightRes_2[i].takeLast(1)]!! + 1
                        } else {
                            multipliersCount_4[rightRes_2[i].takeLast(1)] = 1
                        }

                        if (!(rightRes_2[i][t + 1].toString() in variables)) {
                            temp += " " // пробел для записи другого множителя
                        } else {
                            termsFirst_4[rightRes_2[i][t + 1]] = temp_terms
                            if ((multipliers_4[rightRes_2[i].takeLast(1)] == null) && (multipliersCount_4[rightRes_2[i].takeLast(1)]!! > 1)) {
                                multipliers_4[rightRes_2[i].takeLast(1)] = "(* " + temp + ")"
                                multipliersCount_4[rightRes_2[i].takeLast(1)] = 0
                            } else if ((multipliers_4[rightRes_2[i].takeLast(1)] == null) && (multipliersCount_4[rightRes_2[i].takeLast(1)]!! == 1)) {
                                multipliers_4[rightRes_2[i].takeLast(1)] = temp
                            } else if ((multipliers_4[rightRes_2[i].takeLast(1)] != null) && (multipliersCount_4[rightRes_2[i].takeLast(1)]!! > 1)){
                                multipliers_4[rightRes_2[i].takeLast(1)] = multipliers_3[rightRes_2[i].takeLast(1)] + " (* " + temp + ")"
                                multipliersCount_4[rightRes_2[i].takeLast(1)] = 0
                            } else if ((multipliers_4[rightRes_2[i].takeLast(1)] != null) && (multipliersCount_4[rightRes_2[i].takeLast(1)]!! == 1)){
                                multipliers_4[rightRes_2[i].takeLast(1)] = multipliers_4[rightRes_2[i].takeLast(1)] + " " + temp
                            }

                            temp = ""
                        }
                        temp_terms = ""
                    }
                }
            } else { // если слагаемое НЕ содержит на конце переменную аkа свободный член

                var temp = ""
                var temp_terms = ""
                var statusOfMultiply = true

                for (t in 0 .. (rightRes_2[i].length - 1)) {

                    if ((rightRes_2[i][t] != '*') && (!(rightRes_2[i][t].toString() == rightRes_2[i].takeLast(1)))) { // до знака умножения идет один множитель
                        temp += rightRes_2[i][t]
                        temp_terms += rightRes_2[i][t]
                    } else if (rightRes_2[i][t].toString() == rightRes_2[i].takeLast(1)) {

                        temp += rightRes_2[i][t]
                        temp_terms += rightRes_2[i][t]

                        if (multipliersCount_4["free"] != null) {
                            multipliersCount_4["free"] = multipliersCount_4["free"]!! + 1
                        } else {
                            multipliersCount_4["free"] = 1
                        }

                        if (statusOfMultiply == true) {
                            if ((termsWithoutVar_4[temp_terms[0]] != null) && (termsWithVar_4[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                                termsWithoutVar_4[temp_terms[0]] = termsWithoutVar_4[temp_terms[0]]!! + temp_terms
                            } else if (termsWithoutVar_4[temp_terms[0]] == null) {
                                termsWithoutVar_4[temp_terms[0]] = listOf(temp_terms)
                            }
                        } else {
                            if ((termsWithVar_4[temp_terms[0]] != null) && (termsWithVar_4[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                                termsWithVar_4[temp_terms[0]] = termsWithVar_4[temp_terms[0]]!! + temp_terms
                            } else if (termsWithVar_4[temp_terms[0]] == null) {
                                termsWithVar_4[temp_terms[0]] = listOf(temp_terms)
                            }
                        }

                        if ((multipliers_4["free"] == null) && (multipliersCount_4["free"]!! > 1)) {
                            multipliers_4["free"] = "(* " + temp + ")"
                            multipliersCount_4["free"] = 0
                        } else if ((multipliers_4["free"] == null) && (multipliersCount_4["free"]!! == 1)) {
                            multipliers_4["free"] = temp
                        } else if ((multipliers_4["free"] != null) && (multipliersCount_4["free"]!! > 1)){
                            multipliers_4["free"] = multipliers_4["free"] + " (* " + temp + ")"
                            multipliersCount_4["free"] = 0
                        } else if ((multipliers_4["free"] != null) && (multipliersCount_4["free"]!! == 1)){
                            multipliers_4["free"] = multipliers_4["free"] + " " + temp
                        }

                        temp = ""

                    } else { // попалось умножение

                        statusOfMultiply = false

                        if ((termsWithVar_4[temp_terms[0]] != null) && (termsWithVar_4[temp_terms[0]]?.contains(temp_terms) == false)) {
                            // заносим в мапу термов, если его еще нет внутри
                            termsWithVar_4[temp_terms[0]] = termsWithVar_4[temp_terms[0]]!! + temp_terms
                        } else if (termsWithVar_4[temp_terms[0]] == null) {
                            termsWithVar_4[temp_terms[0]] = listOf(temp_terms)
                        }

                        temp_terms = ""

                        if (multipliersCount_4["free"] != null) {
                            multipliersCount_4["free"] = multipliersCount_4["free"]!! + 1
                        } else {
                            multipliersCount_4["free"] = 1
                        }

                        temp += " " // пробел для записи другого множителя
                    }
                }

            }
        }
    }

    val fileName = "solver.smt2"
    var file = File(fileName)

    val isNewFileCreated :Boolean = file.createNewFile()

    if(isNewFileCreated){
        println("$fileName is created successfully.")
    } else{
        println("$fileName already exists.")
    }

    val checker = mutableListOf<String>()

    File("solver.smt2").bufferedWriter().use { out ->
        out.write("(set-logic QF_NIA)\n")

        for((key, value) in termsWithVar_1){
            for (i in value) {
                if (!(i in checker)) {
                    out.write("(declare-const $i () Int)\n")
                    checker += i
                }
            }
        }

        for((key, value) in termsWithVar_2){
            for (i in value) {
                if (!(i in checker)) {
                    out.write("(declare-const $i () Int)\n")
                    checker += i
                }
            }
        }

        for((key, value) in termsWithVar_3){
            for (i in value) {
                if (!(i in checker)) {
                    out.write("(declare-const $i () Int)\n")
                    checker += i
                }
            }
        }

        for((key, value) in termsWithVar_4){
            for (i in value) {
                if (!(i in checker)) {
                    out.write("(declare-const $i () Int)\n")
                    checker += i
                }
            }
        }

        for((key, value) in termsWithoutVar_1){
            for (i in value) {
                if (!(i in checker)) {
                    out.write("(declare-const $i () Int)\n")
                    checker += i
                }
            }
        }

        for((key, value) in termsWithoutVar_2){
            for (i in value) {
                if (!(i in checker)) {
                    out.write("(declare-const $i () Int)\n")
                    checker += i
                }
            }
        }

        for((key, value) in termsWithoutVar_3){
            for (i in value) {
                if (!(i in checker)) {
                    out.write("(declare-const $i () Int)\n")
                    checker += i
                }
            }
        }

        for((key, value) in termsWithoutVar_4){
            for (i in value) {
                if (!(i in checker)) {
                    out.write("(declare-const $i () Int)\n")
                    checker += i
                }
            }
        }

        out.write("\n")

        var status1 = true
        var status2 = true
        var status3 = true
        var status4 = true

        if (multipliers_1[variables[0]] == null) { // Если у переменной нет множителей, то заполняем нулями
            multipliers_1[variables[0]] = "0"
        }
        if (multipliers_1[variables[1]] == null) {
            multipliers_1[variables[1]] = "0"
        }
        if (multipliers_2[variables[0]] == null) {
            multipliers_2[variables[0]] = "0"
        }
        if (multipliers_2[variables[1]] == null) {
            multipliers_2[variables[1]] = "0"
        }
        if (multipliers_3[variables[0]] == null) {
            multipliers_3[variables[0]] = "0"
        }
        if (multipliers_3[variables[1]] == null) {
            multipliers_3[variables[1]] = "0"
        }
        if (multipliers_4[variables[0]] == null) {
            multipliers_4[variables[0]] = "0"
        }
        if (multipliers_4[variables[1]] == null) {
            multipliers_4[variables[1]] = "0"
        }

        status1 = statusChecker(multipliers_1, variables[0])
        status2 = statusChecker(multipliers_2, variables[0])
        status3 = statusChecker(multipliers_3, variables[0])
        status4 = statusChecker(multipliers_4, variables[0])

        if ((status1 == true) && (status2 == true)) {
            out.write("(assert (>= ${multipliers_1[variables[0]]} ${multipliers_2[variables[0]]}))\n")
        } else if ((status1 == true) && (status2 == false)) {
            out.write("(assert (>= ${multipliers_1[variables[0]]} (+ ${multipliers_2[variables[0]]})))\n")
        } else if ((status1 == false) && (status2 == true)) {
            out.write("(assert (>= (+ ${multipliers_1[variables[0]]}) ${multipliers_2[variables[0]]}))\n")
        } else {
            out.write("(assert (>= (+ ${multipliers_1[variables[0]]}) (+ ${multipliers_2[variables[0]]})))\n")
        }

        if ((status3 == true) && (status4 == true)) {
            out.write("(assert (>= ${multipliers_3[variables[0]]} ${multipliers_4[variables[0]]}))\n")
        } else if ((status3 == true) && (status4 == false)) {
            out.write("(assert (>= ${multipliers_3[variables[0]]} (+ ${multipliers_4[variables[0]]})))\n")
        } else if ((status3 == false) && (status4 == true)) {
            out.write("(assert (>= (+ ${multipliers_3[variables[0]]}) ${multipliers_4[variables[0]]}))\n")
        } else {
            out.write("(assert (>= (+ ${multipliers_3[variables[0]]}) (+ ${multipliers_4[variables[0]]})))\n")
        }

        status1 = statusChecker(multipliers_1, variables[1])
        status2 = statusChecker(multipliers_2, variables[1])
        status3 = statusChecker(multipliers_3, variables[1])
        status4 = statusChecker(multipliers_4, variables[1])

        if ((status1 == true) && (status2 == true)) {
            out.write("(assert (>= ${multipliers_1[variables[1]]} ${multipliers_2[variables[1]]}))\n")
        } else if ((status1 == true) && (status2 == false)) {
            out.write("(assert (>= ${multipliers_1[variables[1]]} (+ ${multipliers_2[variables[1]]})))\n")
        } else if ((status1 == false) && (status2 == true)) {
            out.write("(assert (>= (+ ${multipliers_1[variables[1]]}) ${multipliers_2[variables[1]]}))\n")
        } else {
            out.write("(assert (>= (+ ${multipliers_1[variables[1]]}) (+ ${multipliers_2[variables[1]]})))\n")
        }

        if ((status3 == true) && (status4 == true)) {
            out.write("(assert (>= ${multipliers_3[variables[1]]} ${multipliers_4[variables[1]]}))\n")
        } else if ((status3 == true) && (status4 == false)) {
            out.write("(assert (>= ${multipliers_3[variables[1]]} (+ ${multipliers_4[variables[1]]})))\n")
        } else if ((status3 == false) && (status4 == true)) {
            out.write("(assert (>= (+ ${multipliers_3[variables[1]]}) ${multipliers_4[variables[1]]}))\n")
        } else {
            out.write("(assert (>= (+ ${multipliers_3[variables[1]]}) (+ ${multipliers_4[variables[1]]})))\n")
        }

        out.write("\n")

        status1 = statusChecker(multipliers_1, "free")
        status2 = statusChecker(multipliers_2, "free")
        status3 = statusChecker(multipliers_3, "free")
        status4 = statusChecker(multipliers_4, "free")

        if ((status1 == true) && (status2 == true)) {
            out.write("(assert (>= ${multipliers_1["free"]} ${multipliers_2["free"]}))\n")
        } else if ((status1 == true) && (status2 == false)) {
            out.write("(assert (>= ${multipliers_1["free"]} (+ ${multipliers_2["free"]})))\n")
        } else if ((status1 == false) && (status2 == true)) {
            out.write("(assert (>= (+ ${multipliers_1["free"]}) ${multipliers_2["free"]}))\n")
        } else {
            out.write("(assert (>= (+ ${multipliers_1["free"]}) (+ ${multipliers_2["free"]})))\n")
        }

        if ((status3 == true) && (status4 == true)) {
            out.write("(assert (>= ${multipliers_3["free"]} ${multipliers_4["free"]}))\n")
        } else if ((status3 == true) && (status4 == false)) {
            out.write("(assert (>= ${multipliers_3["free"]} (+ ${multipliers_4["free"]})))\n")
        } else if ((status3 == false) && (status4 == true)) {
            out.write("(assert (>= (+ ${multipliers_3["free"]}) ${multipliers_4["free"]}))\n")
        } else {
            out.write("(assert (>= (+ ${multipliers_3["free"]}) (+ ${multipliers_4["free"]})))\n")
        }

        out.write("\n(assert (and")
        val check = mutableListOf<String>()

        for((key, value) in termsFirst_1){
            out.write(" (>= ${value} 1)")
            check += value
        }
        for((key, value) in termsFirst_2){
            out.write(" (>= ${value} 1)")
            check += value
        }
        for((key, value) in termsFirst_3){
            out.write(" (>= ${value} 1)")
            check += value
        }
        for((key, value) in termsFirst_4){
            out.write(" (>= ${value} 1)")
            check += value
        }
        for((key, value) in termsWithoutVar_1){
            for (i in value) {
                if (!(i in check)) {
                    out.write(" (>= $i 0)")
                    check += i
                } else {
                    continue
                }
            }
        }
        for((key, value) in termsWithoutVar_2){
            for (i in value) {
                if (!(i in check)) {
                    out.write(" (>= $i 0)")
                    check += i
                } else {
                    continue
                }
            }
        }
        for((key, value) in termsWithoutVar_3){
            for (i in value) {
                if (!(i in check)) {
                    out.write(" (>= $i 0)")
                    check += i
                } else {
                    continue
                }
            }
        }
        for((key, value) in termsWithoutVar_4){
            for (i in value) {
                if (!(i in check)) {
                    out.write(" (>= $i 0)")
                    check += i
                } else {
                    continue
                }
            }
        }

        out.write("))")
        out.write("\n")

        out.write("(or (and")

        status1 = statusChecker(multipliers_1, variables[0])
        status2 = statusChecker(multipliers_2, variables[0])

        if ((status1 == true) && (status2 == true)) { // при иксе
            out.write(" (> ${multipliers_1[variables[0]]} ${multipliers_2[variables[0]]})")
        } else if ((status1 == true) && (status2 == false)) {
            out.write(" (> ${multipliers_1[variables[0]]} (+ ${multipliers_2[variables[0]]}))")
        } else if ((status1 == false) && (status2 == true)) {
            out.write(" (> (+ ${multipliers_1[variables[0]]}) ${multipliers_2[variables[0]]})")
        } else {
            out.write(" (> (+ ${multipliers_1[variables[0]]}) (+ ${multipliers_2[variables[0]]}))")
        }

        status1 = statusChecker(multipliers_1, variables[1])
        status2 = statusChecker(multipliers_2, variables[1])

        if ((status1 == true) && (status2 == true)) { // при игреке
            out.write(" (> ${multipliers_1[variables[1]]} ${multipliers_2[variables[1]]})")
        } else if ((status1 == true) && (status2 == false)) {
            out.write(" (> ${multipliers_1[variables[1]]} (+ ${multipliers_2[variables[1]]}))")
        } else if ((status1 == false) && (status2 == true)) {
            out.write(" (> (+ ${multipliers_1[variables[1]]}) ${multipliers_2[variables[1]]})")
        } else {
            out.write(" (> (+ ${multipliers_1[variables[1]]}) (+ ${multipliers_2[variables[1]]}))")
        }

        out.write(")")

        status1 = statusChecker(multipliers_1, "free")
        status2 = statusChecker(multipliers_2, "free")

        if ((status1 == true) && (status2 == true)) { // при игреке
            out.write(" (> ${multipliers_1["free"]} ${multipliers_1["free"]})")
        } else if ((status1 == true) && (status2 == false)) {
            out.write(" (> ${multipliers_1["free"]} (+ ${multipliers_2["free"]}))")
        } else if ((status1 == false) && (status2 == true)) {
            out.write(" (> (+ ${multipliers_1["free"]}) ${multipliers_2["free"]})")
        } else {
            out.write(" (> (+ ${multipliers_1["free"]}) (+ ${multipliers_2["free"]}))")
        }

        out.write(")\n")

        out.write("(or (and")

        status1 = statusChecker(multipliers_3, variables[0])
        status2 = statusChecker(multipliers_4, variables[0])

        if ((status1 == true) && (status2 == true)) { // при иксе
            out.write(" (> ${multipliers_3[variables[0]]} ${multipliers_4[variables[0]]})")
        } else if ((status1 == true) && (status2 == false)) {
            out.write(" (> ${multipliers_3[variables[0]]} (+ ${multipliers_4[variables[0]]}))")
        } else if ((status1 == false) && (status2 == true)) {
            out.write(" (> (+ ${multipliers_3[variables[0]]}) ${multipliers_4[variables[0]]})")
        } else {
            out.write(" (> (+ ${multipliers_3[variables[0]]}) (+ ${multipliers_4[variables[0]]}))")
        }

        status1 = statusChecker(multipliers_3, variables[1])
        status2 = statusChecker(multipliers_4, variables[1])

        if ((status1 == true) && (status2 == true)) { // при игреке
            out.write(" (> ${multipliers_3[variables[1]]} ${multipliers_4[variables[1]]})")
        } else if ((status1 == true) && (status2 == false)) {
            out.write(" (> ${multipliers_3[variables[1]]} (+ ${multipliers_4[variables[1]]}))")
        } else if ((status1 == false) && (status2 == true)) {
            out.write(" (> (+ ${multipliers_3[variables[1]]}) ${multipliers_4[variables[1]]})")
        } else {
            out.write(" (> (+ ${multipliers_3[variables[1]]}) (+ ${multipliers_4[variables[1]]}))")
        }

        out.write(")")

        status1 = statusChecker(multipliers_3, "free")
        status2 = statusChecker(multipliers_4, "free")

        if ((status1 == true) && (status2 == true)) { // при игреке
            out.write(" (> ${multipliers_3["free"]} ${multipliers_4["free"]})")
        } else if ((status1 == true) && (status2 == false)) {
            out.write(" (> ${multipliers_3["free"]} (+ ${multipliers_4["free"]}))")
        } else if ((status1 == false) && (status2 == true)) {
            out.write(" (> (+ ${multipliers_3["free"]}) ${multipliers_4["free"]})")
        } else {
            out.write(" (> (+ ${multipliers_3["free"]}) (+ ${multipliers_4["free"]}))")
        }

        out.write(")\n")

        out.write("(and (or (and")

        for((key, value) in termsFirst_1) {
            out.write(" (> ${value} 1)")
        }

        out.write(")")

        for((key, value) in termsWithoutVar_1){
            for (i in value) {
                    out.write(" (> $i 0)")
            }
        }

        out.write(")")

        out.write(" (or (and")

        for((key, value) in termsFirst_2) {
            out.write(" (> ${value} 1)")
        }

        out.write(")")

        for((key, value) in termsWithoutVar_2){
            for (i in value) {
                out.write(" (> $i 0)")
            }
        }

        out.write(")")

        out.write(" (or (and")

        for((key, value) in termsFirst_3) {
            out.write(" (> ${value} 1)")
        }

        out.write(")")

        for((key, value) in termsWithoutVar_3){
            for (i in value) {
                out.write(" (> $i 0)")
            }
        }

        out.write(")")

        out.write(" (or (and")

        for((key, value) in termsFirst_4) {
            out.write(" (> ${value} 1)")
        }

        out.write(")")

        for((key, value) in termsWithoutVar_4){
            for (i in value) {
                out.write(" (> $i 0)")
            }
        }

        out.write("))\n")

        out.write("(check-sat)\n")
        out.write("(get-model)\n")
        out.write("(exit)\n")

    }
}

fun statusChecker(multipliers: Map<String, String>, variable: String): Boolean {
    var status = true

    for((key, value) in multipliers){
        if (key == variable) {
            for (i in value) {
                if (i == '(') {
                    status = false
                } else if (i == ')') {
                    status = true
                } else if ((i != '(') && (i != ')') && ('(' in value) && (status == true)) {
                    status = false
                    break
                }
            }
        }
    }

    return status
}