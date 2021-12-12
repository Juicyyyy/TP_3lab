package client;

import com.google.gson.Gson;
import DataTransferObject.Structure;
import CheckingValues.CheckingValues;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.Socket;

public class Client {

    private static Socket client;
    private static BufferedReader in;
    private static BufferedWriter out;

    public static void main(String[] args) {
        try {
            try {
                Gson parser = new Gson();
                client = new Socket("localhost", 4004);
                System.out.println("Клиент запущен!");

                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));

                int time = 0;
                String message = CheckingValues.read(in);
                Structure fun = parser.fromJson(message, Structure.class);
                System.out.println("Началась игра: 'Который логический час?'");

                while (true){
                    System.out.println("Вы что-то хотели сказать? Введите это здесь:");

                    fun.message = CheckingValues.readStringFromConsole();
                    CheckingValues.write(out, parser.toJson(fun));
                    time++;
                    message = CheckingValues.read(in);
                    fun = parser.fromJson(message, Structure.class);
                    if (fun.Flag == false){
                        System.out.println("Время: " + time + "\n" + "Ответ.Максимальное время: " + fun.number + "\n");
                        System.out.println("Игра завершена, время угадано");
                        break;
                    }
                    System.out.println("Время: " + time + "\n" + "Ответ.Максимальное время: " + fun.number + "\n");
                }
            } finally {
                client.close();
                in.close();
                out.close();
            }
        } catch (IOException e) {
            System.err.println(e);
        }

    }
}
