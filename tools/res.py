"""Writes the string resources (English and Russian) from one table, so the two never drift apart.

python tools/res.py
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "app/src/main/res"

S = [
    ("app_name", "Gossamer", "Паутинка"),
    ("app_title", "Gossamer", "Паутинка"),
    ("tab_web", "Web", "Паутина"),
    ("tab_thoughts", "Thoughts", "Мысли"),
    ("tab_collection", "Butterflies", "Бабочки"),
    ("settings", "Settings", "Настройки"),
    ("next", "Next", "Дальше"),
    ("back", "Back", "Назад"),
    ("start", "Start", "Начать"),
    ("cancel", "Cancel", "Отмена"),
    ("save", "Save", "Сохранить"),
    ("edit", "Edit", "Исправить"),
    ("delete", "Delete", "Удалить"),
    ("done", "Done", "Готово"),
    ("on", "On", "Включён"),
    ("off", "Off", "Выключен"),
    ("all", "All", "Все"),
    ("d_today", "today, %1$s", "сегодня, %1$s"),
    ("d_yesterday", "yesterday, %1$s", "вчера, %1$s"),
    # onboarding
    ("ob1_t", "Catch it before it flies off", "Поймай, пока не улетела"),
    ("ob1_b", "A thought comes and goes in a second. Write it down here in two: from the widget, the tile in the quick settings, «Share» in any app, or by voice. No titles, no folders. It sticks to the web, and that’s it.",
     "Мысль приходит и уходит за секунду. Запиши её сюда за две: с виджета, из плитки в шторке, через «Поделиться» в любом приложении или голосом. Без заголовков и папок. Она просто прилипнет к паутине."),
    ("ob2_t", "Pull the threads", "Тяни нити"),
    ("ob2_b", "Press a thought and drag a thread to another. The more threads, the closer it moves to the middle: that’s how your real themes show. Every morning dew falls on a few old thoughts. Untied ones the wind takes away after a month.",
     "Зажми мысль и протяни нить к другой. Чем больше нитей, тем ближе она к центру: так проступают твои настоящие темы. Каждое утро на несколько старых мыслей выпадает роса. Непривязанные мысли через месяц уносит ветер."),
    ("ob3_t", "Let a butterfly out", "Выпусти бабочку"),
    ("ob3_b", "When five thoughts or more hang together, spin them into a cocoon. In three days it ripens: reread what’s inside, sum it up in one sentence, and a butterfly flies out. The collection of butterflies is the thoughts that grew into something.",
     "Когда вместе держатся пять мыслей или больше, сверни их в кокон. Через три дня он созреет: перечитай, что внутри, скажи одной фразой, к чему всё это, и вылетит бабочка. Коллекция бабочек — это мысли, из которых что-то выросло."),
    # web
    ("web_a11y", "The web: %1$d thoughts. Tap one to open it, press and drag to tie it to another.",
     "Паутина: мыслей %1$d. Нажми на мысль, чтобы открыть её. Зажми и веди к другой, чтобы связать."),
    ("web_empty", "The web is empty", "Паутина пуста"),
    ("web_empty_b", "Catch the first thought: whatever is in your head right now.", "Поймай первую мысль: что угодно, что сейчас в голове."),
    ("web_hint", "Press a thought and drag a thread to another one", "Зажми мысль и протяни нить к другой"),
    ("catch_btn", "Catch", "Поймать"),
    ("catch_a", "Catch a thought", "Поймать мысль"),
    ("b_ripe", "The cocoon «%1$s» is ripe. Open it", "Кокон «%1$s» созрел. Вскрыть"),
    ("b_lure", "Evening: try today’s catch on «%1$s»", "Вечер: примерь сегодняшний улов к «%1$s»"),
    # catch
    ("c_title", "What got caught?", "Что поймалось?"),
    ("c_hint", "A thought, a question, something you noticed…", "Мысль, вопрос, что-то замеченное…"),
    ("c_say_prompt", "Say the thought", "Скажи мысль"),
    ("catch_share", "Catch in Gossamer", "Поймать в Паутинку"),
    ("sc_catch", "Catch a thought", "Поймать мысль"),
    ("sc_say", "Say it", "Сказать голосом"),
    ("tile_label", "Catch a thought", "Поймать мысль"),
    ("widget_label", "Catch a thought", "Поймать мысль"),
    ("widget_desc", "Write or say a thought in two seconds", "Записать или наговорить мысль за две секунды"),
    ("k_idea", "Idea", "Идея"),
    ("k_question", "Question", "Вопрос"),
    ("k_notice", "Noticed", "Заметил"),
    ("k_words", "Words", "Слова"),
    ("k_idea_b", "A firefly: something to make, try or do.", "Светлячок: что-то сделать, попробовать, придумать."),
    ("k_question_b", "A burr with a hook: it clings until you answer it. A question can be hung as a lure.", "Репей с крючком: цепляется, пока не ответишь. Вопрос можно повесить приманкой."),
    ("k_notice_b", "Dandelion down: something seen, heard, felt.", "Пух одуванчика: что-то увиденное, услышанное, почувствованное."),
    ("k_words_b", "A wisp of someone else’s silk: a quote, a line, a phrase overheard.", "Чужая шёлковая ниточка: цитата, строчка, подслушанная фраза."),
    ("n_caught", "Stuck to the web", "Прилипло к паутине"),
    ("n_caught_night", "Caught in the night. It will glow.", "Поймано ночью. Будет светиться."),
    ("n_echo", "Caught before", "Уже ловил"),
    ("n_echo_b", "Almost the same as «%1$s». They hang side by side now, on a thicker thread.", "Почти то же, что «%1$s». Теперь они висят рядом, на толстой нити."),
    ("n_tied", "Thread pulled tight", "Нить натянута"),
    ("n_tied_b", "Both thoughts are safe from the wind now.", "Теперь ветер не тронет обе мысли."),
    ("n_tied_already", "They are tied already", "Они уже связаны"),
    ("n_dew", "The dew sparkled", "Роса блеснула"),
    ("n_wind_b", "They are in the Fluff, on the Thoughts tab. Nothing is lost.", "Они в Пухе, на вкладке «Мысли». Ничего не потерялось."),
    ("n_spun", "The cocoon «%1$s» is spun", "Кокон «%1$s» свит"),
    ("n_spun_b", "It ripens in three days. No peeking.", "Он созреет через три дня. Подглядывать нельзя."),
    # thought
    ("night_mark", "caught at night", "поймано ночью"),
    ("t_in_cocoon", "This thought is inside a cocoon.", "Эта мысль внутри кокона."),
    ("t_in_fluff", "The wind took this thought to the Fluff.", "Ветер унёс эту мысль в Пух."),
    ("t_is_lure", "Hung as a lure: in the evening, today’s catch is offered to it.", "Висит приманкой: вечером к ней примеряется сегодняшний улов."),
    ("t_no_threads", "No threads yet", "Нитей пока нет"),
    ("t_spin_need", "%1$d more thoughts in this cluster and it can be spun into a cocoon.", "Ещё мыслей: %1$d, и эту гроздь можно свернуть в кокон."),
    ("tie", "Tie to another thought", "Привязать к другой мысли"),
    ("tie_title", "Tie to…", "Привязать к…"),
    ("tie_none", "Nothing to tie to yet.", "Пока не к чему привязать."),
    ("lure_on", "Hang as a lure", "Повесить приманкой"),
    ("lure_off", "Take the lure down", "Снять приманку"),
    ("blow", "Let the wind have it", "Отдать ветру"),
    ("blow_q", "Let the wind have it?", "Отдать ветру?"),
    ("blow_b", "The thought goes to the Fluff, and its threads are cut. You can put it back from there.", "Мысль улетит в Пух, её нити оборвутся. Оттуда её можно вернуть."),
    ("delete_q", "Delete the thought?", "Удалить мысль?"),
    ("delete_b", "It goes for good, with its threads.", "Она исчезнет насовсем, вместе с нитями."),
    ("spin_title", "Spin a cocoon", "Свить кокон"),
    ("spin_b", "These thoughts leave the web and ripen together for three days. Then you open the cocoon and say in one sentence what it all comes to.",
     "Эти мысли уйдут с паутины и три дня будут зреть вместе. Потом ты вскроешь кокон и одной фразой скажешь, к чему всё это."),
    ("spin_hint", "What is it about? «Courtyard tours»", "О чём это? «Экскурсии по дворам»"),
    ("spin_go", "Spin it", "Свить"),
    ("spin_from", "Spin a cocoon from this cluster", "Свить кокон из этой грозди"),
    ("spin_pick", "Picked %1$d. A cocoon holds %2$d to %3$d thoughts.", "Выбрано: %1$d. В кокон помещается от %2$d до %3$d мыслей."),
    ("lure_title", "The lure", "Приманка"),
    ("lure_b", "Does anything caught today belong to this question? Tap to tie it.", "Что из сегодняшнего относится к этому вопросу? Нажми, чтобы привязать."),
    ("night_title", "The night catch", "Ночной улов"),
    ("night_b", "Caught after midnight. Read it in daylight.", "Поймано после полуночи. Перечитай при свете дня."),
    # list
    ("search_hint", "Search", "Поиск"),
    ("o_new", "Newest", "Новые"),
    ("o_tied", "Most tied", "Самые связанные"),
    ("o_old", "Long untouched", "Давно не трогал"),
    ("list_empty", "Nothing caught yet.", "Пока ничего не поймано."),
    ("list_none", "Nothing like that on the web.", "На паутине такого нет."),
    ("fluff", "The Fluff", "Пух"),
    ("fluff_b", "What the wind took: thoughts that hung untied and untouched for %1$d days. They’re not lost. Put one back if it still matters.",
     "Что унёс ветер: мысли, которые %1$d дн. висели без нитей и без внимания. Они не потерялись. Верни, если ещё важно."),
    ("fluff_empty", "The wind hasn’t taken anything yet.", "Ветер пока ничего не унёс."),
    ("fluff_empty_short", "Empty for now", "Пока пусто"),
    ("fluff_when", "caught %1$s, gone %2$s", "поймано: %1$s, унесено: %2$s"),
    ("restore", "Back on the web", "Вернуть на паутину"),
    ("let_go", "Let go for good", "Отпустить насовсем"),
    # collection
    ("cocoons", "Cocoons", "Коконы"),
    ("ripe_now", "Ripe: open it", "Созрел: можно вскрывать"),
    ("butterflies", "Butterflies", "Бабочки"),
    ("butterflies_empty", "No butterflies yet. Tie %1$d thoughts or more into one cluster, spin them into a cocoon, and three days later the first one flies out.",
     "Бабочек пока нет. Свяжи в одну гроздь не меньше %1$d мыслей, сверни её в кокон, и через три дня вылетит первая."),
    ("cocoon_wait", "Thoughts ripen in the dark. When it’s ripe, you’ll reread them all at once and see what they add up to.", "Мысли зреют в темноте. Когда кокон созреет, ты перечитаешь их разом и увидишь, что из них складывается."),
    ("hatch_b", "Read them all, slowly. What do they add up to? A decision, a plan, a first line, a «no»: one sentence.", "Прочитай всё, не спеша. К чему это всё? Решение, план, первая строчка, «нет» — одной фразой."),
    ("hatch_hint", "In one sentence…", "Одной фразой…"),
    ("hatch_examples", "«On Saturday I run the first tour for three friends.» «Not now, and that’s fine.»", "«В субботу провожу первую экскурсию для трёх друзей». «Не сейчас, и это нормально»."),
    ("hatch_go", "Let the butterfly out", "Выпустить бабочку"),
    ("hatch_open", "Open the cocoon", "Вскрыть кокон"),
    ("unspin", "Unspin the cocoon", "Распустить кокон"),
    ("unspin_q", "Unspin the cocoon?", "Распустить кокон?"),
    ("unspin_b", "The thoughts go back on the web with their threads. No butterfly.", "Мысли вернутся на паутину вместе с нитями. Бабочки не будет."),
    ("flew_out", "flew out %1$s · spun %2$s", "вылетела %1$s · свита %2$s"),
    ("year_title", "Indian summer %1$d", "Бабье лето %1$d"),
    ("year_card", "September: gossamer flies. Pick the year’s twelve thoughts.", "Сентябрь, летит паутинка. Выбери двенадцать мыслей года."),
    ("year_b", "In September gossamer flies across the fields. Pick up to %1$d thoughts from the past year: the ones that stayed with you, came true, or changed something.",
     "В сентябре над полями летит паутинка. Выбери до %1$d мыслей за прошедший год: те, что остались с тобой, сбылись или что-то изменили."),
    ("year_mine", "Picked %1$d of %2$d", "Выбрано %1$d из %2$d"),
    ("year_pool", "The year’s thoughts", "Мысли за год"),
    ("year_share", "Send off as text", "Отправить текстом"),
    ("year_share_head", "My year’s twelve thoughts, %1$d", "Двенадцать мыслей моего года, %1$d"),
    # settings
    ("set_wind", "Wind", "Ветер"),
    ("set_wind_b", "How long an untied thought nobody looked at hangs on the web before the wind takes it to the Fluff. Tying or rereading resets it.",
     "Сколько непривязанная мысль, к которой никто не возвращался, висит на паутине, пока ветер не унесёт её в Пух. Нить или новое прочтение сбрасывают срок."),
    ("days_n", "%1$d days", "%1$d дн."),
    ("wind_off", "No wind", "Без ветра"),
    ("set_quick", "Quick ways in", "Быстрый вход"),
    ("set_quick_b", "Long-press the app icon for «Catch» and «Say it». Select text in any app or tap «Share» to catch it. And:",
     "Долгое нажатие на иконку: «Поймать» и «Сказать голосом». Выдели текст в любом приложении или нажми «Поделиться», чтобы поймать его. А ещё:"),
    ("add_widget", "Put the widget on the home screen", "Добавить виджет на главный экран"),
    ("add_tile", "Add a tile to quick settings", "Добавить плитку в шторку"),
    ("set_sound", "The faint ting", "Тихий звон"),
    ("set_lang", "Language", "Язык"),
    ("lang_system", "System", "Системный"),
    ("lang_ru", "Русский", "Русский"),
    ("lang_en", "English", "English"),
    ("set_files", "Files", "Файлы"),
    ("set_files_b", "Everything lives only on this phone. Save a copy to move it or to read it anywhere.", "Всё хранится только на этом телефоне. Сохрани копию, чтобы перенести или перечитать где угодно."),
    ("export_md", "Save as text (Markdown)", "Сохранить текстом (Markdown)"),
    ("export_json", "Save a backup", "Сохранить резервную копию"),
    ("import_json", "Restore from a backup", "Восстановить из копии"),
    ("md_tied", "tied to", "связано с"),
    ("n_saved", "Saved", "Сохранено"),
    ("n_failed", "Couldn’t save the file", "Не получилось сохранить файл"),
    ("n_bad_backup", "This isn’t a Gossamer backup", "Это не резервная копия Паутинки"),
    ("community", "Community", "Сообщество"),
    ("community_text", "Gossamer is made by one person. Bugs, ideas and news go through the StillCraft bot in Telegram.",
     "Паутинку делает один человек. Ошибки, идеи и новости — через бота StillCraft в Telegram."),
    ("community_bug", "Report a bug", "Сообщить об ошибке"),
    ("community_bug_sub", "Something broke or looks wrong", "Что-то сломалось или выглядит не так"),
    ("community_idea", "Suggest an idea", "Предложить идею"),
    ("community_idea_sub", "What would make it better", "Что сделало бы приложение лучше"),
    ("community_follow", "Follow updates", "Следить за обновлениями"),
    ("community_follow_sub", "A message when a new version is out", "Сообщение, когда выйдет новая версия"),
    ("community_chat", "StillCraft chat", "Чат StillCraft"),
    ("community_chat_sub", "Talk to other people who use the apps", "Поговорить с теми, кто пользуется приложениями"),
    ("about", "About", "О приложении"),
    ("about_text", "Gossamer %1$s. Free, no ads, no accounts, no internet. Fonts: Cormorant, Lora, Caveat (SIL OFL). MIT licence.",
     "Паутинка %1$s. Бесплатно, без рекламы, без аккаунтов и без интернета. Шрифты: Cormorant, Lora, Caveat (SIL OFL). Лицензия MIT."),
    ("source", "Source code on GitHub", "Исходный код на GitHub"),
]

P = [
    ("on_web", ("%1$d thought on the web", "%1$d thoughts on the web"), ("%1$d мысль на паутине", "%1$d мысли на паутине", "%1$d мыслей на паутине")),
    ("threads", ("%1$d thread", "%1$d threads"), ("%1$d нить", "%1$d нити", "%1$d нитей")),
    ("echoes", ("came back %1$d time", "came back %1$d times"), ("возвращалась %1$d раз", "возвращалась %1$d раза", "возвращалась %1$d раз")),
    ("b_dew", ("Dew on %1$d thought this morning", "Dew on %1$d thoughts this morning"), ("Утром роса выпала на %1$d мысль", "Утром роса выпала на %1$d мысли", "Утром роса выпала на %1$d мыслей")),
    ("b_night", ("%1$d thought caught in the night", "%1$d thoughts caught in the night"), ("Ночью поймалась %1$d мысль", "Ночью поймались %1$d мысли", "Ночью поймалось %1$d мыслей")),
    ("n_wind", ("The wind took %1$d thought", "The wind took %1$d thoughts"), ("Ветер унёс %1$d мысль", "Ветер унёс %1$d мысли", "Ветер унёс %1$d мыслей")),
    ("t_wind", ("Untied: the wind takes it in %1$d day", "Untied: the wind takes it in %1$d days"), ("Без нитей: ветер унесёт через %1$d день", "Без нитей: ветер унесёт через %1$d дня", "Без нитей: ветер унесёт через %1$d дней")),
    ("t_wind_soon", ("The wind is tugging at it: %1$d day left. Tie it or let it go.", "The wind is tugging at it: %1$d days left. Tie it or let it go."),
     ("Ветер уже треплет её: остался %1$d день. Привяжи или отпусти.", "Ветер уже треплет её: осталось %1$d дня. Привяжи или отпусти.", "Ветер уже треплет её: осталось %1$d дней. Привяжи или отпусти.")),
    ("fluff_n", ("%1$d thought the wind took", "%1$d thoughts the wind took"), ("%1$d мысль унёс ветер", "%1$d мысли унёс ветер", "%1$d мыслей унёс ветер")),
    ("ripe_in", ("Ripens in %1$d day", "Ripens in %1$d days"), ("Созреет через %1$d день", "Созреет через %1$d дня", "Созреет через %1$d дней")),
    ("cocoon_inside", ("%1$d thought inside", "%1$d thoughts inside"), ("Внутри %1$d мысль", "Внутри %1$d мысли", "Внутри %1$d мыслей")),
    ("cocoon_was", ("Grew from %1$d thought", "Grew from %1$d thoughts"), ("Выросла из %1$d мысли", "Выросла из %1$d мыслей", "Выросла из %1$d мыслей")),
    ("year_picked", ("%1$d thought picked. Change or send off.", "%1$d thoughts picked. Change or send off."), ("Выбрана %1$d мысль. Изменить или отправить.", "Выбраны %1$d мысли. Изменить или отправить.", "Выбрано %1$d мыслей. Изменить или отправить.")),
    ("n_restored", ("%1$d thought restored", "%1$d thoughts restored"), ("Восстановлена %1$d мысль", "Восстановлены %1$d мысли", "Восстановлено %1$d мыслей")),
]

UNTRANSLATABLE = ("lang_ru", "lang_en")


def esc(s):
    s = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
    return s.replace("\\", "\\\\").replace("'", "\\'").replace('"', '\\"')


def write(lang, col):
    out = ['<?xml version="1.0" encoding="utf-8"?>', "<!-- Generated by tools/res.py: edit the table there. -->", "<resources>"]
    for name, en, ru in S:
        if name in UNTRANSLATABLE:
            if col == 1:
                continue
            out.append(f'    <string name="{name}" translatable="false">{esc(en)}</string>')
            continue
        out.append(f'    <string name="{name}">{esc(en if col == 0 else ru)}</string>')
    for name, en, ru in P:
        out.append(f'    <plurals name="{name}">')
        if col == 0:
            out.append(f'        <item quantity="one">{esc(en[0])}</item>')
            out.append(f'        <item quantity="other">{esc(en[1])}</item>')
        else:
            for q, v in zip(("one", "few", "many", "other"), (ru[0], ru[1], ru[2], ru[2])):
                out.append(f'        <item quantity="{q}">{esc(v)}</item>')
        out.append("    </plurals>")
    out.append("</resources>")
    d = RES / ("values" if lang == "en" else "values-ru")
    d.mkdir(parents=True, exist_ok=True)
    (d / "strings.xml").write_text("\n".join(out) + "\n", encoding="utf-8", newline="\n")


if __name__ == "__main__":
    names = [s[0] for s in S]
    assert len(names) == len(set(names)), [n for n in names if names.count(n) > 1]
    write("en", 0)
    write("ru", 1)
    print(len(S), "strings,", len(P), "plurals")
