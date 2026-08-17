package data;

import service.LocalizationService;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Storage<T> {
    protected final ArrayList<T> list;

    public Storage() {
        list = new ArrayList<>(100);
    }

    public void add(T item){
        list.add(item);
    }

    @Override
    public String toString() {
        return list.isEmpty()
                ? LocalizationService.getMessage("storage_empty")
                : LocalizationService.getMessage("storage_list") + "\n" +
                IntStream.range(0, list.size())
                // Map each int in int stream to item in list
                .mapToObj(i -> (i + 1) + ". " + list.get(i).toString() + "\n")
                // Collect all strings in stream to 1 string
                .collect(Collectors.joining()).strip();
    }

}