package app.gossamer.data

import android.content.Context
import app.gossamer.AppContainer
import app.gossamer.domain.Web
import app.gossamer.ru
import java.time.LocalDate
import java.time.ZoneId

/** The demo build's web: a month of thoughts, a few hubs, a lure, dew, a ripe cocoon and two butterflies. */
object DemoSeed {
    private class D(val en: String, val ru: String, val kind: Kind, val daysAgo: Int, val hour: Int)

    private val THOUGHTS = listOf(
        D("What would I do with a free Saturday if nobody expected anything?", "Что бы я делал в свободную субботу, если бы никто ничего не ждал?", Kind.QUESTION, 26, 22),
        D("A bakery that only sells yesterday’s bread, cheap, with a story on each loaf", "Пекарня, которая продаёт только вчерашний хлеб, дёшево, с историей на каждом батоне", Kind.IDEA, 25, 9),
        D("The old man on the tram reading a paper map of the city", "Старик в трамвае читал бумажную карту города", Kind.NOTICE, 24, 8),
        D("“We don’t see things as they are, we see them as we are.”", "«Мы видим вещи не такими, какие они есть, а такими, какие есть мы»", Kind.WORDS, 23, 19),
        D("Write to Lena: the book she lent me is still on my shelf", "Написать Лене: её книга до сих пор у меня на полке", Kind.NOTICE, 21, 13),
        D("Why do I always start projects in autumn?", "Почему я всегда начинаю проекты осенью?", Kind.QUESTION, 19, 23),
        D("A walking tour of courtyards, not streets", "Экскурсия по дворам, а не по улицам", Kind.IDEA, 18, 17),
        D("Courtyards keep the oldest trees: no one dared to cut them", "Во дворах растут самые старые деревья: их никто не решился спилить", Kind.NOTICE, 17, 12),
        D("Map of the city by smells: bakery corner, river, the tram depot", "Карта города по запахам: угол с пекарней, река, трамвайное депо", Kind.IDEA, 16, 2),
        D("Cards with a question on one side and a courtyard on the other", "Карточки: на одной стороне вопрос, на другой двор", Kind.IDEA, 15, 21),
        D("Grandma said: a house is warm when someone waits in it", "Бабушка говорила: в доме тепло, когда в нём кого-то ждут", Kind.WORDS, 14, 20),
        D("Should I go part-time and do the courtyard tours properly?", "Перейти на полставки и заняться экскурсиями по дворам всерьёз?", Kind.QUESTION, 12, 23),
        D("Ask the house museum on Lesnaya about old photos of the yard", "Спросить дом-музей на Лесной про старые фото двора", Kind.NOTICE, 11, 11),
        D("Tours at dusk, when windows light up one by one", "Экскурсии в сумерках, когда окна загораются одно за другим", Kind.IDEA, 9, 18),
        D("The light in windows at dusk is the best part of the city", "Свет в окнах в сумерках — лучшее, что есть в городе", Kind.NOTICE, 8, 18),
        D("A tea that tastes like the first cold morning", "Чай со вкусом первого холодного утра", Kind.IDEA, 7, 7),
        D("I keep coming back to courtyards. Maybe that’s it.", "Я всё время возвращаюсь к дворам. Может, это оно.", Kind.NOTICE, 5, 1),
        D("Make a list of ten courtyards worth showing", "Составить список десяти дворов, которые стоит показать", Kind.IDEA, 4, 10),
        D("“Begin anywhere.” John Cage", "«Начни с чего угодно». Джон Кейдж", Kind.WORDS, 3, 15),
        D("The neighbour’s cat sleeps exactly where the sun hits the radiator", "Соседский кот спит ровно там, где солнце падает на батарею", Kind.NOTICE, 2, 14),
        D("A podcast where people describe their window view", "Подкаст, где люди описывают вид из своего окна", Kind.IDEA, 1, 9),
        D("Tours at dusk, when the windows light up", "Экскурсии в сумерках, когда загораются окна", Kind.IDEA, 0, 2),
        D("What is the smallest version of the tour I could run this month?", "Какую самую маленькую версию экскурсии я могу провести в этом месяце?", Kind.QUESTION, 0, 3),
        D("Rain on the bus window turns the street into watercolour", "Дождь на окне автобуса превращает улицу в акварель", Kind.NOTICE, 0, 8),
    )

    private val INSIDE_RIPE = listOf(
        D("A phone-free hour every evening", "Час без телефона каждый вечер", Kind.IDEA, 40, 21),
        D("Why am I most tired on days I did nothing?", "Почему я устаю сильнее всего в дни, когда ничего не делал?", Kind.QUESTION, 38, 22),
        D("Walked home the long way and felt better than after a nap", "Пошёл домой длинной дорогой, и стало лучше, чем после сна", Kind.NOTICE, 36, 19),
        D("“Rest is not idleness.”", "«Отдых — это не безделье»", Kind.WORDS, 35, 20),
        D("Rest that makes something: bread, a walk, a letter", "Отдых, после которого что-то остаётся: хлеб, прогулка, письмо", Kind.IDEA, 33, 18),
    )

