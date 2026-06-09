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

public class UpdateBookingTest {

    private APIClient apiClient;
    private ObjectMapper objectMapper;

    // Исходное бронирование (которое будем создавать)
    private NewBooking originalBooking;

    // Обновленное бронирование (которое будем отправлять в PUT)
    private NewBooking updatedBooking;

    // ID созданного бронирования
    private Integer bookingId;

    // Бронирование, полученное после обновления
    private NewBooking returnedBooking;

    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();

        // ИСХОДНЫЕ данные для создания бронирования
        originalBooking = new NewBooking();
        originalBooking.setFirstname("John");
        originalBooking.setLastname("Loree");
        originalBooking.setTotalprice(100);
        originalBooking.setDepositpaid(true);
        originalBooking.setBookingdates(new BookingDates("2026-06-01", "2026-06-10"));
        originalBooking.setAdditionalneeds("Breakfast");

        // ОБНОВЛЕННЫЕ данные (которые будем отправлять в PUT запросе)
        updatedBooking = new NewBooking();
        updatedBooking.setFirstname("Martin");
        updatedBooking.setLastname("Coock");
        updatedBooking.setTotalprice(250);
        updatedBooking.setDepositpaid(false);
        updatedBooking.setBookingdates(new BookingDates("2026-07-01", "2026-07-15"));
        updatedBooking.setAdditionalneeds("Dinner");
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("DNShkolnik")

    public void testUpdateBooking() throws Exception {

        // Создаем бронирование
        Response createResponse = step("Создание оригинального бронирования POST ", () -> {
            String requestBody = objectMapper.writeValueAsString(originalBooking);
            return apiClient.createBooking(requestBody);
        });


        step("Проверка статус-кода созданного бронирования ", () ->
                assertThat(createResponse.getStatusCode()).isEqualTo(200)
        );

        // Получаем ID созданного бронирования
        bookingId = step("Получение id созданного бронирования ", () ->
                createResponse.jsonPath().getInt("bookingid")
        );

        step("Запрос токена на обновление", () ->
                apiClient.createToken("admin", "password123")
        );

        // Отправляем PUT запрос для обновления бронирования
        Response updateResponse = step("Обновление бронирования запросом PUT ", () -> {
            String requestBody = objectMapper.writeValueAsString(updatedBooking);
            return apiClient.updateBooking(bookingId, requestBody);
        });

        // Проверка успешного обновления
        step("Проверка статус-кода обновленного бронирования ", () ->
                assertThat(updateResponse.getStatusCode()).isEqualTo(200)
        );

        // Получаем обновленное бронирование через GET запрос
        returnedBooking = step("Получаем обновленное бронирование по ID", () -> {
            Response getResponse = apiClient.getBookingById(bookingId);
            assertThat(getResponse.getStatusCode()).isEqualTo(200);
            return objectMapper.readValue(getResponse.getBody().asString(), NewBooking.class);
        });

        // Проверяем, что все поля обновились
        step("Проверка обновленныхь параметров", () -> {
            assertEquals(updatedBooking.getFirstname(), returnedBooking.getFirstname());
            assertEquals(updatedBooking.getLastname(), returnedBooking.getLastname());
            assertEquals(updatedBooking.getTotalprice(), returnedBooking.getTotalprice());
            assertEquals(updatedBooking.isDepositpaid(), returnedBooking.isDepositpaid());
            assertEquals(updatedBooking.getBookingdates().getCheckin(), returnedBooking.getBookingdates().getCheckin());
            assertEquals(updatedBooking.getBookingdates().getCheckout(), returnedBooking.getBookingdates().getCheckout());
            assertEquals(updatedBooking.getAdditionalneeds(), returnedBooking.getAdditionalneeds());
        });
    }

    @AfterEach
    public void tearDown() {
        if (bookingId != null) {
            step("Delete booking after test", () -> {
                apiClient.createToken("admin", "password123");
                apiClient.deleteBooking(bookingId);
                assertThat(apiClient.getBookingById(bookingId).getStatusCode()).isEqualTo(404);
            });
        }
    }
}
