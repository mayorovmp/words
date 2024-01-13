package com.example.words;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Objects.nonNull;

@Component
@RequiredArgsConstructor
public class WordCounter {

    @Value("${words.active-thread-limit}")
    private int ACTIVE_THREAD_LIMIT;

    private final WordValidator wordValidator;

    /**
     * Подсчет слов в файлах выполняется в многопоточном режиме, каждый файл обрабатывается в отдельном потоке.
     * @param files список файлов.
     * @return Словарь с частотой слов.
     * @throws InterruptedException
     */
    public ConcurrentHashMap<String, AtomicInteger> count(ConcurrentLinkedQueue<File> files) throws InterruptedException {
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
                if (wordValidator.isValid(string))
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
}
