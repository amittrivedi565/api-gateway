package com.univault.gateway.gateway;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/gateway")
public class GatewayController {

    private final GatewayService gatewayService;

    @Autowired
    public GatewayController(GatewayService gatewayService){
        this.gatewayService = gatewayService;
    }

    /*
       incomingRequest is an endpoint exposed to public for serving
       provided request should contain X-Service-Name Header for redirection to the required service
       request to the protected routes will only be redirected if Authentication Header have valid token
       validation of token will be executed by auth-service
    */
    @RequestMapping("/**")
    public ResponseEntity<?> incomingRequest(HttpServletRequest request, @RequestHeader("X-Service-Name") String serviceName){

        /*
            @method will contain the incoming requests like get, post, put, delete
            @fullPath incoming request url will be like /gateway/:serviceName/:pathVariables
            @relativePath removes the gateway embedded in the url, only required is passed
        */
        String method = request.getMethod(); // GET, POST, PUT, DELETE
        String fullPath = request.getRequestURI(); // /gateway/abc/123
        String relativePath = fullPath.replaceFirst("/gateway", ""); // /abc/123

        String targetUrl = gatewayService.resolveRoute(serviceName, relativePath);
        return gatewayService.forwardRequest(targetUrl, request, method);
    }
}
