package com.univault.gateway.registry;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/registry")
@RestController
public class RegistryController {

    private final RegistryService registryService;

    @Autowired
    public RegistryController(RegistryService registryService) {
        this.registryService = registryService;
    }

    @PostMapping("/register")
    public void register(@RequestBody InstanceInfo instance) {
        registryService.register(instance);
    }

    @PostMapping("/deregister")
    public void deregister(@RequestBody InstanceInfo instance) {
        registryService.deregister(instance);
    }


    @GetMapping("/{service}")
    public List<InstanceInfo> getInstances(@PathVariable String service) {
        return registryService.getInstances(service);
    }

}
