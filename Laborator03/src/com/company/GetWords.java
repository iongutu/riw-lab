package com.company;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class GetWords {
    private static ArrayList<String> exceptionWords;
    private static ArrayList<String> stopWords;
	
	private static void addException()  {
		
        ArrayList<String> stopList = new ArrayList<>();
        ArrayList<String> exceptionList = new ArrayList<String>();
        try {
            String stop = "src/stopWords.txt";
            String exception = "src/exception.txt";
            Scanner s = new Scanner(new File(stop));
            Scanner s1 = new Scanner(new File(exception));

            while (s.hasNext()) {
                stopList.add(s.next());
            }
            s.close();
            while (s1.hasNext()) {
                exceptionList.add(s1.next());
            }
            s1.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        GetWords.exceptionWords  = exceptionList;
        GetWords.stopWords =  stopList;

    }
    public static HashMap<String, Integer> algoritm(String fileIn) throws IOException {
        addException();
        var builder = new StringBuilder();
        HashMap<String,Integer> cuvinte = new HashMap<>();
        int c;
        InputStream fileInputStream = new FileInputStream(fileIn);
        Reader streamReader = new InputStreamReader(fileInputStream, Charset.defaultCharset());
        while ((c = streamReader.read()) != -1) // if we met EOF
        {
            char character = (char) c;
            if (Character.isDigit(character) || Character.isLetter(character)) {
                builder.append(character);
            } else {
                if (!builder.toString().equals("")) {
                    if(notStopWord(builder.toString().toLowerCase())) {
                        if (cuvinte.get(builder.toString().toLowerCase()) != null)//if in the hash map the word already exists
                        {
                            cuvinte.put(builder.toString().toLowerCase(), cuvinte.get(builder.toString().toLowerCase()) + 1);//update the value (nr de cuvinte)
                        } else {
                            cuvinte.put(builder.toString().toLowerCase(), 1);  //a word wos find for the
                        }
                    }
                }
                builder.setLength(0);
            }

        }
        return cuvinte;
    }

    public static boolean notStopWord(String currentWord) {
        if (exceptionWords.contains(currentWord)) {
            if (stopWords.contains(currentWord))
                return false;
            return true;
        }
        return true;
    }


}
