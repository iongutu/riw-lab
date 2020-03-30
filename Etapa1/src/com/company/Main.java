package com.company;

import org.apache.commons.collections4.MultiMap;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Main {

    public static void main(String[] args) throws IOException {
        DirectIndirectIndex index = new DirectIndirectIndex();
        File folder = new File("C:\\Users\\iongu\\Desktop\\Laboratoare\\Etapa1\\src-files\\test-files");
        String map_file = "C:\\Users\\iongu\\Desktop\\Laboratoare\\Etapa1\\src-files\\map_file.txt";
        String inverse_map  = "C:\\Users\\iongu\\Desktop\\Laboratoare\\Etapa1\\src-files\\inv_map_file.txt";
        index.directIndex(folder,map_file);
        MultiMap<String, HashMap<String,Integer>> idx_ind = index.indexIndirect(folder,map_file,inverse_map);
        String comanda = "coals and flip not Russia";
        BooleanSearch.search(idx_ind,comanda);

    }
}
