package core.clients;

import core.settings.ApiEndpoints;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import io.restassured.specification.RequestSpecification;
import core.models.NewBooking;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.util.Locale.filter;

public class APIClient {

    private final String baseUrl;
    private String token;

    public APIClient() {
        this.baseUrl = determineBaseUrl();
    }

    private String determineBaseUrl() {
       String environment = System.getProperty( "env","test");
       String configFileName = "application-" + environment+ ".properties";

        Properties properties = new Properties();
        try (InputStream input =
                     getClass().getClassLoader().getResourceAsStream(configFileName)) {
            if (input == null) {
                throw new IllegalStateException("Configuration file not found: "
                        + configFileName);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load configuration file:"
                     + configFileName, e);
        }

        return properties.getProperty("baseUrl");
    }



    private RequestSpecification getRequestSpec() {
        return RestAssured.given()
                .baseUri(baseUrl)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .filter(addAuthTokenFilter()); // Фильтр для добавления токена
    }

    private Filter addAuthTokenFilter() {
        return (FilterableRequestSpecification requestSpec,
                FilterableResponseSpecification responseSpec, FilterContext ctx) -> {
            if (token != null) {
                requestSpec.header("Cookie", "token=" + token);
            }
            return ctx.next(requestSpec, responseSpec); // Продолжает выполнение запроса
        };
    }

    public void createToken(String username, String password) {
        String requestBody = String.format("{\"username\":\"%s\",\"password\":\"%s\"}", username, password);

        Response response = getRequestSpec()
                .body(requestBody)
                .when()
                .post(ApiEndpoints.AUTH.getPath())
                .then()
                .statusCode(200)
                .extract()
                .response();




        token = response.jsonPath().getString("token");
    }


    public Response ping() {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.PING.getPath()) // Используем ENUM для эндпоинта /ping
                .then()
                .statusCode(201) // Ожидаемый статус-код 201 Created
                .extract()
                .response();
    }

    public Response getBooking() {
        return getRequestSpec()
                .when()
                .get(ApiEndpoints.BOOKING.getPath()) // Используем ENUM дляэндпоинта /booking
                .then()
                .statusCode(200) // Ожидаемый статус-код 200 OK
                .extract()
                .response();
    }
    public Response getBookingById(int id) {
        return getRequestSpec()
                .pathParam("id", id)
                .when()
                .get(ApiEndpoints.BOOKING.getPathById(id))
                .then()
                .extract()
                .response();
    }



    public Response deleteBooking(int bookingId) {
        return getRequestSpec()
                .pathParam("id", bookingId)
                .when()
                .delete(ApiEndpoints.BOOKING.getPath() + "/{id}")
                .then()
                .log().all()
                .statusCode(201)
                .extract()
                .response();
    }
    public Response createBooking(String newBooking) {
        return getRequestSpec()
                .body(newBooking)
                .log().all()
                .when()
                .post(ApiEndpoints.BOOKING.getPath())
                .then()
                .log().all()
                .extract()
                .response();
    }
    public Response updateBooking(int bookingId, String updatedBooking) {
        return getRequestSpec()
                .pathParam("id", bookingId)
                .body(updatedBooking)
                .log().all()
                .when()
                .put(ApiEndpoints.BOOKING.getPath() + "/{id}")
                .then()
                .log().all()
                .extract()
                .response();
    }

}




