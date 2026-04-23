package ch.zhaw.deeplearningjava.consumer;

import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;

@RestController
public class ConsumerController {

    @GetMapping("/ping")
    public String ping() {
        return "DJL Consumer app is up and running!";
    }

    private String resolveModelUri() {
        String envUrl = System.getenv("MODEL_SERVICE_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            return envUrl;
        }
        File f = new File("/.dockerenv");
        if (f.exists()) {
            return "http://model-service:8080/predictions/resnet18_v1";
        }
        return "http://localhost:8080/predictions/resnet18_v1";
    }

    @PostMapping(path = "/analyze")
    public String predict(@RequestParam("image") MultipartFile image) throws Exception {
        InputStream is = new ByteArrayInputStream(image.getBytes());
        String uri = resolveModelUri();

        var webClient = WebClient.create();
        Resource resource = new InputStreamResource(is);

        var multipartBuilder = new org.springframework.http.client.MultipartBodyBuilder();
        multipartBuilder.part("image", resource);

        var result = webClient.post()
                .uri(uri)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(multipartBuilder.build()))
                .retrieve()
                .bodyToMono(String.class)
                .block();

        return result;
    }
}
