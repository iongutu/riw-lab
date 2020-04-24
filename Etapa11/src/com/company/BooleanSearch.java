package com.company;
import org.apache.commons.collections4.MultiMap;

import java.io.IOException;
import java.util.*;

public class BooleanSearch {
    public static void search(MultiMap<String, HashMap<String, Integer>> indexIndirect, String query,String mapFile,HashMap<String,HashMap<String,Double>> general_vector) throws IOException {
        String[] splitQuery = query.split("\\s+");
        PorterStemmer p = new PorterStemmer();
        LinkedList<String> operator = new LinkedList<>();
        LinkedList<String> cuvinte = new LinkedList<>();
        int i = 0;
        while (i < splitQuery.length) {
            if (i % 2 == 0) {//operand operator operand (de accea am folosit (and, or, not) care de fapt sunt stopWord-uri
                if (GetWords.notStopWord(splitQuery[i].toLowerCase())) {//caz cand nu e stopwords
                    if(GetWords.exceptionWords.contains(splitQuery[i].toLowerCase()))
                        cuvinte.add(splitQuery[i].toLowerCase());
                    else {
                        cuvinte.add(p.stem(splitQuery[i]).toLowerCase());
                    }
                    i++;
                } else {//stopword
                    i = i + 2;
                }
            } else {//altfel e un operator
                operator.add(splitQuery[i]);
                i++;
            }
        }
        boolean flag = true;//flag ce ne va indica gasirea sau nu a solutiei
        List<String> first_part = getFilesContainingWord(indexIndirect, cuvinte.remove());//gasim fisierele in care se afla cuvantul

        while (!cuvinte.isEmpty() && !operator.isEmpty()) {//procesam pana terminam queriul
            String sec_operand = cuvinte.remove();
            String currentOperator = operator.remove();
            List<String> second_part = getFilesContainingWord(indexIndirect, sec_operand);
            if (first_part != null && second_part != null) {
                first_part = executaOperatie(first_part, second_part, currentOperator);
            } else {
                flag = false;
            }
        }

        if(flag){
            for(String file : first_part)
            {
                System.out.println(file);
            }
            System.out.println("In urma procesarii vectoriale pe query. Relevanta documentelor este urmatoarea:");
            HashMap<String,Double> results =  VectorialSearch.search(query,indexIndirect, mapFile,general_vector,first_part);
            for(Map.Entry<String,Double> entry : results.entrySet())
            {
                System.out.println(entry.getKey()+  "->" + (double)Math.round(entry.getValue() * 100.0 * 100.0) / 100.0 + "%");
            }
        } else {
            System.out.println("Inexistent");
        }

    }

    public static List<String> getFilesContainingWord(MultiMap<String, HashMap<String, Integer>> indirect_map, String cuvant) {
        if (!indirect_map.containsKey(cuvant)) {//daca cuvantul nu a fost gasit
            return null;
        }
        List<HashMap<String, Integer>> listofWords = (List<HashMap<String, Integer>>) indirect_map.get(cuvant);
        List<String> listofFile = new ArrayList<>();
        for (HashMap<String, Integer> hash : listofWords) {
            listofFile.addAll(hash.keySet());
        }
        return listofFile;
    }

    private static List<String> executaOperatie(List<String> first_res, List<String>  second_res, String op) {
        List<String> rezultat = new ArrayList<>();
        List<String> cardinal_mare;
        List<String> cardinal_mic;

        if (first_res.size() < second_res.size()) {
            cardinal_mic = first_res;
            cardinal_mare = second_res;
        } else {
            cardinal_mic = second_res;
            cardinal_mare = first_res;
        }

        switch (op.toLowerCase()) {
            case "and"://cu cardinalul minim
                for (String elem : cardinal_mic) {//daca se contine in primul si al 2
                    if (cardinal_mare.contains(elem)) {
                        rezultat.add(elem);
                    }
                }
                break;
            case "or"://cardinalul maxim
                rezultat.addAll(cardinal_mare);
                for (String elem : cardinal_mic) {//se contine in primul
                    if (!cardinal_mare.contains(elem)) {//iar al 2 nu il contine
                        rezultat.add(elem);
                    }
                }
                break;
            case "not":
                for (String elem : first_res) {//se contine in unele fisiere
                    if (!second_res.contains(elem)) {//si nu se contine in celelalte
                        rezultat.add(elem);
                    }
                }
                break;
            default:
                System.out.println("Operatie Inexistana, reverifica query!\n");
                return null;
        }

        return rezultat;
    }
}
