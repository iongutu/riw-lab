package com.company;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Main {
    public static HashSet<String> getLink(Document doc) // preia link-urile de pe site (ancorele)
    {
        Elements otherLink = doc.select("a");
        HashSet<String> allURL = new HashSet<String>();
        for (Element link : otherLink) {
            //extragem linkul absolut
            String absoluteLink = link.attr("abs:href");
            //verificam exitenta anchorei pentru a o elimina ulterior
            int anchorPosition = absoluteLink.indexOf('#');
            if (anchorPosition != -1)
            {
                StringBuilder tempLink = new StringBuilder(absoluteLink);
                tempLink.replace(anchorPosition, tempLink.length(), "");
                absoluteLink = tempLink.toString();
            }
            try {
                //verifcarea cazului cand linkul absolut are o extensie de document(se verifica doar /html sau htm)
                URL real_abs_link = new URL(absoluteLink);
                String path = real_abs_link.getPath();
                if (!path.substring(path.lastIndexOf(".") + 1).isEmpty())
                {
                    if (!(path.endsWith("html") || path.endsWith("htm")))
                    {
                        continue;
                    }
                }
                allURL.add(absoluteLink);
            } catch (MalformedURLException e)
            {
                continue;
            }
        }
        return allURL;
    }
    public static String getRobots(Document doc) // preia lista de robots
    {
        Element robots = doc.selectFirst("meta[name=robots]");
        String robotsString = "";
        if (robots == null) {
            // System.out.println("Nu exista tag-ul <meta name=\"robots\">!");
        } else {
            robotsString = robots.attr("content");
            // System.out.println("Lista de robots a site-ului a fost preluata!");
        }
        return robotsString;
    }
    public static void main(String[] args) throws FileNotFoundException {
        final int  NUM_PAGE_MAX= 100;
        HttpClient httpClient = new HttpClient();
        LinkedList<String> Q = new LinkedList<>();
        HashSet<String> visitedURL =  new HashSet<>();
        HashSet<String> visitedDomanin = new HashSet<>();
        CookieHandler.setDefault(new CookieManager(null, CookiePolicy.ACCEPT_ALL));

// All the following subsequent URLConnections will use the same cookie manager.

        File myFile = new File("C:\\Users\\iongu\\Desktop\\Laboratoare\\webcrawler\\src\\com\\company\\myFile.txt");
        Scanner in;
      try{
            in = new Scanner(myFile);
            while (in.hasNext()) {
                //add all the url from file
                Q.add(in.next());
            }
            while(Q.size() > 0 && (visitedURL.size() <= NUM_PAGE_MAX))
            {

                String L = Q.pop();//pop element from queue one by one
                URL my_ulr = new URL(L);
                if(!visitedURL.contains(L))
                {
                    visitedURL.add(L);
                    String path  = my_ulr.getPath();
                            if (path.equals(""))
                            {
                                path = "/";
                            }
                            try {
                                String robots = httpClient.getResponse(my_ulr.getHost(), "/robots.txt");
                                if (robots != null) {
                                    String rulesRobot = new String(Files.readAllBytes((Paths.get(robots))));
                                    String continut = httpClient.getResponse(my_ulr.getHost(), my_ulr.getPath());
                                    if (continut != null) {
                                        Document document = Jsoup.parse(new File(continut), null, "http://" + my_ulr.getHost() + ":" + 80 + path);
                                        String robo = getRobots(document);
                                        if (!(rulesRobot.contains("noindex") && !rulesRobot.contains("none"))) {
                                            StringBuilder sb = new StringBuilder(continut);
                                            if (sb.indexOf("?") != -1) {
                                                sb.append(".txt");
                                            } else {
                                                sb.replace(sb.lastIndexOf(".") + 1, sb.length(), "txt");
                                            }
                                            String textFileName = sb.toString();

                                            // scriem rezultatul in fisierul text
                                            FileWriter fw = new FileWriter(new File(textFileName), false);
                                            fw.write(document.toString());
                                            fw.close();
                                        }
                                        if (!rulesRobot.contains("none") && !rulesRobot.contains("nofollow")) {
                                            Set<String> linkuri = getLink(document);
                                            for (String l : linkuri) {
                                                if (!visitedURL.contains(l)) {
                                                    Q.addFirst(l);
                                                }
                                            }
                                        }
                                    }

                                }

                            }catch (Exception e)
                            {
                                continue;
                            }
                }
               // URLConnection connection = my_ulr.openConnection();
               // connection = my_ulr.openConnection();
               // String domain = my_ulr.getHost();


            }

        }
      catch (FileNotFoundException | MalformedURLException e)
      {
          System.out.println("File is not found");
      } catch (IOException e) {
          e.printStackTrace();

      }
        Q.clear();
        visitedDomanin.clear();
        visitedURL.clear();
    }
}
