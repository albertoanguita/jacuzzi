package jacz.util.io.http.test;

import jacz.util.io.http.HttpClient;
import jacz.util.lists.Duple;

/**
 * Created by Alberto on 06/11/2015.
 */
public class Test {

    public static void main(String[] args) throws Exception {
        Duple<Integer, String> result = HttpClient.httpRequest(
                "https://testserver01-1100.appspot.com/_ah/api/server/v1/hello",
                HttpClient.Verb.GET,
                HttpClient.ContentType.JSON);

        System.out.println(result.element1);
        System.out.println(result.element2);


        String connectRequest = "{\n" +
                " \"peerID\": \"qwertywww\",\n" +
                " \"externalMainServerPort\": \"5\",\n" +
                " \"localMainServerPort\": \"6\",\n" +
                " \"localIPAddress\": \"123\"\n" +
                "}";
        result = HttpClient.httpRequest(
                "https://testserver01-1100.appspot.com/_ah/api/server/v1/connect",
                HttpClient.Verb.POST,
                HttpClient.ContentType.JSON,
                HttpClient.ContentType.JSON,
                connectRequest);

        System.out.println(result.element1);
        System.out.println(result.element2);

    }
}
