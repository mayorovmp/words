package com.example.words;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class WordPrinter {

    /**
     * Вывод в консоль популярных слов.
     * @param dict словарь.
     * @param limit кол-во слов.
     */
    public void printPopularWord(Map<String, AtomicInteger> dict, int limit) {
        TreeSet<Pair<Integer, String>> set = new TreeSet<>();
        for (var e : dict.entrySet()) {
            set.add(Pair.of(e.getValue().get(), e.getKey()));
        }
        set.descendingSet().stream().limit(limit).forEach(System.out::println);
    }

}
