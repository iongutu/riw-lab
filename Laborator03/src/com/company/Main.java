package com.company;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        DirectIndirectIndex index = new DirectIndirectIndex();
        File folder = new File("C:\\Users\\iongu\\Desktop\\Laboratoare RIW\\riw-lab\\Laborator02\\src-files\\test-files");
        String map_file = "C:\\Users\\iongu\\Desktop\\Laboratoare\\Laborator03\\src-files\\map_file.txt";
        String inverse_map  = "C:\\Users\\iongu\\Desktop\\Laboratoare\\Laborator03\\src-files\\inv_map_file.txt";
        index.directIndex(folder,map_file);
        index.indexIndirect(folder,map_file,inverse_map);
    }
}
