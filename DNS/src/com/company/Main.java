package com.company;

import javax.annotation.processing.SupportedSourceVersion;
import java.io.IOException;
import java.lang.reflect.Array;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Main {
    private static String  getPointerData(int point_index, byte[] buf)//stim ca pointerul are structura 1100 0000=192dec
    {
        if((buf[point_index] & 0xff)==0)
        {
            return "";
        }else if((buf[point_index]&0xff)>=192)//ne duce la indexul de unde incepe numele
    {
        int ptrVal = ((buf[point_index] & 0x3f)<<8) | (buf[point_index+1 ] & 0xFF );
        return getPointerData(ptrVal,buf);
    }
        //am ajuns la datele propriu-zise
        int nr_cuvinte =buf[point_index++]&0xff;//first is number of words ex.[3]www.
        StringBuilder sb = new StringBuilder();
        for(int i = 0;i<nr_cuvinte;++i)
        {
            sb.append((char)(buf[point_index+i]&0xFF));
        }
        point_index += nr_cuvinte;
        return (sb.toString() + "." + getPointerData(point_index,buf));//add point to name www.

    }

    public static void main(String[] args) throws IOException {
        byte mesaj[] = new byte[31];
        Arrays.fill(mesaj, (byte) 0);
        Random x = new Random();
        StringBuilder sb = new StringBuilder();
        mesaj[1] = (byte) (0xFF & x.nextInt(255) + 1);//un identificator specific
        mesaj[5] = (byte) 1;//o singura intrebare
        String domeniu = "www.tuiasi.ro";
        String[] labels = domeniu.split("\\.");
        int idx = 12;
//sectiunea question
        //question name
        for (int i = 0; i < labels.length; ++i) {
            int tmp = labels[i].length();
            mesaj[idx++] = (byte) (tmp & 0xFF);//[3] // 6 // 2
            for (tmp = 0; tmp < labels[i].length(); ++tmp) {
                mesaj[idx++] = (byte) labels[i].charAt(tmp);//numele www / tuiasi / ro

            }
        }
        mesaj[idx] = 0;

        // 28-29 qtype = 1 ip of type ipv4  // 30-31 qclass = 1 the response will give ip adress
        mesaj[30] = mesaj[28] = (byte) 0x01;
       int question_identifier = (((0xFF)& mesaj[0])<<8) | (0xFF & mesaj[1]);
        try {
            DatagramSocket socket = new DatagramSocket();

            // send request

            InetAddress address = InetAddress.getByName("81.180.223.1");
            DatagramPacket packet = new DatagramPacket(mesaj, mesaj.length, address, 53);
            socket.send(packet);

            // get response
            byte[] buf = new byte[512];
            packet = new DatagramPacket(buf, buf.length);
            try {
                socket.receive(packet);
            }catch(SocketTimeoutException timeout)
            {
                socket.close();
                System.err.println("Socket timeout error");
            }
            // display response
            String received = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Recive:" + received);
            socket.close();

            //verify correctitude of response

            //veirficam identificatorul
            int answer_identifier = (((0xFF)& buf[0])<<8) | (0xFF & buf[1]);
            if(question_identifier==answer_identifier)
            {
                System.out.println("Identificatorii se potrivesc: " + answer_identifier +" == "+ question_identifier);
            }
            else
            {
                System.out.println("Identificatorii nu se potrivesc");

            }
            //verificam codul de raspuns(validitatea raspunsului)
            int response_code  = buf[3] & 0xFF; // ultimii patru biti 0-no_eror
            if(response_code ==0)
            {
                System.out.println("Raspunsul primit este admisibil: " + response_code);
            }
            else
            {
                System.out.println("Raspunsul este gresit: " + response_code);
            }
            //Verific Answer Record Count ANC
            //cel putin egal cu 1
            int answer_record_count = ((0xFF & buf[6])<<8) | (0xFF & buf[7]);
            if(answer_record_count>=1)
            {
                System.out.println("Macar un raspuns primit = " + answer_record_count);
            }
            else
            {
                System.out.println("Nici un raspuns primit");
            }

            System.out.println("--------------Se prelucreaza Record-ul primit de la server------------------");
            int index = 0;
            int index_resource_record = 12+domeniu.length() + 6;//header + domain_length+qtype+
            while(answer_record_count>index)
            {
                index++;
                //preluam campul name
                String name = getPointerData(index_resource_record,buf).substring(0,getPointerData(index_resource_record,buf).length()-1);
                if((buf[31]&0xff)>=192) {
                    index_resource_record += 2;
                }


                //record type - 2 octeti

                int record_type = ((buf[index_resource_record++]&0xff)<<8)| (buf[index_resource_record++] & 0xff );
                if(record_type == 1)
                {
                    System.out.println("A fost ceruta adresa ip record type = 1");
                }
                else
                {
                    System.out.println("Record type: "+record_type);
                }
                //class type -2octeti
                int class_type = ((buf[index_resource_record++]&0xff)<<8)| (buf[index_resource_record++] & 0xff );
                System.out.println("Tipul clasei:" +class_type);

                //time to live 4 octeti
                int ttl = ((buf[index_resource_record++]&0xff)<<24)|((buf[index_resource_record++]&0xff)<<16)|((buf[index_resource_record++]&0xff)<<8)|(buf[index_resource_record++]&0xff);
                System.out.println("TTL = " + TimeUnit.MILLISECONDS.toSeconds(ttl)+"s"+ (ttl-TimeUnit.MILLISECONDS.toSeconds(ttl)*1000)+ "ms");

                //rdlength 2 octeti
                int rd_length = ((buf[index_resource_record++]&0xff)<<8)| (buf[index_resource_record++] & 0xff );
                System.out.println("RD_length:"+ rd_length);
                //record data si anume adresa ip
                if(rd_length==4)
                {
                    for(int i = 0;i<4;++i)
                    {
                        sb.append(buf[index_resource_record++]&0xff);
                        sb.append(".");
                    }
                    System.out.println("Adresa IP:" +sb.toString().substring(0,sb.toString().length()-1));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
