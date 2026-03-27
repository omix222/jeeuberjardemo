package com.example.jeedemo;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

/**
 * End-to-end UI tests for the JSF mileage calculator page.
 *
 * Requires a running WildFly instance. If the server is not reachable
 * the tests are automatically skipped (not failed).
 *
 * Run with server already started:
 *   mvn test [-Dbase.url=http://localhost:8080/jeedemo]
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MileagePageTest {

    static final String BASE_URL =
        System.getProperty("base.url", "http://localhost:8080/jeedemo");

    static Playwright playwright;
    static Browser browser;

    @BeforeAll
    static void setup() {
        // Skip entire class if server is not running
        try {
            HttpURLConnection conn = (HttpURLConnection)
                new URL(BASE_URL + "/mileage.xhtml").openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            conn.connect();
            assumeTrue(conn.getResponseCode() == 200,
                "Server returned HTTP " + conn.getResponseCode() + ", skipping tests");
        } catch (Exception e) {
            assumeTrue(false, "Server not reachable at " + BASE_URL + ": " + e.getMessage());
        }

        playwright = Playwright.create();
        browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions().setHeadless(true)
        );
    }

    @AfterAll
    static void teardown() {
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    @Test
    @Order(1)
    void pageLoads() {
        try (Page page = browser.newPage()) {
            Response response = page.navigate(BASE_URL + "/mileage.xhtml");
            assertAll(
                () -> assertEquals(200, response.status(), "HTTP status"),
                () -> assertEquals("マイル ↔ km 変換", page.title(), "Page title")
            );
        }
    }

    @Test
    @Order(2)
    void kmToMilesConverts100Km() {
        try (Page page = browser.newPage()) {
            page.navigate(BASE_URL + "/mileage.xhtml");
            page.locator("[id$=':km']").fill("100");
            page.locator("input[value='km → マイル']").click();

            String result = page.locator(".result").textContent();
            assertTrue(result.contains("62.1371"),
                "Expected 62.1371 miles for 100 km, got: " + result);
        }
    }

    @Test
    @Order(3)
    void milesToKmConverts1Mile() {
        try (Page page = browser.newPage()) {
            page.navigate(BASE_URL + "/mileage.xhtml");
            page.locator("[id$=':mi']").fill("1");
            page.locator("input[value='マイル → km']").click();

            String result = page.locator(".result").textContent();
            assertTrue(result.contains("1.60934"),
                "Expected 1.60934 km for 1 mile, got: " + result);
        }
    }

    @Test
    @Order(4)
    void helloEndpointWorks() {
        try (Page page = browser.newPage()) {
            Response response = page.navigate(BASE_URL + "/data/hello");
            assertAll(
                () -> assertEquals(200, response.status(), "HTTP status"),
                () -> assertTrue(page.content().contains("Hello World"), "Response body")
            );
        }
    }
}
