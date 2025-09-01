package com.gateway.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/*
 * Already receiving urlSuffix containing /institutes/:id
 * Now need to check the exposure of the custom routes
 * They can be said which have custom configs such as different exposure settings for e.g., private
 * Only certain REST methods are allowed for example on /service-name/getAll, only GET is allowed
 * We have to also check for the dynamic variables defined in the routes
 * for e.g., /service-name/getAll/:id
 *
 *
 * First we find the info about called service
 * After this extract routes from the given service name
 * */
@Service
public class GatewayRouteValidator {

    private static final Logger log = LoggerFactory.getLogger(GatewayRouteValidator.class);

    public void checkExposure(String serviceName, String urlSuffix) {

    }
}
