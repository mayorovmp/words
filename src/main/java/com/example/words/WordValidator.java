package com.example.words;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WordValidator {

    @Value("${words.word-length-limit}")
    private int WORD_LENGTH_LIMIT;

    /**
     * Проверка, что длина строки больше параметра и соответствие паттерну.
     * @param string проверяемая строкаю.
     * @return соответствие условиям.
     */
    public boolean isValid(String string) {
        return (string.length() > WORD_LENGTH_LIMIT) && (string.matches("[a-zA-Zа-яА-Я]*"));
    }

}
