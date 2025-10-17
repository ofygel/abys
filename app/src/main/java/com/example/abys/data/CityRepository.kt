package com.example.abys.data

import java.util.Locale

data class CityEntry(
    val id: String,
    val display: String,
    val aliases: List<String>,
    val tokens: List<String>,
)

object CityRepository {
    val cities: List<CityEntry> = listOf(
        entry("almaty", "Almaty", "Алматы", "Алмата", "Almaty", "Almati"),
        entry("astana", "Astana", "Астана", "Nur-Sultan", "Нур-Султан", "Astana", "Nur Sultan"),
        entry("shymkent", "Shymkent", "Шымкент", "Чимкент", "Şymkent", "Chimkent"),
        entry("karaganda", "Karaganda", "Караганда", "Qaragandy", "Карагандa", "Karagandy"),
        entry("aktobe", "Aktobe", "Актобе", "Ақтөбе", "Aktyube", "Aktjubinsk"),
        entry("aktau", "Aktau", "Актау", "Ақтау", "Aktav", "Shevchenko"),
        entry("atyrau", "Atyrau", "Атырау", "Гурьев", "Atıraw", "Atyraw"),
        entry("kokshetau", "Kokshetau", "Кокшетау", "Көкшетау", "Kokchetav"),
        entry("kostanay", "Kostanay", "Костанай", "Қостанай", "Kustanay", "Qostanai"),
        entry("kyzylorda", "Kyzylorda", "Кызылорда", "Қызылорда", "Kyzyl-Orda", "Qyzylorda"),
        entry("pavlodar", "Pavlodar", "Павлодар", "Pavlodar", "Pavlodarskiy"),
        entry("semey", "Semey", "Семей", "Семипалатинск", "Semipalatinsk", "Semei"),
        entry("oskemen", "Oskemen", "Усть-Каменогорск", "Өскемен", "Ust-Kamenogorsk", "Oskemen"),
        entry("taraz", "Taraz", "Тараз", "Dzhambul", "Jambyl", "Taraz"),
        entry("taldykorgan", "Taldykorgan", "Талдыкорган", "Талдықорған", "Taldy-Kurgan"),
        entry("petropavl", "Petropavl", "Петропавл", "Петропавловск", "Petropavlovsk"),
        entry("uralsk", "Oral", "Уральск", "Орал", "Uralsk", "Oral"),
        entry("ekibastuz", "Ekibastuz", "Экибастуз", "Ekibastus", "Ekibastūz"),
        entry("temirtau", "Temirtau", "Темиртау", "Теміртау", "Temir-Tau"),
        entry("turkistan", "Turkistan", "Туркестан", "Түркістан", "Turkestan"),
        entry("zhezkazgan", "Zhezkazgan", "Жезказган", "Жезқазған", "Dzhezkazgan"),
        entry("balqash", "Balkhash", "Балхаш", "Балқаш", "Balhash"),
        entry("stepnogorsk", "Stepnogorsk", "Степногорск", "Stepnogorsk", "Aksu-Ayuly"),
    ).sortedBy { it.display }

    fun featured(): List<CityEntry> = listOf(
        find("almaty"),
        find("astana"),
        find("shymkent"),
        find("karaganda"),
        find("aktobe"),
        find("turkistan"),
    ).filterNotNull()

    fun search(query: String): List<CityEntry> {
        val normalized = normalize(query)
        if (normalized.length < 3) return emptyList()
        return cities.filter { entry ->
            entry.tokens.any { token -> token.contains(normalized) }
        }
    }

    private fun entry(id: String, display: String, vararg tokens: String): CityEntry =
        CityEntry(
            id = id,
            display = display,
            aliases = buildList {
                add(display)
                tokens.mapTo(this) { it.trim() }
            }.filter { it.isNotBlank() }.distinct(),
            tokens = buildList {
                add(normalize(display))
                tokens.mapTo(this) { normalize(it) }
            }.distinct()
        )

    private fun find(id: String): CityEntry? = cities.firstOrNull { it.id == id }

    private fun normalize(value: String): String {
        val lower = value.lowercase(Locale.getDefault())
        return buildString(lower.length) {
            lower.forEach { ch ->
                append(
                    when (ch) {
                        'ё' -> 'е'
                        'ү', 'ұ' -> 'у'
                        'қ' -> 'к'
                        'ғ' -> 'г'
                        'ң' -> 'н'
                        'һ' -> 'х'
                        'ә' -> 'а'
                        'і' -> 'и'
                        else -> ch
                    }
                )
            }
        }
    }
}
