package io.github.vlsi.bugzilla.dbexport

/**
 * Builds a regular expression that matches all the given strings.
 * @param strings strings to match, the list MUST be sorted in ascending order
 */
fun regexpForStrings(list: List<String>, start: Int = 0, end: Int = list.size): String =
    regexpForStrings(list, start, end, 0)

private val TRIVIAL_STRING = Regex("[-=0-da-zA-Z_\\s#%]+")

private fun String.escapeIfNeeded(): String =
    if (TRIVIAL_STRING.matches(this)) this else Regex.escape(this)

private fun regexpForStrings(list: List<String>, start: Int, end: Int, startLetter: Int): String {
    if (start == end + 1) {
        return list[start].escapeIfNeeded()
    }
    val first = list[start]
    val last = list[end - 1]
    if (startLetter == first.length && first == last) {
        return ""
    }
    if (startLetter < first.length && startLetter < last.length && first[startLetter] == last[startLetter]) {
        var pos = startLetter
        while (pos < first.length && pos < last.length && first[pos] == last[pos]) {
            pos++
        }
        // We have common prefix, and split the rest
        return first.substring(startLetter, pos).escapeIfNeeded() +
                regexpForStrings(list, start, end, pos)
    }

    val alternatives = mutableListOf<String>()

    var i = start
    var j = end
    var optional = false
    while (list[i].length <= startLetter) {
        i++
        optional = true
    }
    while (j > i && list[j - 1].length <= startLetter) {
        j--
        optional = true
    }
    while (i < end) {
        val firstLetter = list[i][startLetter]
        val nextLetter =
            -1 - list.binarySearch(fromIndex = i, toIndex = end) { if (it[startLetter] > firstLetter) 1 else -1 }
        alternatives.add(regexpForStrings(list, i, nextLetter, startLetter))
        i = nextLetter
    }
    return when {
        alternatives.size == 1 ->
            alternatives[0].let {
                when {
                    optional -> if (it.endsWith("?")) "(?:$it)?" else "$it?"
                    else -> it
                }
            }

        alternatives.all { it.length == 1 } ->
            alternatives.joinToString("", "[", if (optional) "]?" else "]")

        else -> alternatives.joinToString("|", "(?>", if (optional) ")?" else ")")
    }
}
