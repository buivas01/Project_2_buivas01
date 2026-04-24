package ch.zhaw.deeplearningjava.consumer;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<String> predict(@RequestParam("image") MultipartFile image) throws Exception {
        String uri = resolveModelUri();
        System.out.println("Calling: " + uri);

        byte[] bytes = image.getBytes();
        ByteArrayResource resource = new ByteArrayResource(bytes);

        var webClient = WebClient.builder()
                .codecs(c -> c.defaultCodecs().maxInMemorySize(20 * 1024 * 1024))
                .build();

        try {
            var result = webClient.post()
                    .uri(uri)
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(BodyInserters.fromResource(resource))
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(120))
                    .block();

            result = result.replaceAll("\"n\\d{8} ", "\"");
            System.out.println("Response: " + result);
            return ResponseEntity.ok(result);
        } catch (WebClientResponseException e) {
            System.err.println("HTTP error " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
            HttpStatus status = e.getStatusCode().value() == 403 || e.getStatusCode().value() == 503
                    ? HttpStatus.SERVICE_UNAVAILABLE
                    : HttpStatus.BAD_GATEWAY;
            return ResponseEntity.status(status)
                    .body("Model service unavailable (" + e.getStatusCode() + "). Please try again later.");
        } catch (Exception e) {
            System.err.println("Error calling model service: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Could not reach model service: " + e.getMessage());
        }
    }
}
