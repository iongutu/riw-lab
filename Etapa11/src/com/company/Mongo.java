package com.company;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.apache.commons.collections4.MultiMap;
import org.apache.commons.collections4.MultiMapUtils;
import org.apache.commons.collections4.map.MultiValueMap;
import org.bson.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Mongo {

    public static  MongoCollection insert_direct_index(MongoDatabase database, HashMap<String, HashMap<String,Integer>> directIndex)
    {
        MongoCollection direct_collection = database.getCollection("directIndex");
        direct_collection.drop();
        BasicDBObject fileDB = new BasicDBObject();
        for (Map.Entry<String, HashMap<String, Integer>> file : directIndex.entrySet()) {
            fileDB.put("doc", file.getKey());
            ArrayList<DBObject> array = new ArrayList<>();
            DBObject dbo;
            for (Map.Entry<String, Integer> entry : directIndex.get(file.getKey()).entrySet()) {
                BasicDBObject document = new BasicDBObject();
                document.put("t",entry.getKey());
                document.put("c",entry.getValue());
                dbo = document;
                array.add(dbo);
            }
            fileDB.put("terms",array);
            direct_collection.insertOne(new Document(fileDB));
        }
        return direct_collection;
    }

    public  static  MongoCollection insert_indirect_index(MongoDatabase database, MultiMap<String, HashMap<String, Integer>> my_part)
    {
        MongoCollection inverse_collection = database.getCollection("inverseIndex");
        inverse_collection.drop();
        BasicDBObject wordDB = new BasicDBObject();
        for (String word  : my_part.keySet()) {
            wordDB.put("term", word);
            ArrayList<DBObject> array = new ArrayList<>();
            Collection<HashMap<String,Integer>> data = (Collection<HashMap<String, Integer>>) my_part.get(word);
            DBObject dbo;
            for (HashMap<String, Integer> entry : data) {
                BasicDBObject document = new BasicDBObject();
                for(String key : entry.keySet()) {
                    document.put("d", key);
                    document.put("c", entry.get(key));
                }
                dbo = document;
                array.add(dbo);
            }
            wordDB.put("docs",array);
            inverse_collection.insertOne(new Document(wordDB));
        }
        return  inverse_collection;
    }


    public static HashMap<String, HashMap<String,Integer>> load_direct_index(MongoCollection collection)
    {
        MongoCursor<Document> myCursor = collection.find().iterator();
        HashMap<String, HashMap<String,Integer>> direct_index = new HashMap<>();
        try
        {
            while(myCursor.hasNext())
            {
                String cuvant = null;
                for(Map.Entry<String, Object> entry : myCursor.next().entrySet())
                {
                    if(entry.getKey().equals("doc"))
                    {
                        cuvant = entry.getValue().toString();
                        direct_index.put(cuvant,new HashMap<>());
                    }
                    else if(entry.getKey().equals("terms"))
                    {
                        Matcher isMatch = Pattern.compile(("t=(.*?)}")).matcher(entry.getValue().toString());
                        while(isMatch.find())
                        {
                            String[] data = isMatch.group().split(",");
                            data[0] = data[0].substring(2);
                            direct_index.get(cuvant).put(data[0],Integer.parseInt(data[1].substring(3,data[1].length()-1)));
                        }
                    }

                }
            }
            myCursor.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return direct_index;
    }



    public static MultiMap<String, HashMap<String, Integer>> load_indirect_index(MongoCollection collection)
    {
        MongoCursor<Document> myCursor = collection.find().iterator();
        MultiMap<String, HashMap<String, Integer>> inverse_index = new MultiValueMap<>();
        HashMap<String,Integer> my_data;
        try
        {
            while(myCursor.hasNext())
            {
                String cuvant = null;
                for(Map.Entry<String, Object> entry : myCursor.next().entrySet())
                {
                    if(entry.getKey().equals("term"))
                    {
                        cuvant = entry.getValue().toString();

                    }
                    else if(entry.getKey().equals("docs"))
                    {

                        //inverse_index.put(cuvant,my_data);
                        Matcher isMatch = Pattern.compile(("d=(.*?)}")).matcher(entry.getValue().toString());
                        while(isMatch.find())
                        {
                            my_data = new HashMap<>();
                            String[] data = isMatch.group().split(",");
                            my_data.put(data[0].substring(2),Integer.parseInt(data[1].substring(3,data[1].length()-1)));
                            inverse_index.put(cuvant,my_data);

                        }
                    }

                }
            }
            myCursor.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
        return inverse_index;
    }

}
