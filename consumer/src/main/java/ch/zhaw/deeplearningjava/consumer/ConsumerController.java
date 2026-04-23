package ch.zhaw.deeplearningjava.consumer;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.File;
import java.time.Duration;

@RestController
public class ConsumerController {

    @GetMapping("/ping")
    public String ping() {
        return "DJL Consumer app is up and running!";
    }

    private String resolveModelUri() {
        String envUrl = System.getenv("MODEL_SERVICE_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            System.out.println("Using MODEL_SERVICE_URL: " + envUrl);
            return envUrl;
        }
        File f = new File("/.dockerenv");
        if (f.exists()) {
            System.out.println("Dockerized: using model-service");
            return "http://model-service:8080/predictions/resnet18_v1";
        }
        System.out.println("Local: using localhost");
        return "http://localhost:8080/predictions/resnet18_v1";
    }

    @PostMapping(path = "/analyze")
    public String predict(@RequestParam("image") MultipartFile image) throws Exception {
        String uri = resolveModelUri();
        System.out.println("Calling: " + uri);

        byte[] bytes = image.getBytes();
        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return image.getOriginalFilename() != null ? image.getOriginalFilename() : "image.jpg";
            }
        };

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("image", resource, MediaType.IMAGE_JPEG);

        var webClient = WebClient.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(20 * 1024 * 1024))
                .build();

        try {
            var result = webClient.post()
                    .uri(uri)
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(BodyInserters.fromMultipartData(builder.build()))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(120))
                    .block();

            System.out.println("Response: " + result);
            return result;
        } catch (WebClientResponseException e) {
            System.err.println("HTTP error " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            System.err.println("Error calling model service: " + e.getMessage());
            throw e;
        }
    }
}
