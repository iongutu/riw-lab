package com.company;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class FolderProcessing {

    private LinkedList<File> folders;
    private LinkedList<File> docs;
    private static int i = 0;

    public FolderProcessing() {
        folders = new LinkedList<>();
        docs = new LinkedList<>();
    }

    public void directIndex(File folder) throws IOException {
        String folderPath = folder.getPath();
        try {
            File[] listFiles = folder.listFiles();
            File mapedFile = new File(folderPath + "/direct" + i + "_index.txt");
            i++;
            PrintWriter directWriter = new PrintWriter(mapedFile);
            HashMap<String, HashMap<String, Integer>> cuvinte_fisier = new HashMap<>();
            for (File file : listFiles) {
                if (file.isFile()) {
                    if (!file.getName().contains("_index"))//daca nu e fisier de mapare
                    {
                        HashMap<String, Integer> cuvinte = GetWords.algoritm(file.toString());
                        cuvinte_fisier.put(file.getName(), cuvinte);
                    }
                } else {
                    if (file.isDirectory()) {
                        directIndex(file);
                    }
                }
            }
            for (Map.Entry<String, HashMap<String, Integer>> entry : cuvinte_fisier.entrySet()) {
                directWriter.println(entry.getKey() + "\n" + entry.getValue());
            }
            directWriter.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
