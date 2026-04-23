package ch.zhaw.deeplearningjava.footwear;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.imageio.ImageIO;

import ai.djl.Model;
import ai.djl.ModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.modality.cv.translator.ImageClassificationTranslator;
import ai.djl.translate.TranslateException;
import ai.djl.translate.Translator;

public class Inference {

    Predictor<Image, Classifications> predictor;

    public Inference() {
        try {
            Model model = Models.getModel();

            // Use working directory — matches WORKDIR /usr/src/app in Docker
            Path modelDir = Paths.get("models").toAbsolutePath();
            System.out.println("Loading model from: " + modelDir);

            model.load(modelDir, Models.MODEL_NAME);
            System.out.println("Model loaded successfully.");

            Translator<Image, Classifications> translator = ImageClassificationTranslator.builder()
                    .addTransform(new ToTensor())
                    .optApplySoftmax(true)
                    .build();
            predictor = model.newPredictor(translator);
            System.out.println("Predictor created successfully.");

        } catch (Exception e) {
            System.err.println("ERROR loading model: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Classifications predict(byte[] image) throws ModelException, TranslateException, IOException {
        if (predictor == null) {
            throw new IllegalStateException("Model predictor is not initialized — check startup logs for model loading errors.");
        }
        InputStream is = new ByteArrayInputStream(image);
        BufferedImage bi = ImageIO.read(is);
        Image img = ImageFactory.getInstance().fromImage(bi);
        img = img.resize(Models.IMAGE_WIDTH, Models.IMAGE_HEIGHT, false);
        return this.predictor.predict(img);
    }
}
