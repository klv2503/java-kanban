# java-kanban
Repository for homework project.


Независимо от результатов проверки буду что-то проверять и, возможно, доделывать.

Замечания учёл. Метод по проверке списков на пустоту убрал (теперь его просто нет в 

коде), воспользовавшись советом. Почему-то самому в голову не пришла идея класть 

пустой список вместо clear (что превращало список в null и запрещало использовать 

ArrayList.size() ). Однострочники переделал по всему коду. InMemoryHistoryManager

объявил через Managers.

Первый вариант добавления эпика в качестве подзадачи (другого) эпика сразу выглядел 

довольно глупо (что называется - самому противно). Замечания по этому поводу ожидались 

(и они были - имплементация TaskManager в Epic и к методу addEpicToEpic). Иммплементацию 

убрал, но без нее заставить метод работать не смог (так было и до первой попытки сдачи, 

отчего появилась эта самая имплементация). Пока всё, что к этому методу относится, закомментил

и временно эпик в качестве подзадачи добавить нельзя.

Тем временем попробовал реализовать другую идею. Для неё сделал дополнительный класс EpicAdmin.

Его до конца не сделал, но он способен записывать эпики как подзадачи, а также отлавливать

ситуации когда эпик становится собственной подзадачей. Ситуация отлавливается даже 

когда пытаемся Эпик1 добавить в Эпик2, который служит подзадачей у Эпика3, который

является подзадачей Эпика1. Было хорошим поводом попробовать рекурсию. Несколько проверок

EpicAdmin добавил в тесты (хороший повод потренироваться писать тесты). В том числе проверку

Эпика на самоподзадачу, т.е. если смогу справиться с данными, то проверить уж точно смогу.

С этим классом тоже есть вопросы, например, использовать ли для него TaskManager или 

сделать собственный интерфейс.

Для задач, подзадач и эпиков организовал таблицы.

Все коды (для задач, подзадач, эпиков) генерируются программой. После генерации они 

нигде в коде не меняются, разве что при удалении объекта. На основе таблиц эпиков 

и подзадач делается таблица allEpics, которая и предполагается как собственно комплекс

текущих работ для менеджера.  Пока предполагается, что каждый эпик присутствует в 

allEpics не более, чем в одном экземпляре. Оставил возможность наличия нескольких 

экземпляров одной задачи в качестве подзадач. Считается, что порядок выполнения 

подзадач в эпике задаётся их индексами в списке эпиковых подзадач. Также считается, что

несколько экземпляров одной подзадачи в пределах одного эпика это разные подзадачи, 

у которых реквизиты совпадают, но статусы вообще говоря различны - экземпляры равны с точностью 

до статусов. Сложившаяся структура данных пока не нравится, тоже думаю, как улучшить.

