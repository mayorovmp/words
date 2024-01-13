package com.example.words;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Component
public class FileExtractor {

    /**
     * Извлечение всех файлов, расположенных по пути.
     * @param folderPath путь до папки с файлами.
     * @return Список файлов.
     * @throws IOException
     */
    public ConcurrentLinkedQueue<File> extract(String folderPath) throws IOException {
        ConcurrentLinkedQueue<File> files;
        try (var stream = Files.walk(Paths.get(folderPath))) {
            files = stream
                    .filter(Files::isRegularFile)
                    .map(Path::toFile)
                    .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
        }
        return files;
    }

}
