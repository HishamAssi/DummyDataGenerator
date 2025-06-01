/**
 * Simple test controller for verifying application health and connectivity.
 * Provides a basic endpoint to confirm that the application is running and responding to requests.
 *
 * This controller is primarily used for:
 * - Application health checks
 * - Basic connectivity testing
 * - Load balancer health checks
 *
 * @author Hisham
 */
package com.hisham.dummydatagenerator.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for basic health check operations.
 * Base path: /test
 */
@RestController
@RequestMapping("/test")
public class TestController {
    /**
     * Simple health check endpoint.
     * Returns a success message to confirm the application is running.
     *
     * @return String message indicating successful operation
     */
    @GetMapping
    public String test() {
        return "It works!";
    }
}
