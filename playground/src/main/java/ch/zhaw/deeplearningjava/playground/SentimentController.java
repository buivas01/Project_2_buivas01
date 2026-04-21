package ch.zhaw.deeplearningjava.playground;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SentimentController {

    private SentimentAnalysis analysis = new SentimentAnalysis();

    @GetMapping("/ping")
    public String ping() {
        return "Sentiment app is up and running!";
    }

    @GetMapping("/sentiment")
    public String predict(@RequestParam(name = "text", required = true) String text) throws Exception {
        var result = analysis.predict(text);
        return result.toJson();
    }
}
