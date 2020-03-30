package com.company;

import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
        FolderProcessing index = new FolderProcessing();
        File folder = new File("C:\\Users\\iongu\\Desktop\\Laboratoare RIW\\riw-lab\\Laborator02\\src-files\\test-files");
        index.directIndex(folder);
    }
}
