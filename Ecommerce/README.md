# C2-GLG204-2020
This repository is for Architectures Logicielles Java(2) Project

Ecommerce Project

- build a scalable social networking websites

 1- Microservices with RESTful endpoints with JSON output
2- Service discovery using Consul
3- External configuration using Consul

Part 1, we will build the below components

Tools & Technologies used: 
    - Spring Boot
    - Spring Cloud 
    - Spring Data 
    - H2 DB 
    - Consul

-------
Finally after 5 hours consul worked!
use : consul agent -dev -node machine
-------

Consul: Consul is used for service discovery and external configuration.

Consul also makes API public
to get the Public API: http://localhost:8500/ui/dc1/services


Products API:
- http://localhost:8090/product-service/products

Images API: 
- http://localhost:9090/image-service/images
