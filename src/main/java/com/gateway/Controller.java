package com.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@CrossOrigin(origins = "*", allowedHeaders = "*", exposedHeaders = "Authorization")
@RestController
@RequestMapping("/")
public class Controller {

    private static final Logger log = LoggerFactory.getLogger(Controller.class);
    private final Service service;

    @Autowired
    public Controller(Service service) {
        this.service = service;
    }

    /*
     * Exposed endpoint of the gateway accessible by the internet
     * This will pass the incoming HttpRequest to the service layer with required Args
     * HttpServletRequest contains whole HttpRequest which has all the components such as header, body, uri...
     * serviceName is extracted from the required header
     * request is passed as it is to the @forwardRequest handler
     * */
    @RequestMapping("/**")
    public ResponseEntity<?> incomingRequest(HttpServletRequest request) {
        try {
            return service.forwardRequest(request);
        } catch (RuntimeException e) {
            log.error("Runtime exception while forwarding to service {}: {}", "TBA", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("Service unavailable: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected exception while forwarding to service {}: {}", "TBA", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error occurred");
        }
    }
}
