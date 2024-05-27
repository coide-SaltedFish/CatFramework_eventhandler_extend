package org.sereinfish.catcat.framework.eventhandler.extend.utils

/**
 * 中文数字工厂
 */
object CnNumberFactory {
    private val _0 = "[零]"
    private val _1_9 = "[一二三四五六七八九]"
    private val _10_99 = "${_1_9}?[十]${_1_9}?"
    private val _1_99 = "(${_10_99}|${_1_9})"
    private val _100_999 = "(${_1_9}[百](${_0}${_1_9})?|${_1_9}[百]${_10_99})"
    private val _1_999 = "(${_100_999}|${_1_99})"
    private val _1000_9999 = "(${_1_9}[千](${_0}${_1_99})?|${_1_9}[千]${_100_999})"
    private val _1_9999 = "(${_1000_9999}|${_1_999})"
    private val _10000_99999999 = "(${_1_9999}[万](${_0}${_1_999})?|${_1_9999}[万]${_1000_9999})"
    private val _1_99999999 = "(${_10000_99999999}|${_1_9999})"
    private val _100000000_9999999999999999 = "(${_1_99999999}[亿](${_0}${_1_99999999})?|${_1_99999999}[亿]${_10000_99999999})"
    private val _1_9999999999999999 = "(${_100000000_9999999999999999}|${_1_99999999})"

    private val numberMap = mapOf(
        0 to "零",
        1 to "一",
        2 to "二",
        3 to "三",
        4 to "四",
        5 to "五",
        6 to "六",
        7 to "七",
        8 to "八",
        9 to "九"
    )
    private val unitMap = mapOf(
        100000000 to "亿",
        10000 to "万",
        1000 to "千",
        100 to "百",
        10 to "十",
    )

    fun isCnNumber(cn: String): Boolean {
        return "^($_0|$_1_9999999999999999)$".toRegex().matches(cn)
    }

    /**
     * 把数字转化为中文数字
     */
    fun numberToCn(number: Int): String {
        if (number < 0) error("无法处理负数：$number")

        if (number == 0) return _0
        if (number < 10) return numberMap[number]!!
        if (number < 100) {
            return buildString {
                append(numberToCn(number / 10))
                append(unitMap[10])
                append(numberToCn(number % 10))
            }
        }
        if (number < 1000) {
            return buildString {
                append(numberToCn(number / 100))
                append(unitMap[100])
                if (number % 100 < 10) append(numberMap[0])
                append(numberToCn(number % 100))
            }
        }
        if (number < 10000) {
            return buildString {
                append(numberToCn(number / 1000))
                append(unitMap[1000])
                if (number % 1000 < 100) append(numberMap[0])
                append(numberToCn(number % 1000))
            }
        }
        if (number < 100_000_000) {
            return buildString {
                append(numberToCn(number / 10_000))
                append(unitMap[10_000])
                if (number % 10_000 < 1000) append(numberMap[0])
                append(numberToCn(number % 10_000))
            }
        }
        return buildString {
            append(numberToCn(number / 100_000_000))
            append(unitMap[100_000_000])
            if (number % 100_000_000 < 10_000_000) append(numberMap[0])
            append(numberToCn(number % 100_000_000))
        }
    }

    fun cnToNumber(cn: String): Int {
        if (isCnNumber(cn).not()) error("无法识别中文数字：$cn")
        var sumValue = 0
        var value = 0
        val cnToAnUnitMap = unitMap.entries.associateBy({ it.value }) { it.key }
        val cnToAnMap = numberMap.entries.associateBy({ it.value }) { it.key }
        for (n in cn){
            val ns = "$n"
            if (cnToAnUnitMap.containsKey(ns)){
                if (cnToAnUnitMap[ns]!! >= 10000){
                    sumValue += value
                    sumValue *= (cnToAnUnitMap[ns] ?: 1)
                }else {
                    sumValue += value * (cnToAnUnitMap[ns] ?: 1)
                }
                value = 0
            }else if (cnToAnMap.containsKey(ns)) {
                value += cnToAnMap[ns] ?: 0
            }
        }
        return sumValue + value
    }
}