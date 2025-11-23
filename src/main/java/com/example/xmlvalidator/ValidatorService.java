package com.example.xmlvalidator;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.XMLConstants;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ValidatorService {

    // Classe interne pour stocker les résultats (Succès ou liste d'erreurs) [cite: 28, 29]
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() { return valid; }
        public List<String> getErrors() { return errors; }
    }

    public ValidationResult validate(File xmlFile, List<File> xsdFiles) {
        List<String> errorMessages = new ArrayList<>();

        try {
            // 1. Création de la factory de schéma (W3C XML Schema)
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

            // 2. Gestion des IMPORTS et INCLUDES
            // On définit un "ResourceResolver" pour dire au validateur :
            // "Si tu cherches un fichier importé, regarde dans le dossier du schéma principal."
            factory.setResourceResolver(new LSResourceResolver() {
                @Override
                public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
                    // systemId contient le nom du fichier importé (ex: "commonTypes.xsd")
                    if (systemId != null && !xsdFiles.isEmpty()) {
                        // On cherche dans le même répertoire que le premier fichier XSD sélectionné
                        File baseDir = xsdFiles.get(0).getParentFile();
                        File resource = new File(baseDir, systemId);

                        if (resource.exists()) {
                            // Si trouvé, on retourne une implémentation simple de LSInput
                            return new LSInputImpl(publicId, systemId, resource);
                        }
                    }
                    return null; // Laisser le processeur par défaut se débrouiller
                }
            });

            // 3. Chargement des sources XSD [cite: 17]
            Source[] schemaSources = new Source[xsdFiles.size()];
            for (int i = 0; i < xsdFiles.size(); i++) {
                schemaSources[i] = new StreamSource(xsdFiles.get(i));
            }

            // 4. Création de l'objet Schema (compile les XSD)
            Schema schema = factory.newSchema(schemaSources);

            // 5. Création du Validator
            Validator validator = schema.newValidator();

            // 6. Ajout d'un gestionnaire d'erreurs pour capturer les détails [cite: 28]
            validator.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) {
                    errorMessages.add("AVERTISSEMENT: " + exception.getMessage() + " (Ligne " + exception.getLineNumber() + ")");
                }

                @Override
                public void error(SAXParseException exception) {
                    errorMessages.add("ERREUR: " + exception.getMessage() + " (Ligne " + exception.getLineNumber() + ")");
                }

                @Override
                public void fatalError(SAXParseException exception) {
                    errorMessages.add("ERREUR FATALE: " + exception.getMessage() + " (Ligne " + exception.getLineNumber() + ")");
                }
            });

            // 7. Validation effective du XML [cite: 13, 23]
            validator.validate(new StreamSource(xmlFile));

        } catch (SAXException | IOException e) {
            // Erreurs globales (ex: fichier illisible, XSD malformé)
            errorMessages.add("Erreur critique de validation : " + e.getMessage());
        }

        // Si la liste d'erreurs est vide, c'est un succès
        return new ValidationResult(errorMessages.isEmpty(), errorMessages);
    }

    // Classe utilitaire simple requise pour le ResourceResolver
    private static class LSInputImpl implements LSInput {
        private String publicId;
        private String systemId;
        private File file;

        public LSInputImpl(String publicId, String systemId, File file) {
            this.publicId = publicId;
            this.systemId = systemId;
            this.file = file;
        }

        @Override
        public Reader getCharacterStream() { return null; }
        @Override
        public void setCharacterStream(Reader characterStream) {}
        @Override
        public InputStream getByteStream() {
            try { return new FileInputStream(file); } catch (FileNotFoundException e) { return null; }
        }
        @Override
        public void setByteStream(InputStream byteStream) {}
        @Override
        public String getStringData() { return null; }
        @Override
        public void setStringData(String stringData) {}
        @Override
        public String getSystemId() { return systemId; }
        @Override
        public void setSystemId(String systemId) { this.systemId = systemId; }
        @Override
        public String getPublicId() { return publicId; }
        @Override
        public void setPublicId(String publicId) { this.publicId = publicId; }
        @Override
        public String getBaseURI() { return file.getParentFile().toURI().toString(); }
        @Override
        public void setBaseURI(String baseURI) {}
        @Override
        public String getEncoding() { return "UTF-8"; }
        @Override
        public void setEncoding(String encoding) {}
        @Override
        public boolean getCertifiedText() { return false; }
        @Override
        public void setCertifiedText(boolean certifiedText) {}
    }
}