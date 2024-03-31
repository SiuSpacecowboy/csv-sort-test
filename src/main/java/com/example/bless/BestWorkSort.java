package com.example.bless;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/** Данная программа рассчитана на то, что доступное количество оперативной памяти ≈ 600мб.
 * Также предполагается, что в основном файле строка имеет вид: subEl,subEl2,subEl3,...,subEln. */
public class BestWorkSort {

    private static final long fileMem = 576716800;

    public static void main(String[] args) {
        Path getFilePath = Paths.get(/**TODO На этом месте нужно вписать полный путь до файла, который надо отсортировать*/);
        File fileForSort = new File(getFilePath.toUri());
        try {
            List<File> sortedFiles = sortAndSaveSubFiles(fileForSort);
            mergeSortedFiles(sortedFiles, getFilePath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Сортировка завершена успешно.");
    }

    /** Данный метод разделяет основной файл на отсортированные файлы поменьше(550мб).
     * Сортирует по первому элементу, выбирая его посредством подстроки от 0 элемента до ",".*/
    private static List<File> sortAndSaveSubFiles(File stFile) throws IOException {
        List<File> resultFiles = new ArrayList<>();
        int countOfFiles = (int) Math.ceil((double) stFile.length() / fileMem);
        try (BufferedReader reader = Files.newBufferedReader(stFile.toPath())) {
            List<String> subList;
            for (int i = 0; i < countOfFiles; i++) {
                Path subPath = Paths.get("files/file" + i + ".txt");
                subList = new ArrayList<>();
                int listByteSize = 0;
                String str;
                while (listByteSize < fileMem && (str = reader.readLine()) != null) {
                    subList.add(str);
                    listByteSize += str.getBytes().length;
                }
                if (!(subList.isEmpty())) {
                    subList.sort(Comparator.comparingInt(val -> Integer.parseInt(val.substring(0, val.indexOf(",")))));
                    try (BufferedWriter writer = Files.newBufferedWriter(subPath)) {
                        subList.forEach(v -> {
                            try {
                                writer.write(v);
                                writer.newLine();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                    resultFiles.add(new File(String.valueOf(subPath)));
                }
            }
        }
        return resultFiles;
    }

    /** Данный метод выбирает из каждого побочного файла элемент и записывает наименьший из них в финальный файл. */
    private static void mergeSortedFiles(List<File> sortedFiles, Path outputFilePath) throws IOException {
        String str;
        BufferedReader[] readers = new BufferedReader[sortedFiles.size()];
        Map<Integer, String> mergeMap = new HashMap<>();
            try (BufferedWriter writer = Files.newBufferedWriter(outputFilePath)) {
                for (int i = 0; i < sortedFiles.size(); i++) {
                    readers[i] = Files.newBufferedReader(sortedFiles.get(i).toPath());
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
                        Files.delete(sortedFiles.get(val.getKey()).toPath());
                    }
                }
                int key = mergeMap.keySet().iterator().next();
                writer.write(mergeMap.get(key));
                writer.newLine();
                while ((str = readers[key].readLine()) != null) {
                    writer.write(str);
                    writer.newLine();
                }
                readers[key].close();
                Files.delete(sortedFiles.get(key).toPath());
            }
    }

    /** Метод для нахождения минимального значения. */
    public static Map.Entry<Integer, String> findMin(Map<Integer, String> map) {
        return map.entrySet().stream()
                .min(Comparator.comparingInt(val -> Integer.parseInt(val.getValue()
                        .substring(0, val.getValue().indexOf(",")))))
                .get();
    }
}

