package com.company;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jsoup.Jsoup;
public class Main {

    public static void main(String[] args) throws  IOException{
        String fileName = "src/index.html";
        File myFile2  = new File(fileName);
        String store = "";
        if (myFile2.exists())
        {
            System.out.println("Fisierul Exista!");
            Document doc = Jsoup.parse(myFile2, "utf-8");
            Elements bodyText = doc.select("body");
            bodyText.stream()
                    .map(Element::text)
                    .forEach(System.out::println);


            Elements links = doc.select("a");
            for (Element link : links) {
                System.out.println(link.absUrl("href"));
            }


            Elements titles = doc.select("title");
            titles.stream()
                    .map(Element::text)
                    .forEach(System.out::println);


            Elements metas = doc.select("meta");
            metas.stream()
                    .filter(meta -> meta.attr("name").equals("keywords"))
                    .map(meta -> meta.attr("content"))
                    .forEach(System.out::println);


            Elements metass = doc.select("meta");
            metass.stream()
                    .filter(meta -> meta.attr("name").equals("description"))
                    .map(meta -> meta.attr("content"))
                    .forEach(System.out::println);


            Elements metarobots = doc.select("meta");
            metarobots.stream()
                    .filter(meta -> meta.attr("name").equals("robots"))
                    .map(meta -> meta.attr("content"))
                    .forEach(System.out::println);



        }

        System.out.println("Exercitiul 2!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        Hashtable<String,Integer> hash_table = new Hashtable<String,Integer>();
        String filemane = "C:\\Users\\iongu\\Desktop\\Project\\src\\1.txt";
        File myFile = new File(filemane);
        var builder = new StringBuilder();
        //String word = "";
        if(myFile.exists())
        {
            System.out.println("AIci");
            FileReader reader = new FileReader(myFile);
            BufferedReader br = new BufferedReader(reader);
            int c= 0;
            int i =0;
            while((c= br.read())!= -1) //while is not the end of a file
            {
                char character = (char)c;
                if(Character.isLetter(character) || Character.isDigit(character))
                {
                   // word += character;
                    builder.append(character);
                }
                else
                {
                   // word = word.toLowerCase();

                    if(!builder.toString().equals(""))
                    {
                        if(hash_table.get(builder.toString())!= null)//if in the hash table the word already exists
                        {
                            hash_table.put(builder.toString(), hash_table.get(builder.toString()) + 1);//update the value (nr de cuvinte)
                        }
                        else
                        {
                            hash_table.put(builder.toString(),1);  //a word wos find for the
                        }
                    }
                    builder.setLength(0);
                }
                i++;

            }

            for(Map.Entry<String,Integer> entry : hash_table.entrySet())
            {
                System.out.println(entry.getKey() + " " + entry.getValue());
            }


        }
    }
}
