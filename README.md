# Приложение для подсчета и вывода популярных слов.
При запуске приложения запускается подсчет слов в указанной папке по всем файлам, которые в ней находятся.\
Не допустимо размещать файлы отличные от текстовых.\
Обработка файлов осуществляется в многопоточном режиме, кол-во потоков задается в секции параметров(см.ниже).

### Параметры приложения:

```yaml
words:
  # Лимит длины слова. В подсчете участвуют слова длина которых превышает заданную.
  word-length-limit: 2
  # Кол-во одновременных потоков для обработки файлов.
  active-thread-limit: 3
  # Путь до папки с файлами.
  folder-path: C:\Users\mayorov\Desktop\files
  # Кол-во популярных слов.
  popular-word-limit: 10
```
