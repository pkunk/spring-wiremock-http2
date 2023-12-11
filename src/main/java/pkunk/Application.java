package pkunk;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

@RestController
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    private WebClient.Builder webClientBuilder;

    @GetMapping(value = "/dto", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getDto() {
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body("");
    }

    @GetMapping(value = "/test-http1", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Dto> testHttp1(
            @RequestParam(required = false) Integer port
    ) {
        return getResponse(port, HttpProtocol.HTTP11);
    }

    @GetMapping(value = "/test-http2", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Dto> testHttp2(
            @RequestParam(required = false) Integer port
    ) {
        return getResponse(port, HttpProtocol.H2C);
    }

    private ResponseEntity<Dto> getResponse(
            @Nullable Integer port,
            HttpProtocol... protocols
    ) {
        HttpClient httpClient = HttpClient.create()
                .protocol(protocols)
                .secure();

        if (port == null) {
            port = 8080;
        }

        var response = webClientBuilder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build().get()
                .uri("http://localhost:%s/dto".formatted(port))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Dto.class)
                .checkpoint()
                .block();

        return response == null
                ? ResponseEntity.noContent().build()
                : ResponseEntity.ok(response);
    }

    public record Dto() {
    }
}