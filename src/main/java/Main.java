import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.Scanner;

public class Main {

    private static String pageUrl = "";

    public static void main(String[] args) {

        Connection httpConnection;
        Scanner UserInput = new Scanner(System.in);
        String docInString = "";
        Document docInHtml = null;

        System.out.println("Introduzca una URL: ");
        pageUrl = UserInput.nextLine();

        if (!pageUrl.contains("http://")) {
            // de esta forma no hay que escribir http://
            pageUrl = String.format("http://%s", pageUrl);
        }

        try {
            httpConnection = Jsoup.connect(pageUrl).timeout(6000);
            docInString = httpConnection.execute().body();
            docInHtml = httpConnection.get();
        } catch (Exception e) {
            System.out.println("Ha ocurrido un error. Quizas la URl no es correcta");
            System.exit(0);
        }

        int lines = docInString.split("\n").length;
        int paragraphs = docInHtml.getElementsByTag("p").size();
        int images = docInHtml.getElementsByTag("img").size();
        int getForms = getTotalForms(docInHtml, "get");
        int postForms = getTotalForms(docInHtml, "post");

        System.out.println(String.format("Se han encontrado %d lineas", lines)); // point A
        System.out.println(String.format("Se han encontrado %d parrafos", paragraphs)); // point B
        System.out.println(String.format("Se han encontrados %d imagenes", images)); // point C
        System.out.println(String.format("Se han encontrado %d formularios con el metodo GET", getForms)); // point D
        System.out.println(String.format("Se han encontrado %d formularios con el metodo POST", postForms)); // point D

        System.out.println("\n");
        showInputTags(docInHtml); // point C
        System.out.println("\n");
        sendPostRequest(docInHtml); // point E
    }

    private static void showInputTags (Document doc) {

        for (Element form : doc.getElementsByTag("form")) {
            System.out.println(String.format("form method=%s action=%s", form.attr("method"), form.attr("action")));

            for (Element input : form.getElementsByTag("input")) {
                System.out.println(String.format("tipo de input=%s", input.attr("type")));
            }
        }
    }

    private static void sendPostRequest (Document doc) {
        String url = "";
        for (Element form : doc.getElementsByTag("form")) {
            if (form.attr("method").equalsIgnoreCase("post")) {
                String sendUrl = form.attr("action").contains("http") ? form.attr("action") : String.format("%s%s", pageUrl, form.attr("action"));
                int index = sendUrl.lastIndexOf('/');

                if (sendUrl.contains("https")) {
                    url = sendUrl;
                } else {
                    String protocol = sendUrl.substring(0, 4);
                    if (protocol.equalsIgnoreCase("https")) {
                        String tmp = sendUrl.substring(8);
                        String[] tmpUrl = tmp.split("/");
                        url = tmpUrl[0] + form.attr("action");
                    } else {
                        String tmp = sendUrl.substring(7);
                        String[] tmpUrl = tmp.split("/");
                        url = protocol + "://" + tmpUrl[0] + form.attr("action");
                    }
                }


                try {
                    Document request = Jsoup.connect(url).header("matricula", "20160522").data("asignatura", "practica1").post();
                    System.out.println("Respuesta Form:");
                    System.out.println(request.html());
                    System.out.println();

                } catch (Exception e) {
                    System.out.println("Ha ocurrido un error");
                    System.exit(0);
                }
            }
        }
    }

    private static int getTotalForms (Document doc, String method) {
        int total = 0;

        for (Element form : doc.getElementsByTag("form")) {
            if (form.attr("method").equalsIgnoreCase(method)) {
                total++;
            }
        }
        return total;
    }

}
