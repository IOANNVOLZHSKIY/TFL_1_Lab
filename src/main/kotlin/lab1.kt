import java.io.File

data class Term(
    val multipliers_temp: MutableMap<String, String>,
    val multipliersCount_temp: MutableMap<String, Int>,
    val termsWithVar_temp: MutableMap<Char, List<String>>,
    val termsWithoutVar_temp: MutableMap<Char, List<String>>,
    val termsFirst_temp: MutableMap<Char, String>
)

var multipliers = mutableListOf<MutableList<MutableMap<String, String>>>()// Множетели у переменных и свободного члена. Ключ - переменная или "free", если свободная
var multipliersCount = mutableListOf<MutableList<MutableMap<String, Int>>>() // Количество множетелей у переменной
var termsWithVar = mutableListOf<MutableList<MutableMap<Char, List<String>>>>() // Мапа термов при переменных у конструкторов, после разложения в линейные
var termsWithoutVar = mutableListOf<MutableList<MutableMap<Char, List<String>>>>() // Мапа термов при свободных членах у конструкторов, после разложения в линейные
var termsFirst = mutableListOf<MutableList<MutableMap<Char, String>>>()

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

fun multiplier(part: List<String>, variables: List<String>): Term {

    val multipliers_temp = mutableMapOf<String, String>() // Множетели у переменных и свободного члена. Ключ - переменная или "free", если свободная
    val multipliersCount_temp = mutableMapOf<String, Int>() // Количество множетелей у переменной
    val termsWithVar_temp = mutableMapOf<Char, List<String>>() // Мапа термов при переменных у конструкторов, после разложения в линейные
    val termsWithoutVar_temp = mutableMapOf<Char, List<String>>() // Мапа термов при свободных членах у конструкторов, после разложения в линейные
    val termsFirst_temp = mutableMapOf<Char, String>()

    for (i in 0..(part.size - 1)) {

        if (part[i].takeLast(1) in variables) { // если слагаемое содержит на конце переменную

            var temp = ""
            var temp_terms = ""

            for (t in 0 .. (part[i].length - 2)) {

                if (part[i][t] != '*') { // до знака умножения идет один множитель
                    temp += part[i][t]
                    temp_terms += part[i][t]
                } else {

                    if ((termsWithVar_temp[temp_terms[0]] != null) && (termsWithVar_temp[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                        termsWithVar_temp[temp_terms[0]] = termsWithVar_temp[temp_terms[0]]!! + temp_terms
                    } else if (termsWithVar_temp[temp_terms[0]] == null) {
                        termsWithVar_temp[temp_terms[0]] = listOf(temp_terms)
                    }

                    if (multipliersCount_temp[part[i].takeLast(1)] != null) {
                        multipliersCount_temp[part[i].takeLast(1)] = multipliersCount_temp[part[i].takeLast(1)]!! + 1
                    } else {
                        multipliersCount_temp[part[i].takeLast(1)] = 1
                    }

                    if (!(part[i][t + 1].toString() in variables)) {
                        temp += " " // пробел для записи другого множителя
                    } else {
                        termsFirst_temp[part[i][t + 1]] = temp_terms
                        if ((multipliers_temp[part[i].takeLast(1)] == null) && (multipliersCount_temp[part[i].takeLast(1)]!! > 1)) {
                            multipliers_temp[part[i].takeLast(1)] = "(* " + temp + ")"
                            multipliersCount_temp[part[i].takeLast(1)] = 0
                        } else if ((multipliers_temp[part[i].takeLast(1)] == null) && (multipliersCount_temp[part[i].takeLast(1)]!! == 1)) {
                            multipliers_temp[part[i].takeLast(1)] = temp
                        } else if ((multipliers_temp[part[i].takeLast(1)] != null) && (multipliersCount_temp[part[i].takeLast(1)]!! > 1)){
                            multipliers_temp[part[i].takeLast(1)] = multipliers_temp[part[i].takeLast(1)] + " (* " + temp + ")"
                            multipliersCount_temp[part[i].takeLast(1)] = 0
                        } else if ((multipliers_temp[part[i].takeLast(1)] != null) && (multipliersCount_temp[part[i].takeLast(1)]!! == 1)){
                            multipliers_temp[part[i].takeLast(1)] = multipliers_temp[part[i].takeLast(1)] + " " + temp
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

            for (t in 0 .. (part[i].length - 1)) {

                if ((part[i][t] != '*') && (!(part[i][t].toString() == part[i].takeLast(1)))) { // до знака умножения идет один множитель
                    temp += part[i][t]
                    temp_terms += part[i][t]
                } else if (part[i][t].toString() == part[i].takeLast(1)) {

                    temp += part[i][t]
                    temp_terms += part[i][t]

                    if (multipliersCount_temp["free"] != null) {
                        multipliersCount_temp["free"] = multipliersCount_temp["free"]!! + 1
                    } else {
                        multipliersCount_temp["free"] = 1
                    }

                    if (statusOfMultiply == true) {
                        if ((termsWithoutVar_temp[temp_terms[0]] != null) && (termsWithVar_temp[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                            termsWithoutVar_temp[temp_terms[0]] = termsWithoutVar_temp[temp_terms[0]]!! + temp_terms
                        } else if (termsWithoutVar_temp[temp_terms[0]] == null) {
                            termsWithoutVar_temp[temp_terms[0]] = listOf(temp_terms)
                        }
                    } else {
                        if ((termsWithVar_temp[temp_terms[0]] != null) && (termsWithVar_temp[temp_terms[0]]?.contains(temp_terms) == false)) { // заносим в мапу термов, если его еще нет внутри
                            termsWithVar_temp[temp_terms[0]] = termsWithVar_temp[temp_terms[0]]!! + temp_terms
                        } else if (termsWithVar_temp[temp_terms[0]] == null) {
                            termsWithVar_temp[temp_terms[0]] = listOf(temp_terms)
                        }
                    }

                    if ((multipliers_temp["free"] == null) && (multipliersCount_temp["free"]!! > 1)) {
                        multipliers_temp["free"] = "(* " + temp + ")"
                        multipliersCount_temp["free"] = 0
                    } else if ((multipliers_temp["free"] == null) && (multipliersCount_temp["free"]!! == 1)) {
                        multipliers_temp["free"] = temp
                    } else if ((multipliers_temp["free"] != null) && (multipliersCount_temp["free"]!! > 1)){
                        multipliers_temp["free"] = multipliers_temp["free"] + " (* " + temp + ")"
                        multipliersCount_temp["free"] = 0
                    } else if ((multipliers_temp["free"] != null) && (multipliersCount_temp["free"]!! == 1)){
                        multipliers_temp["free"] = multipliers_temp["free"] + " " + temp
                    }

                    temp = ""

                } else { // попалось умножение

                    statusOfMultiply = false

                    if ((termsWithVar_temp[temp_terms[0]] != null) && (termsWithVar_temp[temp_terms[0]]?.contains(temp_terms) == false)) {
                        // заносим в мапу термов, если его еще нет внутри
                        termsWithVar_temp[temp_terms[0]] = termsWithVar_temp[temp_terms[0]]!! + temp_terms
                    } else if (termsWithVar_temp[temp_terms[0]] == null) {
                        termsWithVar_temp[temp_terms[0]] = listOf(temp_terms)
                    }

                    temp_terms = ""

                    if (multipliersCount_temp["free"] != null) {
                        multipliersCount_temp["free"] = multipliersCount_temp["free"]!! + 1
                    } else {
                        multipliersCount_temp["free"] = 1
                    }

                    temp += " " // пробел для записи другого множителя
                }
            }

        }
    }

    return Term(multipliers_temp, multipliersCount_temp, termsWithVar_temp, termsWithoutVar_temp, termsFirst_temp)
}

fun main() {

    val inputLines = mutableListOf<String>() // список TRS по строкам

    println("Enter variables (for example, variables = x, y)")
    val input_var = readLine().toString()

    while (true) {
        print("Enter TRS (for example, f(g(x, y)) -> g(h(y), x)) or empty string to end reading process:\n")
        val input = readLine() ?: ""

        if (input.isEmpty()) {
            break
        }

        inputLines.add(input)
    }

    var parts = input_var.split(" = ")[1].split(", ")
    val variables = parts.toMutableList() // список переменных

    var trs = mutableListOf<List<List<String>?>>() //TRS по строкам, разложенные на левую и правую часть

    for (i in 0 until inputLines.size) {
        parts = inputLines[i].split(" -> ")
        trs += listOf(
            parser(parts[0].trim(), variables),
            parser(parts[1].trim(), variables)
        )
    }

    multipliers = mutableListOf()
    multipliersCount = mutableListOf()
    termsWithVar = mutableListOf()
    termsWithoutVar = mutableListOf()
    termsFirst = mutableListOf()

    for (i in 0 until trs.size) { // перебор по строкам
        multipliers.add(mutableListOf())
        multipliersCount.add(mutableListOf())
        termsWithVar.add(mutableListOf())
        termsWithoutVar.add(mutableListOf())
        termsFirst.add(mutableListOf())
        for (t in 0 until trs[i].size) { // перебор по левым и правым частям
            multipliers[i].add(mutableMapOf())
            multipliersCount[i].add(mutableMapOf())
            termsWithVar[i].add(mutableMapOf())
            termsWithoutVar[i].add(mutableMapOf())
            termsFirst[i].add(mutableMapOf())
            val result = trs[i][t]?.let { multiplier(it, variables) }
            if (result != null) {
                multipliers[i][t] = result.multipliers_temp
                multipliersCount[i][t] = result.multipliersCount_temp
                termsWithVar[i][t] = result.termsWithVar_temp
                termsWithoutVar[i][t] = result.termsWithoutVar_temp
                termsFirst[i][t] = result.termsFirst_temp
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

        for (i in 0 until termsWithVar.size) {
            for (t in 0 until termsWithVar[i].size) {
                for((key, value) in termsWithVar[i][t]){
                    for (g in value) {
                        if (!(g in checker)) {
                            out.write("(declare-const $g () Int)\n")
                            checker += g
                        }
                    }
                }
            }
        }

        for (i in 0 until termsWithoutVar.size) {
            for (t in 0 until termsWithoutVar[i].size) {
                for((key, value) in termsWithoutVar[i][t]){
                    for (g in value) {
                        if (!(g in checker)) {
                            out.write("(declare-const $g () Int)\n")
                            checker += g
                        }
                    }
                }
            }
        }

        out.write("\n")

        var status1 = true
        var status2 = true
        var status3 = true
        var status4 = true

        for (i in 0 until multipliers.size) {
            for (t in 0 until multipliers[i].size) {
                if (multipliers[i][t][variables[0]] == null) { // Если у переменной нет множителей, то заполняем нулями
                    multipliers[i][t][variables[0]] = "0"
                }
                if ((variables.size > 1) && (multipliers[i][t][variables[1]] == null)) {
                    multipliers[i][t][variables[1]] = "0"
                }
            }
        }

        for (i in 0 until multipliers.size) {
            for (t in 0 until variables.size) {
                status1 = statusChecker(multipliers[i][0], variables[t])
                status2 = statusChecker(multipliers[i][1], variables[t])

                if ((status1 == true) && (status2 == true)) {
                    out.write("(assert (>= ${multipliers[i][0][variables[t]]} ${multipliers[i][1][variables[t]]}))\n")
                } else if ((status1 == true) && (status2 == false)) {
                    out.write("(assert (>= ${multipliers[i][t][variables[t]]} (+ ${multipliers[i][1][variables[t]]})))\n")
                } else if ((status1 == false) && (status2 == true)) {
                    out.write("(assert (>= (+ ${multipliers[i][0][variables[t]]}) ${multipliers[i][1][variables[t]]}))\n")
                } else {
                    out.write("(assert (>= (+ ${multipliers[i][0][variables[t]]}) (+ ${multipliers[i][1][variables[t]]})))\n")
                }
            }
        }

        out.write("\n")

        for (i in 0 until multipliers.size) {
            status1 = statusChecker(multipliers[i][0], "free")
            status2 = statusChecker(multipliers[i][1], "free")

            if ((status1 == true) && (status2 == true)) {
                out.write("(assert (>= ${multipliers[i][0]["free"]} ${multipliers[i][1]["free"]}))\n")
            } else if ((status1 == true) && (status2 == false)) {
                out.write("(assert (>= ${multipliers[i][0]["free"]} (+ ${multipliers[i][1]["free"]})))\n")
            } else if ((status1 == false) && (status2 == true)) {
                out.write("(assert (>= (+ ${multipliers[i][0]["free"]}) ${multipliers[i][1]["free"]}))\n")
            } else {
                out.write("(assert (>= (+ ${multipliers[i][0]["free"]}) (+ ${multipliers[i][1]["free"]})))\n")
            }
        }

        out.write("\n(assert (and")
        val check = mutableListOf<String>()

        for (i in 0 until termsFirst.size) {
            for (t in 0 until termsFirst[i].size) {
                for((key, value) in termsFirst[i][t]){
                    out.write(" (>= ${value} 1)")
                    check += value
                }
            }
        }

        for (i in 0 until termsWithoutVar.size) {
            for (t in 0 until termsWithoutVar[i].size) {
                for((key, value) in termsWithoutVar[i][t]){
                    for (g in value) {
                        if (!(g in check)) {
                            out.write(" (>= $g 0)")
                            check += g
                        } else {
                            continue
                        }
                    }
                }
            }
        }

        out.write("))")
        out.write("\n")

        out.write("(assert ")

        for (i in 0 until multipliers.size) {
            out.write("(or (and")

            status1 = statusChecker(multipliers[i][0], variables[0])
            status2 = statusChecker(multipliers[i][1], variables[0])

            if ((status1 == true) && (status2 == true)) {
                out.write(" (> ${multipliers[i][0][variables[0]]} ${multipliers[i][1][variables[0]]})")
            } else if ((status1 == true) && (status2 == false)) {
                out.write(" (> ${multipliers[i][0][variables[0]]} (+ ${multipliers[i][1][variables[0]]}))")
            } else if ((status1 == false) && (status2 == true)) {
                out.write(" (> (+ ${multipliers[i][0][variables[0]]}) ${multipliers[i][1][variables[0]]})")
            } else {
                out.write(" (> (+ ${multipliers[i][0][variables[0]]}) (+ ${multipliers[i][1][variables[0]]}))")
            }

            if (variables.size > 1) {
                status1 = statusChecker(multipliers[i][0], variables[1])
                status2 = statusChecker(multipliers[i][1], variables[1])

                if ((status1 == true) && (status2 == true)) { // при иксе
                    out.write(" (> ${multipliers[i][0][variables[1]]} ${multipliers[i][1][variables[1]]})")
                } else if ((status1 == true) && (status2 == false)) {
                    out.write(" (> ${multipliers[i][0][variables[1]]} (+ ${multipliers[i][1][variables[1]]}))")
                } else if ((status1 == false) && (status2 == true)) {
                    out.write(" (> (+ ${multipliers[i][0][variables[1]]}) ${multipliers[i][1][variables[1]]})")
                } else {
                    out.write(" (> (+ ${multipliers[i][0][variables[1]]}) (+ ${multipliers[i][1][variables[1]]}))")
                }
            }

            out.write(")")

            status1 = statusChecker(multipliers[i][0], "free")
            status2 = statusChecker(multipliers[i][1], "free")

            if ((status1 == true) && (status2 == true)) { // при иксе
                out.write(" (> ${multipliers[i][0]["free"]} ${multipliers[i][1]["free"]})")
            } else if ((status1 == true) && (status2 == false)) {
                out.write(" (> ${multipliers[i][0]["free"]} (+ ${multipliers[i][1]["free"]}))")
            } else if ((status1 == false) && (status2 == true)) {
                out.write(" (> (+ ${multipliers[i][0]["free"]}) ${multipliers[i][1]["free"]})")
            } else {
                out.write(" (> (+ ${multipliers[i][0]["free"]}) (+ ${multipliers[i][1]["free"]}))")
            }

            out.write("))\n")
        }

        out.write("(assert (and")

        for (i in 0 until inputLines.size) {
            for (t in 0 until 2) {
                out.write(" (or (and")
                for((key, value) in termsFirst[i][t]) {
                    out.write(" (> ${value} 1)")
                }

                out.write(")")

                for((key, value) in termsWithoutVar[i][t]){
                    for (g in value) {
                        out.write(" (> $g 0)")
                    }
                }

                out.write(")")
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