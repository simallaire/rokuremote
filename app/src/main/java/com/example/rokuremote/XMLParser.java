package com.example.rokuremote;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;


public class XMLParser {
    private final DocumentBuilder documentBuilder;

    public XMLParser(DocumentBuilder documentBuilder) {
        this.documentBuilder = documentBuilder;
    }
    public XMLParser() {
        try {
            // Initialize the DocumentBuilderFactory and DocumentBuilder
            javax.xml.parsers.DocumentBuilderFactory factory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            this.documentBuilder = factory.newDocumentBuilder();
        } catch (Exception e) {
            throw new RuntimeException("Error initializing XML parser", e);
        }
    }

    // Method to parse XML data
    public Document parseXML(String xmlData) {
        try {
            // Parse the XML data using the DocumentBuilder
            // Process the parsed document as needed
            return documentBuilder.parse(new InputSource(new StringReader(xmlData)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if parsing fails
    }
}