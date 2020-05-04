package com.company;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class Main {
    public static void WriteFile(String fileName, String text)
    {
        FileWriter fw;
        try
        {
            fw = new FileWriter(fileName, true);
            BufferedWriter out = new BufferedWriter(fw);
            out.write(text);

            out.close();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Socket skt = null;
        String method = "GET";
        String host = "riweb.tibeica.com";
        String resursa = " /crawl/";
        String user_agent ="CLIENT RIW";
        StringBuilder sbl =new StringBuilder();
        boolean flag = true;
        while(flag)
        {
            File f = new File("error.txt");
            f.delete();
            f = new File("content.html");
            f.delete();
            sbl.append(method + resursa +" HTTP/1.1\r\n");
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
                while((line = br.readLine()) != null){
                    System.out.println(line);
                    sb.append(line + "\r\n");
                    if (line.contains("HTTP/1.1")) {
                        String items[] = line.split(" ");
                        code = Integer.parseInt(items[1]);
                    }
                }
                br.close();
                if (code ==200){//construim pagina html daca raspunsul e ok
                    String content = sb.toString();
                    if (content.indexOf("<!DOCTYPE") != -1)
                        {
                            content = content.substring(content.indexOf("<!DOCTYPE"));
                            WriteFile("content.html", content);
                        }
                    flag = false;
                }else if (code == 301){
                    String content = sb.toString();
                    String location = content.substring(content.indexOf("Location"));
                    location = location.split(": ")[1];
                    System.out.println(location);
                    host = location.substring(location.indexOf("//") + 2);
                    //flag-ul ramane true, am avut o redirectionare
                }else if (code >= 400) {//erori de server 5xx si erori de client 4xx
                    WriteFile("error.txt", sb.toString());
                    flag = false;
                }
            } catch (UnknownHostException e) {
                flag = false;
                e.printStackTrace();
            } catch (IOException e) {
                flag = false;
                e.printStackTrace();
            }
    }

    }
}
