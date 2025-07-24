package akash.gateway.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/**")
public class ProxyController {

    private final RestTemplate restTemplate;

    public ProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private final String USER_SERVICE_URL = "http://localhost:8081";

    @RequestMapping(value = "/api/**", method =
            {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyRequest(HttpServletRequest request, HttpMethod method, @RequestBody(required = false) String body) {
        try {
            String path = request.getRequestURI().replaceFirst("/api", "");
            String url = USER_SERVICE_URL + path + (request.getQueryString() != null ? "?" + request.getQueryString() : "");

            // Copy headers from the incoming request
            HttpHeaders headers = new HttpHeaders();
            request.getHeaderNames().asIterator()
                    .forEachRemaining(headerName -> headers.add(headerName, request.getHeader(headerName)));

            HttpEntity<?> requestEntity = new HttpEntity<>(body, headers);
            // Forward the request to the user service
            ResponseEntity<String> response = restTemplate.exchange(url, method, requestEntity, String.class);

            // Return the response from the user service
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing request: " + e.getMessage());
        }
    }

}
