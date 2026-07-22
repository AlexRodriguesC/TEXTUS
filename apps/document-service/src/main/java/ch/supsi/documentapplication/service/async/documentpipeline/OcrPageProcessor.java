package ch.supsi.documentapplication.service.async.documentpipeline;

import ch.supsi.documentapplication.configuration.properties.AppProperties;
import java.awt.image.BufferedImage;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OcrPageProcessor {

    private final String tessdataPath;

    @Autowired
    public OcrPageProcessor(AppProperties appProperties) {
        this.tessdataPath = appProperties.getTessdataPath();
    }

    public String extractText(BufferedImage image, String tesseractLanguageCode) throws TesseractException {
        Tesseract tesseract = new Tesseract();
        tesseract.setLanguage(tesseractLanguageCode);
        tesseract.setDatapath(tessdataPath);
        return tesseract.doOCR(image);
    }
}
