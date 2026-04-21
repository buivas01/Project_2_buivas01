package ch.zhaw.deeplearningjava.playground;

import ai.djl.Application;
import ai.djl.Device;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExportSentimentModel {

    public static void main(String[] args) throws IOException, ModelNotFoundException, MalformedModelException, TranslateException {
        Path exportDir = Paths.get("serving/sentiment");
        Files.createDirectories(exportDir);

        Criteria<String, Classifications> criteria = Criteria.builder()
                .optApplication(Application.NLP.SENTIMENT_ANALYSIS)
                .setTypes(String.class, Classifications.class)
                .optDevice(Device.cpu())
                .optProgress(new ProgressBar())
                .build();

        System.out.println("Downloading and loading sentiment model from DJL Zoo...");
        ZooModel<String, Classifications> model = criteria.loadModel();

        // force full model load by running a prediction
        Predictor<String, Classifications> predictor = model.newPredictor();
        predictor.predict("test");
        predictor.close();

        System.out.println("Saving model to: " + exportDir.toAbsolutePath());
        model.save(exportDir, "sentiment");
        model.close();

        System.out.println("Done! Model saved to serving/sentiment/");
    }
}
