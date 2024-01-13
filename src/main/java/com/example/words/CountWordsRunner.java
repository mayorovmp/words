package com.example.words;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Component
public class CountWordsRunner implements CommandLineRunner {

    @Value("${words.folder-path}")
    private String FOLDER_PATH;

    @Value("${words.active-thread-limit}")
    private int ACTIVE_THREAD_LIMIT;

    @Value("${words.word-length-limit}")
    private int WORD_LENGTH_LIMIT;

    @Value("${words.popular-word-limit}")
    private int POPULAR_WORD_LIMIT;

    @Override
    public void run(String... args) throws Exception {
        var files = readFiles(FOLDER_PATH);
        var dict = countWordsInFiles(files);
        printPopularWords(dict, POPULAR_WORD_LIMIT);
    }

    /**
     * Вывод в консоль популярных слов.
     * @param dict словарь.
     * @param limit кол-во слов.
     */
    private void printPopularWords(Map<String, AtomicInteger> dict, int limit) {
        TreeSet<Pair<Integer, String>> set = new TreeSet<>();
        for (var e : dict.entrySet()) {
            set.add(Pair.of(e.getValue().get(), e.getKey()));
        }
        set.descendingSet().stream().limit(limit).forEach(System.out::println);
    }

    /**
     * Подсчет слов в файлах.
     * @param files список файлов.
     * @return Словарь с частотой слов.
     * @throws InterruptedException
     */
    private ConcurrentHashMap<String, AtomicInteger> countWordsInFiles(ConcurrentLinkedQueue<File> files) throws InterruptedException {
        ConcurrentHashMap<String, AtomicInteger> dict = new ConcurrentHashMap<>();
        List<Thread> threads = new ArrayList<>();
        AtomicInteger busyThreads = new AtomicInteger(0);
        while (!files.isEmpty()) {
            if (busyThreads.get() < ACTIVE_THREAD_LIMIT) {
                busyThreads.incrementAndGet();
                var thread = new Thread(() -> {
                    try {
                        var file = files.poll();
                        if (nonNull(file)) {
                            countWordsInFile(dict, file);
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    busyThreads.decrementAndGet();
                });
                thread.start();
                threads.add(thread);
            }
        }
        for (Thread thread : threads) {
            thread.join();
        }
        return dict;
    }

    /**
     * Чтение всех файлов из пути.
     * @param folderPath путь до папки с файлами.
     * @return Список файлов.
     * @throws IOException
     */
    private ConcurrentLinkedQueue<File> readFiles(String folderPath) throws IOException {
        ConcurrentLinkedQueue<File> files;
        try (var stream = Files.walk(Paths.get(folderPath))) {
            files = stream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
        }
        return files;
    }

    /**
     * Подсчет слов в файле.
     * @param dict мутируемый объект словаря.
     * @param file файл для чтения.
     * @throws IOException
     */
    private void countWordsInFile(ConcurrentHashMap<String, AtomicInteger> dict, File file) throws IOException {
        var fileReader = new FileReader(file);
        var br = new BufferedReader(fileReader);
        String line;
        while ((line = br.readLine()) != null) {
            var strings = line.split("[ ,.]");
            for (var string : strings) {
                if (isValidWord(string))
                    incWord(dict, string);
            }
        }
    }

    /**
     * Инкремент кол-ва слов в словаре.
     * @param dict мутируемый объект словаря.
     * @param word ключ словаря.
     */
    private void incWord(ConcurrentHashMap<String, AtomicInteger> dict, String word) {
        var previous = dict.putIfAbsent(word, new AtomicInteger(1));
        if (nonNull(previous)) {
            previous.incrementAndGet();
        }
    }

    /**
     * Проверка, что длина строки больше параметра и соответствие паттерну.
     * @param string проверяемая строкаю.
     * @return соответствие условиям.
     */
    private boolean isValidWord(String string) {
        return (string.length() > WORD_LENGTH_LIMIT) && (string.matches("[a-zA-Zа-яА-Я]*"));
    }
}
