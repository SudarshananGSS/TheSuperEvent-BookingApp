package model;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper model grouping multiple events with the same title.
 */
public class EventGroup {
    private String title;
    private Map<String, List<String>> venueDays;

    public EventGroup(String title) {
        this.title = title;
        this.venueDays = new LinkedHashMap<>();
    }

    public void addOption(String venue, String day) {
        venueDays.computeIfAbsent(venue, v -> new ArrayList<>());
        List<String> days = venueDays.get(venue);
        if (!days.contains(day)) {
            days.add(day);
        }
    }

    public String getTitle() {
        return title;
    }

    public String getVenuesDisplay() {
        return String.join(", ", venueDays.keySet());
    }

    public String getOptionsDisplay() {
        return venueDays.entrySet().stream()
                .map(e -> e.getKey() + " - " + String.join("/", e.getValue()))
                .collect(Collectors.joining("; "));
    }

    public List<String> getOptionList() {
        List<String> list = new ArrayList<>();
        for (Map.Entry<String, List<String>> e : venueDays.entrySet()) {
            for (String d : e.getValue()) {
                list.add(e.getKey() + " - " + d);
            }
        }
        return list;
    }


}
