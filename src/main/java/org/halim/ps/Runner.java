package org.halim.ps;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.CalendarOutputter;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Date;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Period;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.parameter.Cn;
import net.fortuna.ical4j.model.parameter.Role;
import net.fortuna.ical4j.model.property.*;
import net.fortuna.ical4j.util.MapTimeZoneCache;
import net.fortuna.ical4j.util.RandomUidGenerator;
import net.fortuna.ical4j.util.UidGenerator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.ParseException;
import java.util.*;

public class Runner {

    public static void main(String[] args) throws Exception {

        Calendar calendar = new GregorianCalendar();

        calendar.setTime(new Date());
        System.out.println(calendar.getTime());

        calendar.add(Calendar.DATE, 5);
        System.out.println(calendar.getTime());

        print(calendar);
        /*calendar.clear(Calendar.HOUR_OF_DAY); // so doesn't override
        calendar.set(Calendar.HOUR, 3);
        //print(calendar);*/

        createDailyEvent();
        generateEvents();
    }

    private static void createDailyEvent() throws Exception {

        Calendar calendar = new GregorianCalendar();
        calendar.setFirstDayOfWeek(GregorianCalendar.SUNDAY);

        calendar.setTime(new java.util.Date());
        UidGenerator ug = new RandomUidGenerator();

        net.fortuna.ical4j.model.Calendar calendarEvents = new net.fortuna.ical4j.model.Calendar();
        calendarEvents.getProperties().add(new ProdId("calendar"));
        calendarEvents.getProperties().add(Version.VERSION_2_0);
        calendarEvents.getProperties().add(CalScale.GREGORIAN);

        List<String> required = Collections.singletonList("mailto:abdelhalim.2031@gmail.com");
        List<String> optional = Collections.singletonList("mailto:halimdeng@gmail.com");

        calendarEvents.getComponents().add(buildEvent(ug.generateUid(), new Date(calendar.getTime()), "Daily",
                "FREQ=DAILY;COUNT=9", required, optional));
        calendarEvents.getComponents().add(buildEvent(ug.generateUid(), new Date(calendar.getTime()), "Monthly",
                "FREQ=MONTHLY", required, optional));
        calendarEvents.getComponents().add(buildEvent(ug.generateUid(), new Date(calendar.getTime()), "Sunday Recur",
                "FREQ=MONTHLY;INTERVAL=1;BYDAY=SU;BYSETPOS=1;COUNT=3", required, optional));
        calendarEvents.getComponents().add(buildEvent(ug.generateUid(), new Date(calendar.getTime()), "Yearly",
                "FREQ=YEARLY", required, optional));

        System.out.println(calendarEvents);

        FileOutputStream fileOutputStream = new FileOutputStream("calendar.ics");
        CalendarOutputter calendarOutputter = new CalendarOutputter();
        calendarOutputter.output(calendarEvents, fileOutputStream);
    }

    private static VEvent buildEvent(Uid eventUid, Date startDate, String summary, String rulePattern,
                                     List<String> requiredAttendees, List<String> optionalAttendees) throws ParseException {
        VEvent event = new VEvent(startDate, summary);
        event.getProperties().add(eventUid);
        event.getProperties().add(new ProdId(eventUid.getValue()));
        event.getProperties().add(new RRule(rulePattern));


        requiredAttendees.forEach(attendee -> {
            Attendee eventAttendee = new Attendee(URI.create(attendee));
            eventAttendee.getParameters().add(Role.REQ_PARTICIPANT);
            eventAttendee.getParameters().add(new Cn(String.valueOf(UUID.randomUUID())));
            event.getProperties().add(eventAttendee);
        });

        optionalAttendees.forEach(attendee -> {
            Attendee eventAttendee = new Attendee(URI.create(attendee));
            eventAttendee.getParameters().add(Role.OPT_PARTICIPANT);
            eventAttendee.getParameters().add(new Cn(String.valueOf(UUID.randomUUID())));
            event.getProperties().add(eventAttendee);
        });

        return event;
    }

    private static void generateEvents() throws ParseException {
        // Reading the file and build the calendar object
        System.setProperty("net.fortuna.ical4j.timezone.cache.impl", MapTimeZoneCache.class.getName());
        CalendarBuilder builder = new CalendarBuilder();
        net.fortuna.ical4j.model.Calendar cal = null;
        try {
            cal = builder.build(new FileInputStream("calendar.ics"));
        } catch (IOException | ParserException e) {
            e.printStackTrace();
        }

        // Create the date range which is desired to look into it.
        DateTime from = new DateTime("20210628T070000Z");
        DateTime to = new DateTime("20300628T070000Z");
        Period period = new Period(from, to);

        // For each VEVENT in the ICS, print the recurrence period date and time
        Objects.requireNonNull(cal).getComponents("VEVENT")
                .parallelStream()
                .forEach(event -> event.calculateRecurrenceSet(period).forEach(System.out::println));
    }

    private static void print(Calendar calendar) {
        System.out.println("ERA: " + calendar.get(Calendar.ERA));
        System.out.println("YEAR: " + calendar.get(Calendar.YEAR));
        System.out.println("MONTH: " + calendar.get(Calendar.MONTH));
        System.out.println("WEEK_OF_YEAR: " + calendar.get(Calendar.WEEK_OF_YEAR));
        System.out.println("WEEK_OF_MONTH: " + calendar.get(Calendar.WEEK_OF_MONTH));
        System.out.println("DATE: " + calendar.get(Calendar.DATE));
        System.out.println("DAY_OF_MONTH: " + calendar.get(Calendar.DAY_OF_MONTH));
        System.out.println("DAY_OF_YEAR: " + calendar.get(Calendar.DAY_OF_YEAR));
        System.out.println("DAY_OF_WEEK: " + calendar.get(Calendar.DAY_OF_WEEK));
        System.out.println("DAY_OF_WEEK_IN_MONTH: " + calendar.get(Calendar.DAY_OF_WEEK_IN_MONTH));
        System.out.println("AM_PM: " + calendar.get(Calendar.AM_PM));
        System.out.println("HOUR: " + calendar.get(Calendar.HOUR));
        System.out.println("HOUR_OF_DAY: " + calendar.get(Calendar.HOUR_OF_DAY));
        System.out.println("MINUTE: " + calendar.get(Calendar.MINUTE));
        System.out.println("SECOND: " + calendar.get(Calendar.SECOND));
        System.out.println("MILLISECOND: " + calendar.get(Calendar.MILLISECOND));
        System.out.println("ZONE_OFFSET: " + (calendar.get(Calendar.ZONE_OFFSET) / (60 * 60 * 1000))); // in hours
        System.out.println("DST_OFFSET: " + (calendar.get(Calendar.DST_OFFSET) / (60 * 60 * 1000))); // in hours
    }


}
