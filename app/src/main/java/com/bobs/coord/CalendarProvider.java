package com.bobs.coord;

import org.springframework.stereotype.Component;

import java.util.Calendar;

/**
 * Created by dokeeffe on 2/6/17.
 */
@Component
public class CalendarProvider {

    public Calendar currentCalendar() {
        return Calendar.getInstance();
    }
}
