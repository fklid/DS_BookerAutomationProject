package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.BookingDates;
import core.models.NewBooking;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CheckBookingByIdTest {

    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private NewBooking newBooking;

    // Переменные, которые будут заполнены внутри шагов теста
    private Integer createdBookingId;
    private NewBooking returnedBooking;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();

        // Просто готовим данные в памяти, на сервер пока не отправляем
        newBooking = new NewBooking();
        newBooking.setFirstname("Jone");
        newBooking.setLastname("Constantin");
        newBooking.setTotalprice(150);
        newBooking.setDepositpaid(true);
        newBooking.setBookingdates(new BookingDates("2026-06-01", "2026-07-01"));
        newBooking.setAdditionalneeds("Breakfast");
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("DNShkolnik")

    public void testGetBookingByIdBodyCheck() throws Exception {

        // ШАГ 1: Отправляем POST запрос и получаем Response
        Response response = step("Получение зпапроса  POST на создание бронирования", () -> {
            String requestBody = objectMapper.writeValueAsString(newBooking);
            return apiClient.createBooking(requestBody);
        });

        // ШАГ 2: Проверяем успешность создания
        step("Проверка получения статус кода", () ->
                assertThat(response.getStatusCode()).isEqualTo(200)
        );

        // ШАГ 3: Извлекаем ID созданного бронирования из JSON ответа
        createdBookingId = step("Получение  id созданного бронирования", () ->
                response.jsonPath().getInt("bookingid")
        );

        // ШАГ 4: Делаем GET запрос по ID и сразу десериализуем ответ в объект SingleBooking
        returnedBooking = step("Отправка запроса  GET на получение созданного бронирования по id", () -> {
            Response getResponse = apiClient.getBookingById(createdBookingId);
            assertThat(getResponse.getStatusCode()).isEqualTo(200);
            return objectMapper.readValue(getResponse.getBody().asString(), NewBooking.class);
        });

        // ШАГ 5: Сравниваем отправленные данные с полученными
        step("Проверьте данных бронирования", () -> {
            assertEquals(newBooking.getFirstname(), returnedBooking.getFirstname());
            assertEquals(newBooking.getLastname(), returnedBooking.getLastname());
            assertEquals(newBooking.getTotalprice(), returnedBooking.getTotalprice());
            assertEquals(newBooking.isDepositpaid(), returnedBooking.isDepositpaid());
            assertEquals(newBooking.getBookingdates().getCheckin(), returnedBooking.getBookingdates().getCheckin());
            assertEquals(newBooking.getBookingdates().getCheckout(), returnedBooking.getBookingdates().getCheckout());
            assertEquals(newBooking.getAdditionalneeds(), returnedBooking.getAdditionalneeds());
        });
    }

    @AfterEach
    public void tearDown() {
         {
            step("Удаление записи бронирования после проверок теста", () -> {
                apiClient.createToken("admin", "password123");
                apiClient.deleteBooking(createdBookingId);

                assertThat(apiClient.getBookingById(createdBookingId).getStatusCode()).isEqualTo(404);
            });
        }
    }
}