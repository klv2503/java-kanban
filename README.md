# java-kanban
Repository for homework project.


Для задач и эпиков организовал таблицы.

Подзадачи в моём понимании имеют смысл только в привязке к эпику, поэтому в их таблице ключ

это название эпика. При этом оставил возможность наличия нескольких экземпляров одной задачи

в качестве подзадач. Во многих процессах одна и та же подзадача может выполняться неоднократно.

Пример: приготовление ужина при наличии одной сковороды. Жарим мясо, моем сковороду, жарим

кабачки, снова моем сковороду, жарим грибы и опять...

Чтобы экземпляры задачи в эпике между собой различать, добавил в подзадачах поле codeInEpic.

Не уверен, что в этой таблице ключом должно быть название эпика, возможно, лучше его код (проще

работать).

Сделал генерацию задач и нескольких эпиков сразу с подзадачами, чтобы нагляднее проходило тестирование.

Это действительно позволило выявить несколько неочевидных моментов. Сами тесты разделил комментариями,

опять же для наглядности. После вывода итога каждого теста прога ждёт нажатия Enter, что позволяет

сразу заглянуть в прогу, проверить, соответствует ли результат ожиданиям и т.п.

TaskManager оказался перегруженным из-за изобилия тестов. Кроме того, я не знаю (пока), в каком порядке

принято методы располагать, они оказались несколько хаотично разбросанными. Тут нужен совет.

Также пока не знаю, как принято поступать с фигурными скобками в случаях, когда в них ровно один оператор:

принято ли в любом случае их ставить, или строка типа if (epicList.containsKey(code)) epic = epicList.get(code);

признаётся в рамках правил хорошего тона.