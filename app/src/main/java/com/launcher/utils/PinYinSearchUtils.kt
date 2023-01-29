package com.launcher.utils

import com.github.promeg.pinyinhelper.Pinyin
import com.github.promeg.pinyinhelper.PinyinMapDict

object PinYinSearchUtils {

    init {
        // Add custom dictionary for last launcher scenario,
        // which means we manually put some words used frequently in Chinese Apps' names to correct PinYin-s.
        Pinyin.init(
            Pinyin.newConfig()
                .with(object : PinyinMapDict() {
                    override fun mapping(): Map<String, Array<String>> {
                        val map = HashMap<String, Array<String>>()
                        map["电话薄"] = arrayOf("DIAN", "HUA", "BU")
                        map["電話簿"] = arrayOf("DIAN", "HUA", "BU")
                        map["音乐"] = arrayOf("YIN", "YUE")
                        map["音樂"] = arrayOf("YIN", "YUE")
                        map["银行"] = arrayOf("YIN", "HANG")
                        map["銀行"] = arrayOf("YIN", "HANG")
                        map["帕弥什"] = arrayOf("PA", "MI", "SHI")
                        map["果壳"] = arrayOf("GUO", "KE")
                        map["果殼"] = arrayOf("GUO", "KE")
                        map["重庆"] = arrayOf("CHONG", "QING")
                        map["重慶"] = arrayOf("CHONG", "QING")
                        map["重返"] = arrayOf("CHONG", "FAN")
                        map["东阿"] = arrayOf("DONG", "E")
                        map["東阿"] = arrayOf("DONG", "E")
                        map["番禺"] = arrayOf("PAN", "YU")
                        return map
                    }
                })
        )
    }

    /**
     * Converts the input string to pinyin, using the user dictionary you set up earlier,
     * and inserts separators in character units.
     * For example, when the separator is ",", given the input "hello:中国",
     * this method will output "h,e,l,l,o,:,ZHONG,GUO,!"
     *
     * @param str       input string
     * @param separator separator
     * @return converted string from Chinese to Pinyin.
     */
    private fun toPinyin(
        str: String?,
        separator: String?
    ): String {
        return Pinyin.toPinyin(
            str,
            separator
        ) //This is a wrapper method for om.github.promeg.pinyinhelper.Pinyin.toPinyin .
    }

    /**
     * convert the input character to Pinyin
     * @param c input character
     * @return return pinyin if c is chinese in uppercase, String.valueOf(c) otherwise.
     */
    @Suppress("unused")
    fun toPinyin(c: Char): String {
        return Pinyin.toPinyin(c) //This is a wrapper method for om.github.promeg.pinyinhelper.Pinyin.toPinyin .
    }

    /**
     * @param query   pattern string
     * @param strings text string
     * @return true if @query is sequentially found in @strings else false, and @query can be Chinese Pinyin .
     * For example, "LL" is sequentially found in "Last Launcher", so return true;
     * "yyds" is sequentially found in "你是永远滴神", so it is also true.
     * This method supports Chinese Pinyin Search with "Duoyinzi".
     * For example, "yin yue" matches "音乐" while "yin le" doesn't.
     */
    @JvmStatic
    fun pinYinSimpleFuzzySearch(
        query: CharSequence,
        strings: String?
    ): Boolean {
        return Utils.simpleFuzzySearch(
            toPinyin(query.toString().replace("\\s+".toRegex(), ""), ""),
            toPinyin(strings, "")
        )
    }

}
