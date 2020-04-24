package com.company;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;

import java.io.*;
import java.util.*;
public class DirectIndirectIndex {

    private LinkedList<File> folders;
    private LinkedList<File> docs;
    private HashMap<String, HashMap<String,Integer>> direct_indices;
    private HashMap<String,Integer> nr_cuvinte_fisier;
    private static int i = 0;

    public DirectIndirectIndex() {
        folders = new LinkedList<>();
        docs = new LinkedList<>();
        direct_indices = new HashMap<>();
        nr_cuvinte_fisier = new HashMap<>();
    }



    public  HashMap<String, HashMap<String,Integer>> getDirect_indices()
    {
        try
        {
            return direct_indices;
        }
        catch (Exception e){

            return null;
        }

    }


    public void directIndex(File folder, String mapDirector) throws IOException {

        String folderPath  = folder.getPath();
        File[] listFiles = folder.listFiles();
        //PrintWriter inverse_map = new PrintWriter(mapDirector);
        BufferedWriter pw = new BufferedWriter(new FileWriter(mapDirector,true));
        File indexFile = new File(folderPath + "/direct"+i+"_index.txt");
        i++;
        PrintWriter directWriter  = new PrintWriter(indexFile);
        HashMap<String, HashMap<String,Integer>> cuvinte_fisier = new HashMap<>();
        for(File file: listFiles)
        {

            if(file.isFile()) {

                //System.out.println(file.getName());
                if (!file.getAbsolutePath().contains("_index"))//daca nu e fisier de indexare
                {
                    DataWords dw = GetWords.algoritm(file.getAbsolutePath());
                    nr_cuvinte_fisier.put(file.getAbsolutePath(),dw.getnrCuvinte());
                    cuvinte_fisier.put(file.getAbsolutePath(), dw.getCuvinte());
                    pw.write(file.getAbsolutePath() + "->" + indexFile.getAbsolutePath());
                    pw.newLine();
                    direct_indices.put(file.getAbsolutePath(),dw.getCuvinte());
                }

            }
            else
            {
                if(file.isDirectory())
                {
                    directIndex(file,mapDirector);
                }
            }


        }

        for(Map.Entry<String, HashMap<String, Integer>> entry : cuvinte_fisier.entrySet())
        {
            directWriter.println(entry.getKey() + "\n" +  entry.getValue());
        }

        pw.close();
        directWriter.close();
    }



    public PartVectorialSearch indexIndirect(File folder, String mapDirectFile, String inverse_map) throws IOException {
        PrintWriter pw = new PrintWriter(inverse_map);
        folders.add(folder);
        while(!folders.isEmpty())//pentru fiecare dir in parte
        {
            File file  = folders.remove();
            File[] files=  file.listFiles();
            for(File myFile :files)
            {
                if(myFile.isFile())
                {
                    docs.add(myFile);//adaugam toate fisierele
                }
                else
                {
                    if(myFile.isDirectory())
                    {
                        folders.add(myFile);//adaugam toate sub-directoarele
                    }
                }
            }
        }

        List<HashMap<String,Integer>> hashMapList  =  new ArrayList<>();
        List<String> filesList  = new ArrayList<>();


        while(!docs.isEmpty())
        {
            File myFile = docs.remove();
            if(!myFile.getAbsolutePath().contains("_index"))//daca nu e un fisier de indexare
            {
                HashMap<String,Integer> cuvinte = direct_indices.get(myFile.getAbsolutePath());//extragem hashmap-ul din fisier
                hashMapList.add(cuvinte);//adaugam intr-o lista de hashmap-uri un hashmap pentru fiecare fisier
                filesList.add(myFile.getAbsolutePath());//salvam si numele fisierului
            }
        }
        MultiMap<String, HashMap<String, Integer>> cuvinte_invers_fisier = new MultiValueMap<>();
        for(int i = 0;i<hashMapList.size();++i)//pentru fiecare hashmap
        {
            for(Map.Entry<String,Integer> entry : hashMapList.get(i).entrySet())//extragem valorile salvate
            {
                HashMap<String,Integer> tmp = new HashMap<>();
                tmp.put(filesList.get(i),entry.getValue());//schimbam valorile
                cuvinte_invers_fisier.put(entry.getKey(),tmp);//introducem in noul multi-map

            }
        }
        //mapDirectFile
        HashMap<String,String> direct_map = new HashMap<>();
        String line;
        BufferedReader br = new BufferedReader(new FileReader(mapDirectFile));
        while((line= br.readLine())!= null)
        {
            String[] parts = line.split("->",2);
            if(parts.length>=2)
            {
                direct_map.put(parts[0], parts[1]);
            }
        }
        List<HashMap<String,Integer>> all_data;
        HashMap<String,Set<String>> only_file= new HashMap();
       Set<String> keys = cuvinte_invers_fisier.keySet();
        List<String> files;
       for(String key : keys)
       {
            all_data = (List<HashMap<String,Integer>>)cuvinte_invers_fisier.get(key);
           files = new LinkedList<>();
            for(int i =0;i<all_data.size();++i)
            {
                for(String key1 : all_data.get(i).keySet())
                {
                    files.add(key1);
                }
               //in files se afla toate fisierele
                Set<String> fisiere_idx = new LinkedHashSet<>();
                for(int j=0;j<files.size();++j)
                {
                    fisiere_idx.add(direct_map.get(files.get(j)));
                }
                only_file.put(key,fisiere_idx);
            }
       }

        PrintWriter out = new PrintWriter("C:\\Users\\iongu\\Desktop\\laboratoare\\Etapa11\\src-files\\index_indirect.txt");//salvam indexul indirect
        for(Map.Entry<String,Object> entry: cuvinte_invers_fisier.entrySet())
        {
            out.println(entry.getKey()+" "+entry.getValue());
        }
        out.close();
        for(Map.Entry<String,Set<String>> entry: only_file.entrySet())
        {
            pw.println(entry.getKey()+ " " + entry.getValue());
        }
        pw.close();
        return  new PartVectorialSearch(cuvinte_invers_fisier, getDirect_indices(),nr_cuvinte_fisier);
    }

}

class PartVectorialSearch{
    private MultiMap<String, HashMap<String, Integer>> data;
    private  HashMap<String,HashMap<String,Integer>> words;
    private HashMap<String,Integer> nr_cuvinte_fisier;
    public PartVectorialSearch(MultiMap<String, HashMap<String, Integer>> data,HashMap<String,HashMap<String,Integer>> words,HashMap<String,Integer> nr_cuvinte_fisier)
    {
        this.data= data;
        this.words = words;
        this.nr_cuvinte_fisier = nr_cuvinte_fisier;
    }

    public MultiMap<String, HashMap<String, Integer>> getData() {
        return data;
    }//direct index from a file

    public HashMap<String, HashMap<String, Integer>> getWords() {
        return words;
    }// hashmap with words from each file

    public HashMap<String,Integer> getNumber() {
        return nr_cuvinte_fisier;
    }//return number of word from a file

}