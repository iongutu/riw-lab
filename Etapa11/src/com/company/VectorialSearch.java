package com.company;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;

import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.map.MultiValueMap;
import org.apache.lucene.search.DoubleValues;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class VectorialSearch {

    public static MultiMap<String, HashMap<String, Double>> getDocumentVector(File folder ,PartVectorialSearch part, String map_file) throws IOException {

        MultiMap<String, HashMap<String, Double>> general_vectors = new MultiValueMap<>();

        List<HashMap<String,Integer>> all_data;
        double tf = 0,idf = 0;
           for(String word :part.getData().keySet())
           {
                all_data = (List<HashMap<String,Integer>>)part.getData().get(word);

                for(int i = 0; i<all_data.size();++i)
                {
                    HashMap<String,Double> curr_doc_vector = new HashMap<>();
                       tf = getTf(word, (String)all_data.get(i).keySet().toArray()[0],part.getWords().get( all_data.get(i).keySet().toArray()[0]),part.getNumber().get((String)all_data.get(i).keySet().toArray()[0]));
                       idf = getIdf(word,part.getData(),map_file);
                      curr_doc_vector.put(word,tf*idf);
                      general_vectors.put((String)all_data.get(i).keySet().toArray()[0],curr_doc_vector);
                }

           }
        Gson doc = new GsonBuilder().setPrettyPrinting().create();
        String doc_vec = doc.toJson(general_vectors);
        Writer doc_writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(folder + "_vector_content.json"),"utf-8"));
        doc_writer.write(doc_vec);
        doc_writer.close();
        return general_vectors;
    }


    public  static HashMap<String, HashMap<String, Double>> loadDocumentVector(File folder) throws IOException {
        HashMap<String, HashMap<String, Double>> general_vectors = new HashMap<>();
        JsonReader jreader = new JsonReader(new InputStreamReader(new FileInputStream(folder+ "_vector_content.json"),"utf-8"));
        jreader.beginObject();
        while(jreader.hasNext())
        {
            String doc_name = jreader.nextName();
            HashMap<String, Double> curr_doc_vector = new HashMap<>();
            jreader.beginArray();
            while (jreader.hasNext())
            {
                jreader.beginObject();
                curr_doc_vector.put(jreader.nextName(), jreader.nextDouble());
                jreader.endObject();
            }
            jreader.endArray();
            general_vectors.put(doc_name, curr_doc_vector);
        }
        jreader.endObject();
        return  general_vectors;
    }



   private static double getTf(String word, String document,HashMap<String,Integer> words_from_file,int total_count_words) throws IOException {
       int word_count = words_from_file.get(word);
       return (double)word_count/total_count_words;
   }

    private static int countLinesNew(String filename) throws IOException {
        InputStream is = new BufferedInputStream(new FileInputStream(filename));
        try {
            byte[] c = new byte[1024];

            int readChars = is.read(c);
            if (readChars == -1) {
                return 0;
            }
            int count = 0;
            while (readChars == 1024) {
                for (int i=0; i<1024;) {
                    if (c[i++] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }
            while (readChars != -1) {
                for (int i=0; i<readChars; ++i) {
                    if (c[i] == '\n') {
                        ++count;
                    }
                }
                readChars = is.read(c);
            }
            return count == 0 ? 1 : count;
        } finally {
            is.close();
        }
    }
    private static double getIdf(String termen, MultiMap<String, HashMap<String, Integer>> indirect_map, String map_file) throws IOException {
        int doc_contain_word = 0;
        int nr_tot_doc = 0;
       try {
            doc_contain_word = BooleanSearch.getFilesContainingWord(indirect_map, termen).size();
            nr_tot_doc = countLinesNew(map_file);
    }catch (Exception ex)
       {
           ex.printStackTrace();
       }
       return  Math.log((double)nr_tot_doc/(1+doc_contain_word));
    }

    private static double getTfQuery(String word, LinkedList<String> words)
    {
        int nr= 0;
        for(String wor : words)
        {
            if(wor.equals(word))
            {
                ++nr;
            }
        }
        return (double)nr/words.size();
    }

    public static double similaritateCosinus(HashMap<String, Double> curr_vector,HashMap<String,Double> query_vector )
    {
        double scalar_num = 0;
        double prod_if_idf1,prod_if_idf2;
        double norm1 = 0,norm2= 0;

        for(String w : query_vector.keySet()) {
            try {
                prod_if_idf1 = curr_vector.get(w);
            } catch (Exception e)
            {
                prod_if_idf1 = 0;
            }
                prod_if_idf2 = query_vector.get(w);
                scalar_num += Math.abs(prod_if_idf1*prod_if_idf2);
                norm1 += prod_if_idf1*prod_if_idf1;
                norm2 += prod_if_idf2*prod_if_idf2;


        }
        if(scalar_num == 0 )
            return  0;
        else
        {
            double data = scalar_num/(Math.sqrt(norm1) * Math.sqrt(norm2));
            return data;
        }
    }


    public static HashMap<String, Double> sortByValue(HashMap<String, Double> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Double> > list = new LinkedList<Map.Entry<String, Double> >(hm.entrySet());

        // Sort the list
        Collections.sort(list,new Comparator<Map.Entry<String, Double> >() {
            public int compare(Map.Entry<String, Double> o1,
                               Map.Entry<String, Double> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });


        // put data from sorted list to hashmap
        HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }


    public static HashMap<String,Double> search(String query, MultiMap<String, HashMap<String, Integer>> indirect_map, String map_file, HashMap<String, HashMap<String, Double>> general_vector,List<String> resultFile) throws IOException {
        String[] splitQuery = query.split("\\s+");
        PorterStemmer p = new PorterStemmer();
        LinkedList<String> operator = new LinkedList<>();
        LinkedList<String> cuvinte = new LinkedList<>();
        int i = 0;
        while (i < splitQuery.length) {
            if (i % 2 == 0) {
                if (GetWords.notStopWord(splitQuery[i].toLowerCase())) {//when is not a stopword
                    cuvinte.add(p.stem(splitQuery[i]).toLowerCase());
                    i++;
                } else {//stopword
                    i = i + 2;
                }
            } else {//altfel e un operator
                operator.add(splitQuery[i]);
                i++;
            }
        }
        HashMap<String,Double> query_vector = new HashMap<>();
        for (String word: cuvinte)
        {
            query_vector.put(word,getTfQuery(word,cuvinte)*getIdf(word,indirect_map,map_file));
        }

        HashMap<String, Double> similaritati = new HashMap<>();
        for(String doc  : general_vector.keySet())
        {
            if(resultFile.contains(doc)) {
            double similaritatea = similaritateCosinus(general_vector.get(doc), query_vector);
            if (similaritatea != 0) {
                similaritati.put(doc, similaritatea);
            }
            }
        }

        HashMap<String, Double> sortedMap= sortByValue(similaritati);
        return sortedMap;
        //sortam documentele dupa similaritatea cosinus(descrescator)

    }

}
