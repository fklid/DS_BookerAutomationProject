package tests;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Booking;
import core.models.BookingDates;
import core.models.CreatedBooking;
import core.models.NewBooking;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class CheckBookingListTest {

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
    @Step("Проверка: создание бронирования и получение списка")
    public void testGetBookingList() throws Exception {
        // Создаём бронирование
        apiClient.createToken("admin", "password123");
        String bookingJson = objectMapper.writeValueAsString(newBooking);
        Response createResponse = apiClient.createBooking(bookingJson);

        assertThat(createResponse.getStatusCode())
                .as("Бронирование должно быть создано")
                .isEqualTo(200);

        // Сохраняем ссылку для последующего удаления
        createdBooking = createResponse.as(CreatedBooking.class);
        Allure.addAttachment("Создано бронирование", "ID: " + createdBooking.getBookingid());

        //  Получаем список бронирований
        Response response = apiClient.getBooking();
        assertThat(response.getStatusCode()).isEqualTo(200);

        List<Booking> bookings = objectMapper.readValue(
                response.getBody().asString(),
                new TypeReference<List<Booking>>() {}
        );

        assertThat(bookings).isNotEmpty();
        assertThat(bookings).extracting(Booking::getBookingid)
                .allMatch(id -> id > 0);
    }

    @AfterEach
    @Step("Удаление записи бронирования после проверок теста")
    public void tearDown() {
        apiClient.createToken("admin", "password123");
        apiClient.deleteBooking(createdBooking.getBookingid());

        assertThat(apiClient.getBookingById(createdBooking.getBookingid()).getStatusCode()).isEqualTo(404);

    }

}
