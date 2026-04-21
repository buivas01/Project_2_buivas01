package ch.zhaw.deeplearningjava.playground;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;

@RestController
public class SentimentServingController {

    private String getServingUri() {
        if (new File("/.dockerenv").exists()) {
            return "http://sentiment-serving:8080/predictions/sentiment";
        }
        return "http://localhost:8083/predictions/sentiment";
    }

    @GetMapping("/sentiment-serving")
    public String predict(@RequestParam(name = "text") String text) {
        var webClient = WebClient.create();

        String jsonBody = "{\"inputs\": \"" + text.replace("\"", "\\\"") + "\"}";

        return webClient.post()
                .uri(getServingUri())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(jsonBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
