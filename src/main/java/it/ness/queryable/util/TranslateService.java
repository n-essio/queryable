package it.ness.queryable.util;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.model.TranslationsListResponse;
import org.apache.maven.plugin.logging.Log;

public class TranslateService {

    public static String translateText(String text,
                                       String language,
                                       Log log,
                                       String google_translate_apikey) throws IOException, GeneralSecurityException
    {
        log.info("translating to lang " + language + " text : " + text);
        Translate t = new Translate.Builder(
                GoogleNetHttpTransport.newTrustedTransport()
                , GsonFactory.getDefaultInstance(), null)
                // Set your application name
                .setApplicationName("queryable")
                .build();
        Translate.Translations.List list = t.new Translations().list(
                Collections.singletonList(
                        // Pass in list of strings to be translated
                        text
                ),
                // Target language
                language.toString());

        list.setKey(google_translate_apikey);
        TranslationsListResponse response = list.execute();
        if (response.getTranslations().size() == 1) {
            String translatedText = response.getTranslations().get(0).getTranslatedText();
            if (translatedText != null) {
                translatedText = translatedText.replaceAll("&#39;", "'");
            }
            return translatedText;
        }
        return null;
    }
}