    private val BUTTERFLY_1 = listOf(
        D("Learn to draw the street I live on", "Научиться рисовать улицу, на которой живу", Kind.IDEA, 90, 20),
        D("Sketchbook in the coat pocket, not on the shelf", "Скетчбук в кармане пальто, а не на полке", Kind.NOTICE, 88, 9),
        D("Ten minutes a day, ugly allowed", "Десять минут в день, некрасиво можно", Kind.IDEA, 85, 21),
        D("What do I draw when I don’t think?", "Что я рисую, когда не думаю?", Kind.QUESTION, 84, 23),
        D("“The way to get started is to quit talking and begin doing.”", "«Чтобы начать, надо перестать говорить и начать делать»", Kind.WORDS, 80, 12),
        D("Drew the bakery. Badly. Happily.", "Нарисовал пекарню. Плохо. Счастливо.", Kind.NOTICE, 78, 18),
    )

    private val BUTTERFLY_2 = listOf(
        D("Call dad on Sundays, not when something happens", "Звонить папе по воскресеньям, а не когда что-то случилось", Kind.IDEA, 70, 20),
        D("He tells the same story about the boat. I like it.", "Он рассказывает одну и ту же историю про лодку. Мне нравится.", Kind.NOTICE, 66, 21),
        D("What did he want to be at my age?", "Кем он хотел быть в моём возрасте?", Kind.QUESTION, 64, 22),
        D("Record his voice telling the boat story", "Записать его голос, как он рассказывает про лодку", Kind.IDEA, 60, 19),
        D("“Ask while you can.”", "«Спрашивай, пока можно»", Kind.WORDS, 58, 13),
    )

    private val FLUFF = listOf(
        D("App for tracking houseplants", "Приложение для учёта комнатных растений", Kind.IDEA, 70, 15),
        D("Buy a better umbrella", "Купить зонт получше", Kind.NOTICE, 64, 9),
        D("Is it worth learning Portuguese?", "Стоит ли учить португальский?", Kind.QUESTION, 50, 22),
    )

    private fun at(d: D, zone: ZoneId): Long =
        LocalDate.now(zone).minusDays(d.daysAgo.toLong()).atTime(d.hour, (d.daysAgo * 7) % 60).atZone(zone).toInstant().toEpochMilli()

    suspend fun seedIfEmpty(context: Context, c: AppContainer) {
        if (c.web.thoughtList().isNotEmpty()) return
        val ru = context.ru
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        suspend fun put(d: D, cocoon: Long? = null, gone: Long? = null, touchedDaysAgo: Int? = null): Long {
            val t = minOf(at(d, zone), now - 60_000)
            return c.web.insert(Thought(text = if (ru) d.ru else d.en, kind = d.kind, at = t, touched = touchedDaysAgo?.let { now - it * DAY } ?: t, night = Web.isNight(t, zone), cocoon = cocoon, gone = gone))
        }
        suspend fun chain(ids: List<Long>, at: Long) { for (i in 1 until ids.size) c.web.tie(Strand.of(ids[i - 1], ids[i], at)); c.web.tie(Strand.of(ids.first(), ids.last(), at)) }

        val b1 = c.web.spin(Cocoon(name = if (ru) "Рисовать улицу" else "Drawing my street", at = now - 75 * DAY, ripe = now - 72 * DAY, hatched = now - 71 * DAY,
            verdict = if (ru) "Рисую десять минут в день, и улица стала моей" else "Ten minutes a day, and the street became mine", seed = 41))
        chain(BUTTERFLY_1.map { put(it, cocoon = b1) }, now - 75 * DAY)
        val b2 = c.web.spin(Cocoon(name = if (ru) "Папа и лодка" else "Dad and the boat", at = now - 55 * DAY, ripe = now - 52 * DAY, hatched = now - 50 * DAY,
            verdict = if (ru) "Каждое воскресенье звоню и записываю одну историю" else "Every Sunday I call and record one story", seed = 1337))
        chain(BUTTERFLY_2.map { put(it, cocoon = b2) }, now - 55 * DAY)
        val ripe = c.web.spin(Cocoon(name = if (ru) "Отдых, который наполняет" else "Rest that fills you up", at = now - 4 * DAY, ripe = now - DAY, seed = 99))
        chain(INSIDE_RIPE.map { put(it, cocoon = ripe) }, now - 4 * DAY)
        for (d in FLUFF) put(d, gone = now - (d.daysAgo - 31) * DAY)

        val ids = THOUGHTS.mapIndexed { i, d -> put(d, touchedDaysAgo = when (i) { 2, 5 -> 23; 19 -> null; else -> null }) }
        fun id(i: Int) = ids[i]
        // The courtyards: a hub with a cluster round it.
        val tie = listOf(6 to 7, 6 to 9, 6 to 13, 6 to 17, 6 to 11, 13 to 14, 9 to 0, 11 to 22, 16 to 6, 8 to 1, 8 to 6, 3 to 10, 20 to 14, 15 to 1, 18 to 22)
        for ((a, b) in tie) c.web.tie(Strand.of(id(a), id(b), now - 2 * DAY))
        // The repeat at night: tied by the spider on a thicker thread.
        c.web.tie(Strand.of(id(13), id(21), now - 3 * HOUR, echo = true))
        c.web.thought(id(13))?.let { c.web.update(it.copy(echoes = 1)) }

        val today = Web.day(now, zone)
        c.settings.update {
            it.copy(
                onboarded = true, started = now - 95 * DAY, lure = id(11),
                dewDay = today, dew = listOf(id(4), id(12), id(10)), dewDone = emptySet(),
                year = Web.year(now, zone), yearPicks = listOf(id(16), id(10), id(1)),
            )
        }
        c.refreshOutside()
    }
}
