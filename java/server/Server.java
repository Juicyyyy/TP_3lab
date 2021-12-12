package server;

import com.google.gson.Gson;
import DataTransferObject.Structure;
import CheckingValues.CheckingValues;
import org.xml.sax.SAXException;
import javax.xml.XMLConstants;
import javax.xml.bind.*;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.stream.Collectors;

public class Server {

    private static ServerSocket server;
    private static BufferedReader in;
    private static BufferedWriter out;
    private static File stateFileServer;
    private static File stateFileClient;

    public static void main(String[] args) {
        try {
            try {
                Gson parser = new Gson();
                server = new ServerSocket(4004);
                System.out.println("Сервер запущен!");

                stateFileServer = new File("state.xml");
                stateFileClient = new File("state2.xml");
                JAXBContext jaxbContext = JAXBContext.newInstance(Structure.class);
                SchemaOutputResolver sor = new MySchemaOutputResolver();
                jaxbContext.generateSchema(sor);
                sor.createOutput("src/main/resources", "schema1.xsd");
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Schema schema = schemaFactory.newSchema(new File("schema1.xsd"));
                Marshaller marshaller = jaxbContext.createMarshaller();
                Marshaller marshaller2 = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                marshaller2.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                Unmarshaller unmarshaller2 = jaxbContext.createUnmarshaller();
                marshaller.setSchema(schema);
                marshaller2.setSchema(schema);
                unmarshaller.setSchema(schema);
                unmarshaller2.setSchema(schema);

                Socket clientSocket = server.accept();
                try {
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

                    int time = 0;
                    String message;
                    Structure mes = initializeFistMessageAccordingToStateFileExisting(unmarshaller);
                    int number = mes.number;
                    marshaller.marshal(mes, stateFileServer);
                    CheckingValues.write(out, parser.toJson(mes));
                    while (true){
                        message = CheckingValues.read(in);
                        mes = parser.fromJson(message, Structure.class);
                        time++;
                        if (time == mes.number) {
                            mes.Flag = false;
                            mes.message = "Игра завершена, время угадано";
                            CheckingValues.write(out, parser.toJson(mes));
                            Structure messageClient = new Structure(mes.message, time, mes.Flag);
                            marshaller2.marshal(messageClient, stateFileClient);
                            }
                        else {
                            if (time < mes.number){
                                mes.number = number;
                                CheckingValues.write(out, parser.toJson(mes));
                            }
                        }
                        System.out.println("Время: " + time + "\n" + "Сообщение: " + mes.message + "\n");
                        Structure messageClient = new Structure(mes.message, time, true);
                        marshaller2.marshal(messageClient, stateFileClient);
                    }
                } finally {
                    clientSocket.close();
                    in.close();
                    out.close();
                }
            } finally {
                System.out.println("Сервер закрыт!");
                server.close();
            }
        } catch (IOException | JAXBException | SAXException e) {
            System.err.println(e);
        }
    }

    public static class MySchemaOutputResolver extends SchemaOutputResolver {

        public Result createOutput(String namespaceURI, String suggestedFileName) throws IOException {
            File file = new File(suggestedFileName);
            StreamResult result = new StreamResult(file);
            result.setSystemId(file.toURI().toURL().toString());
            return result;
        }

    }

    private static Structure initializeFistMessageAccordingToStateFileExisting(Unmarshaller unmarshaller)
            throws JAXBException, SAXException {
        Structure message;
        if (!stateFileServer.exists()) {
            System.out.println("Введите целочисленное время:");
            message = new Structure("Началась игра: 'Который логический час?'", CheckingValues.readIntFromConsole(), true);
        }
        else {
            System.out.println("Прошлая игра была завершена некорректно");
            Structure savedMessage = (Structure) unmarshaller.unmarshal(stateFileClient);
            System.out.println("Прошлое время:" + savedMessage.number);
            message = new Structure("Игра началась из сохраненного состояния", savedMessage.number, true);
        }
        return message;
    }
}

