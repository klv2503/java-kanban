package ru.taskmanagment;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.time.DayOfWeek.*;

public class TimeManager {

    /* Пока неясно
    а) как будет поставлена задача отображения в коде движения эпиков и подзадач во времени;
    б) может ли начинаться исполнение эпика пока не завершено выполнение какого-то другого;
    в) что планируется делать с рабочим временем (уверен, что это понятие будет введено в оборот);
    г) ... и с выходными;
    д) ... и с обеденным перерывом
    считаем, что имеем дело с планом, который стартует с даты standartStart, рабочее время описывается
    константами workDayStart и workDayEnd, пока реализовал метод, определяющий, является ли время
    рабочим. Скорее всего он пригодится. Обеденный перерыв пока не учитывал, если потребуется,
    проверки будут здесь же
     */
    public static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");
    public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    public static final LocalDateTime standartStart =
            LocalDateTime.of(2024, 9, 1, 0, 0, 0);
    static long timeShift = 0; //смещение времени относительно старта пока определяется в минутах
    public static final LocalTime workDayStart = LocalTime.of(8, 0);
    public static final LocalTime workDayEnd = LocalTime.of(17, 0);
    public final Duration workTimeDuration = Duration.between(workDayStart, workDayEnd);
    public static final Set<DayOfWeek> workDays = new HashSet<>(Arrays.asList(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY));

    public TimeManager() {

    }

    public static boolean isWorkTime(LocalDateTime anyTime) {
        return workDays.contains(anyTime.getDayOfWeek())
                && !LocalTime.of(anyTime.getHour(), anyTime.getMinute()).isBefore(workDayStart)
                && !LocalTime.of(anyTime.getHour(), anyTime.getMinute()).isAfter(workDayEnd);
    }

    public static String duration2String(Duration duration) {
        int localMinutes = (int) duration.toMinutes();
        return LocalTime.of(localMinutes / 60, localMinutes % 60).format(timeFormatter);
    }

    public static Long findStartShift(long duration) {
        long shiftToReturn = timeShift;
        LocalDateTime thisDay = standartStart.plus(Duration.ofMinutes(timeShift));
        //если время рабочее и срок выполнения подзадачи не выходит за рамки рабочего времени,
        //то стартуем с текущего сдвига времени
        if (isWorkTime(thisDay)
                && isWorkTime(thisDay.plus(Duration.ofMinutes(duration)))) {
            timeShift += duration;
        } else {
            long addToShift = workDayStart.getHour() * 60 + workDayStart.getMinute();
            //переходим в 0:00 следующего дня
            LocalDateTime nextDay = thisDay.plusDays(1).minusHours(thisDay.getHour()).minusMinutes(thisDay.getMinute());
            addToShift += Duration.between(thisDay, nextDay).toMinutes();
            if (nextDay.getDayOfWeek().equals(SATURDAY))
                addToShift += 2 * 24 * 60;
            else if (nextDay.getDayOfWeek().equals(SUNDAY)) {
                addToShift += 24 * 60;
            }
            timeShift += addToShift;
            shiftToReturn = timeShift;
            timeShift += duration;
        }
        return shiftToReturn;
    }

}

