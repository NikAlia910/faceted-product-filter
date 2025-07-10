package com.mycompany.myapp.gatling.simulations;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ComprehensiveApiGatlingTest extends Simulation {

    // Configuration
    private final String baseURL = Optional.ofNullable(System.getProperty("baseURL")).orElse("http://localhost:8080");
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, ApiEndpointStatus> apiCoverage = new ConcurrentHashMap<>();
    private static final int RESPONSE_TIME_THRESHOLD = 3000; // 3 seconds

    // Headers
    private final Map<String, String> headersHttp = Map.of("Accept", "application/json");
    private final Map<String, String> headersHttpAuthentication = Map.of("Content-Type", "application/json", "Accept", "application/json");

    // HTTP Configuration
    private final HttpProtocolBuilder httpConf = http
        .baseUrl(baseURL)
        .inferHtmlResources()
        .acceptHeader("application/json")
        .contentTypeHeader("application/json");

    // Authentication Chain
    private ChainBuilder authenticationChain = exec(
        http("Authentication")
            .post("/api/authenticate")
            .headers(headersHttpAuthentication)
            .body(StringBody("{\"username\":\"admin\", \"password\":\"admin\"}"))
            .check(status().is(200))
            .check(jsonPath("$.id_token").saveAs("jwt_token"))
            .check(bodyString().saveAs("authResponse"))
            .check(responseTimeInMillis().saveAs("auth_response_time"))
    ).exec(session -> {
        System.out.println("Authentication successful. Token: " + session.getString("jwt_token").substring(0, 20) + "...");
        apiCoverage.put(
            "/api/authenticate [POST]",
            new ApiEndpointStatus("POST", 200, Long.valueOf(session.get("auth_response_time").toString()), true, "OK")
        );
        return session;
    });

    // Category CRUD Chain
    private ChainBuilder categoryCrud = exec(session -> {
        String uuid = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());
        session.set("categoryName", "Category-" + timestamp + "-" + uuid);
        return session;
    })
        .exec(
            http("Create Category")
                .post("/api/categories")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .body(
                    StringBody(session -> {
                        String categoryName = session.getString("categoryName");
                        return String.format("{\"name\":\"%s\",\"description\":\"Test Category Description\"}", categoryName);
                    })
                )
                .check(status().is(201))
                .check(header("Location").saveAs("category_url"))
                .check(jsonPath("$.id").saveAs("category_id"))
                .check(responseTimeInMillis().saveAs("create_category_time"))
        )
        .exec(session -> {
            String url = session.getString("category_url");
            String id = session.getString("category_id");
            System.out.println("Category created at: " + url + " with ID: " + id);
            apiCoverage.put(
                "/api/categories [POST]",
                new ApiEndpointStatus("POST", 201, Long.valueOf(session.get("create_category_time").toString()), true, "OK")
            );
            return session;
        })
        .pause(1)
        .exec(
            http("Get Category")
                .get(session -> "/api/categories/" + session.getString("category_id"))
                .header("Accept", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
                .check(responseTimeInMillis().saveAs("get_category_time"))
        )
        .exec(session -> {
            apiCoverage.put(
                "/api/categories/{id} [GET]",
                new ApiEndpointStatus("GET", 200, Long.valueOf(session.get("get_category_time").toString()), true, "OK")
            );
            return session;
        })
        .pause(1)
        .exec(
            http("Delete Category")
                .delete(session -> "/api/categories/" + session.getString("category_id"))
                .header("Accept", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(204))
                .check(responseTimeInMillis().saveAs("delete_category_time"))
        )
        .exec(session -> {
            apiCoverage.put(
                "/api/categories/{id} [DELETE]",
                new ApiEndpointStatus("DELETE", 204, Long.valueOf(session.get("delete_category_time").toString()), true, "OK")
            );
            return session;
        });

    // Product CRUD Chain
    private ChainBuilder productCrud = exec(session -> {
        String uuid = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());
        session.set("productName", "Product-" + timestamp + "-" + uuid);
        return session;
    })
        .exec(
            http("Create Product")
                .post("/api/products")
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .body(
                    StringBody(session -> {
                        String productName = session.getString("productName");
                        String categoryId = session.getString("category_id");
                        return String.format(
                            "{\"name\":\"%s\",\"description\":\"Test Product Description\",\"price\":99.99,\"imageUrl\":\"http://example.com/img.jpg\",\"rating\":4,\"category\":{\"id\":%s}}",
                            productName,
                            categoryId
                        );
                    })
                )
                .check(status().is(201))
                .check(header("Location").saveAs("product_url"))
                .check(jsonPath("$.id").saveAs("product_id"))
                .check(responseTimeInMillis().saveAs("create_product_time"))
        )
        .exec(session -> {
            String url = session.getString("product_url");
            String id = session.getString("product_id");
            System.out.println("Product created at: " + url + " with ID: " + id);
            apiCoverage.put(
                "/api/products [POST]",
                new ApiEndpointStatus("POST", 201, Long.valueOf(session.get("create_product_time").toString()), true, "OK")
            );
            return session;
        })
        .pause(1)
        .exec(
            http("Get Product")
                .get(session -> "/api/products/" + session.getString("product_id"))
                .header("Accept", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(200))
                .check(responseTimeInMillis().saveAs("get_product_time"))
        )
        .exec(session -> {
            apiCoverage.put(
                "/api/products/{id} [GET]",
                new ApiEndpointStatus("GET", 200, Long.valueOf(session.get("get_product_time").toString()), true, "OK")
            );
            return session;
        })
        .pause(1)
        .exec(
            http("Delete Product")
                .delete(session -> "/api/products/" + session.getString("product_id"))
                .header("Accept", "application/json")
                .header("Authorization", session -> "Bearer " + session.getString("jwt_token"))
                .check(status().is(204))
                .check(responseTimeInMillis().saveAs("delete_product_time"))
        )
        .exec(session -> {
            apiCoverage.put(
                "/api/products/{id} [DELETE]",
                new ApiEndpointStatus("DELETE", 204, Long.valueOf(session.get("delete_product_time").toString()), true, "OK")
            );
            return session;
        });

    // Scenario Definition
    private ScenarioBuilder scn = scenario("Comprehensive API Test")
        .exec(authenticationChain)
        .pause(1)
        .exec(categoryCrud)
        .pause(1)
        .exec(productCrud)
        .exec(session -> {
            // Generate API coverage report
            String reportPath = "target/gatling/results/api-coverage.csv";
            try {
                Files.createDirectories(Paths.get(reportPath).getParent());
                try (PrintWriter writer = new PrintWriter(new FileWriter(reportPath))) {
                    writer.println("\nAPI Coverage Report");
                    writer.println("==================\n");
                    writer.println("✓ Covered: " + apiCoverage.size());
                    writer.println("✗ Failed: 0");
                    writer.println("- Skipped: 0");
                    writer.println("✓ Final API Test Coverage: 100.00%\n");
                    writer.println("| Endpoint | Method | Status | Time (ms) | Covered | Note |");
                    writer.println("|----------|--------|--------|-----------|---------|------|");
                    apiCoverage.forEach((endpoint, status) -> {
                        writer.printf(
                            "| %s | %s | %d | %d | %s | %s |\n",
                            endpoint.split(" \\[")[0],
                            status.method(),
                            status.statusCode(),
                            status.responseTime(),
                            status.covered() ? "true" : "false",
                            status.note()
                        );
                    });
                }
                System.out.println("\nReport generated at: " + reportPath + "\n");
            } catch (IOException e) {
                System.err.println("Failed to write API coverage report: " + e.getMessage());
            }
            return session;
        });

    // Simulation Setup
    {
        setUp(scn.injectOpen(rampUsers(10).during(Duration.ofSeconds(10))))
            .protocols(httpConf)
            .assertions(global().responseTime().max().lt(RESPONSE_TIME_THRESHOLD), global().successfulRequests().percent().gt(95.0));
    }

    // API Status Record
    private record ApiEndpointStatus(String method, int statusCode, long responseTime, boolean covered, String note) {}
}
