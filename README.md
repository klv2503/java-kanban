# java-kanban
Repository for homework project.

По заданию к спринту8

1. Добавление фактора времени

В принятой модели Task служит просто для описания (систематически) выполняемой работы. Собственно работа

отражается в подзадачах и эпиках, поэтому поля времени и продолжительности добавлены только у них.

При этом остался ряд вопросов, не ракрытыхв ТЗ:

- учитывать ли праздники, выходные, обеденный перерыв;

- как поступать со временем, высвобождающимся при удалении подзадачи или эпика (есть разные варианты);

- что делать, если срок выполнения эпика (подзадачи) прошел, а он (она) не выполнена. Чтобы пока не иметь

этой проблемы сроки начала всех работ унёс в сентябрь. Впрочем, сделал так, чтобы дату/время старта

было легко поменять;

- допустимо ли выполнять подзадачу одного эпика, затем подзадачу другого, а потом выполнять вторую 

подзадачу первого эпика. В первом случае что считать продолжительностью эпика, во втором можно быстро 

работать без TreeSet, достаточно эпики сортировать в момент создания.

Для упрощения работы со временем завел класс TimeManager. Кроме него появились классы 

- TaskManagerTest - согласно ТЗ спринта

- CSVFormatter - идея взята у наставника, реализовывал сам. Это переделка части задания к ТЗ Спринта7, 

что-то пришлось переделать под стандарт, для чего-то нашлось более удачное решение.

- HistoryManagerTest - просто вынес из InMemoryTaskManagerTest то, что относится к истории. Оказалось, что 

требуемые для HistoryManager тесты сделал уже давно.

Тест на валидность времени генерируемой SubTask добавил в InMemoryTaskManagerTest, т.к. метод реализован

в InMemoryTaskManager из-за перебора SubTask (TimeManager про них ничего не знает). Там же добавлен тест, 

рассматривающий все ситуации статуса эпика

Тест исключения разместил в FileBackedTaskManagerTest, т.к. использованные в проекте исключения  возникают

в FileBackedTaskManager.

Остальные изменения в коде вызваны необходимостью учета времени в ранее написанных классах/методах. А также

переписыванием циклов forEach в Stream.