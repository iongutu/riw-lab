package com.company;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;

public class HttpClient {
    public static void WriteFile(String fileName, String text) {
        FileWriter fw;
        try {
            fw = new FileWriter(fileName, true);
            BufferedWriter out = new BufferedWriter(fw);
            out.write(text);

            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getResponse(String host, String resursa) {
        Socket skt = null;
        String method = "GET";
        String saveFolder = "./data";
        //String host = "riweb.tibeica.com";
        //String resursa = " /crawl/";
        String user_agent = "CLIENT RIW";
        StringBuilder sbl = new StringBuilder();
        String content = null;
        boolean flag = true;
        while (flag) {
            File f = new File("error.txt");
            f.delete();
            f = new File("content.html");
            f.delete();
            sbl.append(method + " " + resursa + " HTTP/1.1\r\n");
            sbl.append("Host: " + host + "\r\n");
            sbl.append("User-Agent: " + user_agent + "\r\n");
            sbl.append("Connection: close\r\n");
            sbl.append("\r\n");
            String cerere = sbl.toString();
            System.out.println(cerere);
            System.out.println("-----------");
            try {

                skt = new Socket(InetAddress.getByName(host), 80);
                PrintWriter pw = new PrintWriter(skt.getOutputStream());
                pw.print(cerere);
                pw.flush();
                BufferedReader br = new BufferedReader(new InputStreamReader(skt.getInputStream()));//pt raspuns
                String line;
                StringBuilder sb = new StringBuilder();
                int code = 0;
                while ((line = br.readLine()) != null) {
                    //System.out.println(line);
                    sb.append(line + "\r\n");
                    if (line.contains("HTTP/1.1")) {
                        String items[] = line.split(" ");
                        code = Integer.parseInt(items[1]);
                    }
                }
                br.close();
                String path_to_file = "";
                if (code == 200) {//construim pagina html daca raspunsul e ok
                    content = sb.toString();
                        //WriteFile("content.html", content);
                        path_to_file = saveFolder + "/" +  host + resursa;
                        if (!(path_to_file.endsWith(".html") || path_to_file.endsWith("htm")) && !resursa.equals("/robots.txt"))
                        {
                            if (!path_to_file.endsWith("/"))
                            {
                                path_to_file += "/";
                            }
                            path_to_file += "index.html";
                        }

                        File file = new File(path_to_file);
                        File parentDirectory = file.getParentFile();
                        if (!parentDirectory.exists())
                        {
                            parentDirectory.mkdirs();
                        }
                        BufferedWriter writer = new BufferedWriter(new FileWriter(path_to_file));
                        writer.write(content.toString());
                        writer.close();

                    flag = false;
                } else if (code == 301) {
                    skt.close();

                    content = sb.toString();
                    String location = content.substring(content.indexOf("Location"));
                    location = location.split(": ")[1];
                    System.out.println(location);
                    host = location.substring(location.indexOf("//") + 2);
                    URL newOne = new URL(location);
                    return getResponse(newOne.getHost(),newOne.getPath());
                    //flag-ul ramane true, am avut o redirectionare
                } else if (code >= 400) {//erori de server 5xx si erori de client 4xx
                    WriteFile("error.txt", sb.toString());
                    flag = false;
                    skt.close();
                }
                skt.close();
                return path_to_file;

            } catch (UnknownHostException e) {
                flag = false;
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                flag = false;
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

}
