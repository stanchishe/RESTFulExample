package restfulExample;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class App
{
    public static void main( String[] args )
    {
        // Create constants
        final String baseUrl = "https://api.assemblyai.com/v2/transcript";
        //POST REQ
        URI baseUri;

        try {
            baseUri = new URI(baseUrl);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        //Create Transcript
        Transcript transcript = new Transcript();
        transcript.setAudio_url("https://github.com/stanchishe/SelTests/blob/main/src/audio/Thirsty.mp4?raw=true");

        // Use GSON to translate Java Obj to JSON format
        Gson gson = new Gson();
        String jsonReq = gson.toJson(transcript);

        System.out.println(jsonReq);

        //POST Request build - add url, body, params
        HttpRequest postRequest = HttpRequest.newBuilder()
                // Add URI
                .uri(baseUri)
                // Add header values
                .header("Authorization", "8991f694ac1644e2bbe783d8350568f1")
                // Add HTTP Method
                .POST(HttpRequest.BodyPublishers.ofString(jsonReq))
                // Build the request
                .build();

        // Request is build, time to send it!
        HttpClient httpClient = HttpClient.newHttpClient();

        // Create Response object to store the POST response
        HttpResponse<String> postResp;

        try {
            postResp = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // Show what we receive
        System.out.println(postResp.body());

        // Reverse translate JSON to Java Obj
        transcript = gson.fromJson(postResp.body(), Transcript.class);

        // Check the assigned id
        System.out.println(transcript.getId());

        // Create URI for the GET request
        URI getUri;

        try {
            getUri = new URI(baseUrl + "/" + transcript.getId());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        //Build the GET request
        HttpRequest getRequest = HttpRequest.newBuilder()
                // Add URI
                .uri(getUri)
                // Add header
                .header("Authorization", "8991f694ac1644e2bbe783d8350568f1")
                // Add HTTP method
                // The GET HTTP method is default for the newBuilder, can be omitted.
                //.GET()
                // Build the request
                .build();

        HttpResponse<String> getResp;

        while (true) {
            try {
                getResp = httpClient.send(getRequest, HttpResponse.BodyHandlers.ofString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            // Check the GET resp
            //System.out.println(getResp.body());

            // Add properties form the GET Response to the Transcript Obj
            transcript = gson.fromJson(getResp.body(), Transcript.class);

            // Print the Request Status
            System.out.println(transcript.getStatus());

            // Add conditions to exit the while loop
            if("completed".equals(transcript.getStatus()) || "error".equals(transcript.getStatus())) {
                break;
            }

            // Sleep for 1 second
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        if(!"error".equals(transcript.getStatus())) {
            System.out.println("Transcript Completed!");
            System.out.println(transcript.getText());
        }
    }
}
