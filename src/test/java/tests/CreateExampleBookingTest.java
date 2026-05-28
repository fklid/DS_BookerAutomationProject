package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.BookingDates;
import core.models.CreatedBooking;
import core.models.NewBooking;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class CreateExampleBookingTest
{
    private APIClient apiClient;
    private ObjectMapper objectMapper;
    private NewBooking newBooking; // Новый объектдля созданного бронирования
    private CreatedBooking createdBooking; // Храним созданное бронирование

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();

        // Создаемобъект Booking c необходимыми данными

        newBooking = new NewBooking();
        newBooking.setFirstname("Jone");
        newBooking.setLastname("Constantin");
        newBooking.setTotalprice(150);
        newBooking.setDepositpaid(true);
        newBooking.setBookingdates(new BookingDates("2026-06-01", "2026-07-01")); // Примеры дат
        newBooking.setAdditionalneeds("Breakfast");

        }


    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("")
    public void createBooking() throws JsonProcessingException {
        //Выполняем запрос к эндпоинту,booking через APIClient
        Response response = step("Создаем новое бронирование", () -> {
            String requestBody = objectMapper.writeValueAsString(newBooking);
            return apiClient.createBooking(requestBody);
        });
        // Проверяем, что статус-код ответа 200
        step("Проверка отправки запроса", () ->
                assertThat(response.getStatusCode()).isEqualTo(200));

        // Разбираем тело ответа(десериализируем) в объект Booking
        createdBooking = step("Извлечение параметров ответа", () -> {
            String responseBody = response.getBody().asString();
            return objectMapper.readValue(responseBody, CreatedBooking.class);
        });

        step("Проверка что бронирование создано", () ->
                assertThat(createdBooking).isNotNull());

        step("Проверка получаемых после создания параметров бронирования", () -> {
            assertEquals(newBooking.getFirstname(), createdBooking.getBooking().getFirstname());
            assertEquals(newBooking.getLastname(), createdBooking.getBooking().getLastname());
            assertEquals(newBooking.getTotalprice(), createdBooking.getBooking().getTotalprice());
            assertEquals(newBooking.isDepositpaid(), createdBooking.getBooking().isDepositpaid());
            assertEquals(newBooking.getBookingdates().getCheckin(), createdBooking.getBooking().getBookingdates().getCheckin());
            assertEquals(newBooking.getBookingdates().getCheckout(), createdBooking.getBooking().getBookingdates().getCheckout());
            assertEquals(newBooking.getAdditionalneeds(), createdBooking.getBooking().getAdditionalneeds());
        });
    }

    @AfterEach
    @Step("Удаление записи бронирования после проверок теста")
    public void tearDown() {
        apiClient.createToken("admin", "password123");
        apiClient.deleteBooking(createdBooking.getBookingid());

         assertThat(apiClient.getBookingById(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404);

    }
}
