package ch.zhaw.deeplearningjava.playground;

import ai.djl.Application;
import ai.djl.Device;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

public class SentimentAnalysis {

    Predictor<String, Classifications> predictor;

    public SentimentAnalysis() {
        try {
            Criteria<String, Classifications> criteria = Criteria.builder()
                    .optApplication(Application.NLP.SENTIMENT_ANALYSIS)
                    .setTypes(String.class, Classifications.class)
                    .optDevice(Device.cpu())
                    .build();
            ZooModel<String, Classifications> model = criteria.loadModel();
            predictor = model.newPredictor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Classifications predict(String text) throws TranslateException {
        return predictor.predict(text);
    }
}
