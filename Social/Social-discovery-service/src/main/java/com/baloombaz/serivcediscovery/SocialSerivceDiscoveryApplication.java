package com.baloombaz.serivcediscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class SocialSerivceDiscoveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(SocialSerivceDiscoveryApplication.class, args);
    }

}
