package com.example.bless;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/** Данная программа рассчитана на то, что доступное количество оперативной памяти ≈ 600мб.
 * Отличается от первой тем, что она обрабатывает вариант, где в строке может быть только один элемент,
 * однако работает медленнее:
 * 1 строка: subEl\n
 * 2 строка: subEl1,subEl2,subEl3,...,subEln.*/
public class CsvReaderSort {

    private static final long filesMem = 576716800;

    public static void main(String[] args) {
        Path getFilePath = Paths.get(""/**TODO На этом месте нужно вписать полный путь до файла, который надо отсортировать*/);
        File stFile = new File(getFilePath.toUri());
        List<File> sortedFiles = sortAndSaveSubFiles(stFile);
        mergeSortedFiles(sortedFiles, getFilePath);
    }

    /** Данный метод разделяет основной файл на отсортированные файлы поменьше(550мб).
     * Для сортировки разделяет строку на массив строк, разделяемый ",". */
    private static List<File> sortAndSaveSubFiles(File stFile) {
        List<File> resultFiles = new ArrayList<>();
        int countOfFiles = (int) Math.ceil((double) stFile.length() / filesMem);
        try (BufferedReader reader = Files.newBufferedReader(stFile.toPath())) {
            List<String> subList;
            for (int i = 0; i < countOfFiles; i++) {
                Path finPath = Paths.get("files/file" + i + ".txt");
                subList = new ArrayList<>();
                int listByteSize = 0;
                String str;
                while (listByteSize < filesMem && (str = reader.readLine()) != null) {
                    subList.add(str);
                    listByteSize += str.getBytes().length;
                }
                if (!(subList.isEmpty())) {
                    subList.sort(Comparator.comparingInt(val -> Integer.parseInt(val.split(",")[0])));
                    try (BufferedWriter writer = Files.newBufferedWriter(finPath)) {
                        subList.forEach(v -> {
                            try {
                                writer.write(v);
                                writer.newLine();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                    resultFiles.add(new File(String.valueOf(finPath)));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resultFiles;
    }

    /** Данный метод выбирает из каждого побочного файла элемент и записывает наименьший из них в финальный файл. */
    private static void mergeSortedFiles(List<File> sortedChunks, Path outputFilePath) {
        String str;
        BufferedReader[] readers = new BufferedReader[sortedChunks.size()];
        Map<Integer, String> mergeMap = new HashMap<>();
        if (sortedChunks.size() == 1) {
            sortedChunks.get(0).renameTo(outputFilePath.toFile());
        } else {
            try (BufferedWriter writer = Files.newBufferedWriter(outputFilePath)) {
                for (int i = 0; i < sortedChunks.size(); i++) {
                    readers[i] = Files.newBufferedReader(sortedChunks.get(i).toPath());
                    mergeMap.put(i, readers[i].readLine());
                }
                while (!(mergeMap.size() == 1)) {
                    Map.Entry<Integer, String> val = findMin(mergeMap);
                    writer.write(val.getValue());
                    writer.newLine();
                    if ((str = readers[val.getKey()].readLine()) != null) {
                        mergeMap.replace(val.getKey(), str);
                    } else {
                        mergeMap.remove(val.getKey());
                        readers[val.getKey()].close();
                        Files.delete(sortedChunks.get(val.getKey()).toPath());
                    }
                }
                int key = mergeMap.keySet().iterator().next();
                while ((str = readers[key].readLine()) != null) {
                    writer.write(str);
                    writer.newLine();
                }
                readers[key].close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /** Метод для нахождения минимального значения. */
    public static Map.Entry<Integer, String> findMin(Map<Integer, String> map) {
        return map.entrySet().stream()
                .min(Comparator.comparingInt(val -> Integer.parseInt(val.getValue().split(",")[0])))
                .get();
    }
}