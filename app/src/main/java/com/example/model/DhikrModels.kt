package com.example.model

enum class BracketStyle(val title: String, val exampleFormat: (String) -> String) {
    ROUND("أقواس دائرية ( )", { "($it)" }),
    QURANIC("أقواس قرآنية ﴿ ﴾", { "﴿ $it ﴾" }),
    SQUARE("أقواس معقوفة [ ]", { "[$it]" }),
    NONE("بدون أقواس", { it })
}

enum class ArabicFontType(val title: String, val subtitle: String) {
    AMIRI("خط أميري", "خط نسخي كلاسيكي أصيل وفخم"),
    CAIRO("خط كايرو", "خط عربي حديث واضح وهندسي"),
    SYSTEM("خط النظام", "الخط الافتراضي للجهاز")
}

enum class DisplayTheme(val title: String, val description: String) {
    PARCHMENT("ورق عاجي دافئ", "خلفية ورق كلاسيكية مريحة للعين"),
    ISLAMIC_ART("زخرفة إسلامية ناعمة", "أرضية مزخرفة بنقوش هندسية هادئة"),
    PURE_WHITE("أبيض ناصع نقي", "أعلى درجات التباين والوضوح التام"),
    SAGE_MINT("أخضر مسكي هادئ", "لون مريح هادئ مستوحى من الطبيعة"),
    SOFT_MARBLE("رخام هادئ", "ملمس رخامي عصري ناعم")
}

data class DhikrItem(
    val id: Int,
    val arabicText: String,
    val virtue: String,
    val translation: String
)
