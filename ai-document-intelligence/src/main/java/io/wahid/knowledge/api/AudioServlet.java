package io.wahid.knowledge.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.wahid.knowledge.application.Chirp3Transcriber;
import io.wahid.knowledge.application.domain.voice.intent.IntentClassifier;
import io.wahid.knowledge.application.domain.voice.intent.IntentResponse;
import io.wahid.knowledge.application.exception.FileProcessingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@MultipartConfig(
        fileSizeThreshold = 2 * 1024 * 1024,   // 2 MB
        maxFileSize = 200 * 1024 * 1024,       // 200 MB
        maxRequestSize = 210 * 1024 * 1024
)
public class AudioServlet extends HttpServlet {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Path UPLOAD_DIR = Path.of("/tmp/publication-upload");
    private static final Logger LOGGER = Logger.getLogger(AudioServlet.class.getName());
    private static final Pattern JOB_STATUS_PATTERN = Pattern.compile("/publication/job/status/([0-9a-fA-F-]{36})");

    transient Chirp3Transcriber transcriber;

    public AudioServlet() throws IOException {
        this.transcriber = new Chirp3Transcriber();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        setJsonContentType(resp);
        route("POST", req, resp);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        setJsonContentType(resp);
        route("GET", req, resp);
    }

    private void route(String method, HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException {
        String path = req.getPathInfo();
        switch (method) {
            case "GET":
                break;

            case "POST":
                if ("/transcribe".equals(path)) {
                    getTranscribe(req, resp);
                } else if ("/classify".equals(path)) {
                    getIntent(req, resp);
                }
                break;

            default:
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void getIntent(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        JsonNode node = MAPPER.readTree(req.getInputStream());
        String transcript = node.get("intent").asText();
        try {
            IntentResponse intentResponse = new IntentClassifier().classify(transcript);
            LOGGER.log(Level.INFO, "Servlet intent:{0}", intentResponse);

            ObjectNode root = MAPPER.createObjectNode();
            root.put("intent", intentResponse.getIntent());
            root.set("arguments", MAPPER.valueToTree(intentResponse.getArguments()));
            resp.getWriter().write(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root));
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            throw new FileProcessingException("Intent classification error", e);
        }
        //"নতুন কাস্টমার যোগ করুন"
    }

    private void getTranscribe(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        Files.createDirectories(UPLOAD_DIR);

        Part filePart = req.getPart("audio");
        if (filePart == null || filePart.getSize() == 0) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "File is required");
            return;
        }
        String transcript;
        try (InputStream in = filePart.getInputStream()) {
            transcript = transcriber.transcribe(in);
        } catch (Exception e) {
            throw new FileProcessingException("Error in audio file processing", e);
        }

        ObjectNode root = MAPPER.createObjectNode();

        // Basic info
        root.put("name", "Java RAG Platform");
        root.put("version", "1.0.0");
        root.put("description", "Retrieval-Augmented Generation platform built in pure Java");
        root.put("transcript", transcript);
        //"নতুন কাস্টমার যোগ করুন"

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.getWriter().write(MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(root));
    }

    private void setJsonContentType(HttpServletResponse resp) {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
    }
}
