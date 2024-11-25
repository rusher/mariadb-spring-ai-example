package org.test.ia.mariadbtest;

import java.io.IOException;
import java.util.List;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class RagController {
  private static String MARIADB_PDF_URL =
      "https://mariadb.org/wp-content/uploads/2024/10/MariaDBServerKnowledgeBase.pdf";
  private static String BASIS_PDF =
      "https://mariadb.com/wp-content/uploads/2019/03/mariadb-and-json-flexible-data-modeling_whitepaper_1007.pdf";

  @Autowired private ChatModel chatModel;

  @Autowired private VectorStore vectorStore;

  @Autowired private JdbcTemplate jdbcTemplate;

  @GetMapping("/")
  public String base() {
    return "index.html";
  }

  @GetMapping("/purge")
  public ResponseEntity<String> purgeEmbeddings() {
    jdbcTemplate.execute("TRUNCATE TABLE vector_store");
    return ResponseEntity.ok().body("embeddings purged");
  }

  @GetMapping("/init")
  public ResponseEntity<String> init() throws IOException {
    // read and transform pdf into Documents
    Resource pdf = UrlResource.from(MARIADB_PDF_URL);
    var reader = new PagePdfDocumentReader(pdf);
    var splitter = new TokenTextSplitter();
    List<Document> documents = splitter.apply(reader.get());

    // save documents into vector store
    vectorStore.add(documents);

    // display response
    return ResponseEntity.ok()
        .body(
            String.format(
                "%d documents created from pdf file: %s", documents.size(), pdf.getFilename()));
  }

  @GetMapping("/search")
  public String vectorStoreSearch(Model model, @RequestParam(value = "message") String message) {
    // super basic query
    // similarity search from store
    String response =
        ChatClient.builder(chatModel)
            .build()
            .prompt(message)
            .advisors(new QuestionAnswerAdvisor(this.vectorStore, SearchRequest.defaults()))
            .user(message)
            .call()
            .content();
    // render
    Parser parser = Parser.builder().build();
    Node document = parser.parse(response);
    String render = HtmlRenderer.builder().build().render(document);
    model.addAttribute("message", message);
    model.addAttribute("result", render);

    return "index.html";
  }
}
