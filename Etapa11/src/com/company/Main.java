package com.company;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.collections4.MultiMap;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws IOException {
         final String RED = "\033[0;31m";     // RED
        //pentru crearea indexilor
        DirectIndirectIndex index = new DirectIndirectIndex();
        PartVectorialSearch my_part = null;
        //pentru baza de datae
        MongoClient mongoClient = new MongoClient("localhost", 27017);
        MongoDatabase database = mongoClient.getDatabase("ProiectRIW");
        MongoCollection direct_collection = null;
        MongoCollection inverse_collection = null;
        //pentru extragerea din baza de date
        HashMap<String,HashMap<String, Integer>> direct = null;
        MultiMap<String, HashMap<String, Integer>> invers = null;
        //pentru extragerea d{word:if*idf}
        HashMap<String,HashMap<String,Double>> my_data;
        //pentru cautarea
        String query;
        Scanner scan = new Scanner(System.in);
        File folder = new File("C:\\Users\\iongu\\Desktop\\laboratoare\\Etapa11\\src-files\\test-files");
        String map_file = "C:\\Users\\iongu\\Desktop\\laboratoare\\Etapa11\\src-files\\map_file.txt";
        String inverse_map  = "C:\\Users\\iongu\\Desktop\\laboratoare\\Etapa11\\src-files\\inv_map_file.txt";
        File vector_content_file = new File(folder + "_vector_content.json");



       // String comanda = "coals and flip or Russia and not";

        do {
            System.out.println("1. Creaza indexul direct & Introducere in baza de date");
            System.out.println("2. Extrage index direct din  baza de date");
            System.out.println("3. Creeaza index indirect & Introducerea in baza de date");
            System.out.println("4. Extrage indexul indirect din baza de date");
            System.out.println("5. Cautare query (boolean + vectorial)");
            System.out.println("6. Iesire");

            System.out.print("---->");
            System.out.println();
            Scanner in = new Scanner(System.in);
            int alegere = in.nextInt();
            System.out.println();
            switch (alegere)
            {
                case 1:

                    try
                    {
                        File my_file = new File(map_file);//fisierul cu mapare indexului direct se creaza cu append si initial se va sterge
                        if(my_file.exists())
                            my_file.delete();
                        index.directIndex(folder, map_file);
                        direct_collection = Mongo.insert_direct_index(database,index.getDirect_indices());
                    }
                    catch(Exception e)
                    {
                    System.err.println("Eroare la crearea indexului direct \n"+ e.getMessage());
                    break;
                    }
                break;
                case 2:
                    try
                    {
                        if(direct_collection!= null)
                            direct = Mongo.load_direct_index(direct_collection);
                        else
                        {
                            System.out.println("Indicele direct trebuie initial incarcat in baza de date\n");
                            break;
                        }
                    }
                    catch(Exception e)
                    {
                        System.err.println("Eroare la Extragerea  indexului direct \n"+ e.getMessage());
                        break;
                    }
                break;
                case 3:
                    try
                    {
                        if(index.getDirect_indices().size()!=0) {
                            my_part = index.indexIndirect(folder, map_file, inverse_map);
                            inverse_collection = Mongo.insert_indirect_index(database, my_part.getData());
                        }
                        else
                        {
                            System.out.println("Indexul invers se creeaza pe baza la cel direct\n");
                            break;
                        }
                    }
                    catch(Exception e)
                    {
                        System.err.println("Eroare la Crearea  indexului indirect \n"+ e.getMessage());
                        break;
                    }
                break;
                case 4:
                    try
                    {
                        if(inverse_collection!= null)
                            invers = Mongo.load_indirect_index(inverse_collection);
                        else
                        {
                            System.out.println("Indicele invers trebuie initial incarcat in baza de date\n");
                            break;
                        }
                    }
                    catch(Exception e)
                    {
                        System.err.println("Eroare la Extragerea  indexului invers \n"+ e.getMessage());
                        break;
                    }
                    break;
                case 5:
                    try
                    {
                        if(my_part.getData()== null){
                           System.out.println("Initial trebuie creat indexul indirect\n");
                           break;
                        }
                        if(vector_content_file.exists())
                            my_data = VectorialSearch.loadDocumentVector(folder);//incarcarea din fisier
                        else {
                            VectorialSearch.getDocumentVector(folder, my_part, map_file);//calcularea propriuzisa
                            my_data = VectorialSearch.loadDocumentVector(folder);
                        }
                        System.out.println("Introduceti query pentru cautare (operator: and,or,not)\n");
                        query =scan.nextLine();
                        System.out.println("Se cauta fisierele ce satisfac cautarea\n");

                        if(invers==null)
                            BooleanSearch.search(my_part.getData(),query,map_file,my_data);
                        else
                            BooleanSearch.search(invers,query,map_file,my_data);
                    }
                    catch(Exception e)
                    {

                        break;
                    }
                    break;
                case 6:
                    System.exit(0);
                default:
                    System.out.println("Optiune necunoscuta");

            }


        }while(true);




    }


}
