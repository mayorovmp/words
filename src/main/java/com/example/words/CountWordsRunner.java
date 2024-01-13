package com.example.words;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CountWordsRunner implements CommandLineRunner {

    @Value("${words.folder-path}")
    private String FOLDER_PATH;

    @Value("${words.popular-word-limit}")
    private int POPULAR_WORD_LIMIT;

    private final WordPrinter wordPrinter;

    private final WordCounter wordCounter;

    private final FileExtractor fileExtractor;

    @Override
    public void run(String... args) throws Exception {
        var files = fileExtractor.extract(FOLDER_PATH);
        var dict = wordCounter.count(files);
        wordPrinter.printPopularWord(dict, POPULAR_WORD_LIMIT);
    }

}
