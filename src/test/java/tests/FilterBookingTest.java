package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.BookingDates;
import core.models.NewBooking;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

public class FilterBookingTest {

    private APIClient apiClient;
    private ObjectMapper objectMapper;

    // Храним все созданные бронирования для удаления в tearDown
    private List<Integer> createdBookingIds = new ArrayList<>();

    // IDs бронирований для разных сценариев
    private Integer bookingIdJohnSmith;      // John Smith, июнь
    private Integer bookingIdJohnDoe;        // John Doe, июль
    private Integer bookingIdJaneSmith;      // Jane Smith, август
    private Integer bookingIdBobJohnson;     // Bob Johnson, сентябрь

    @BeforeEach
    public void setup() throws Exception {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();

        // Создаем 4 разных бронирования с уникальными данными
        bookingIdJohnSmith = createBooking("John", "Smith", "2026-06-01", "2026-06-10");
        bookingIdJohnDoe = createBooking("John", "Doe", "2026-07-15", "2026-07-20");
        bookingIdJaneSmith = createBooking("Jane", "Smith", "2026-08-25", "2026-08-30");
        bookingIdBobJohnson = createBooking("Bob", "Johnson", "2026-09-01", "2026-09-10");
    }


    private Integer createBooking(String firstname, String lastname, String checkin, String checkout) throws Exception {
        NewBooking booking = new NewBooking();
        booking.setFirstname(firstname);
        booking.setLastname(lastname);
        booking.setTotalprice(100);
        booking.setDepositpaid(true);
        booking.setBookingdates(new BookingDates(checkin, checkout));
        booking.setAdditionalneeds("Breakfast");

        String requestBody = objectMapper.writeValueAsString(booking);
        Response response = apiClient.createBooking(requestBody);
        Integer bookingId = response.jsonPath().getInt("bookingid");
        createdBookingIds.add(bookingId);
        return bookingId;
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("DNShkolnik")

    public void testFilterByFirstName() {
        step("Фильтрация по firstName='John' должна вернуть John Smith и John Doe", () -> {
            Response response = apiClient.getBookingsWithFilters("John", null);
            assertThat(response.getStatusCode()).isEqualTo(200);

            List<Integer> returnedIds = response.jsonPath().getList("bookingid", Integer.class);

            // Должны быть в результате
            assertThat(returnedIds)
                    .as("John Smith должен быть в результате")
                    .contains(bookingIdJohnSmith);
            assertThat(returnedIds)
                    .as("John Doe должен быть в результате")
                    .contains(bookingIdJohnDoe);

            // НЕ должны быть в результате
            assertThat(returnedIds)
                    .as("Jane Smith НЕ должна быть в результате")
                    .doesNotContain(bookingIdJaneSmith);
            assertThat(returnedIds)
                    .as("Bob Johnson НЕ должен быть в результате")
                    .doesNotContain(bookingIdBobJohnson);
        });
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("DNShkolnik")

    public void testFilterByLastName() {
        step("Фильтрация по lastName='Smith' должна вернуть John Smith и Jane Smith", () -> {
            Response response = apiClient.getBookingsWithFilters(null, "Smith");
            assertThat(response.getStatusCode()).isEqualTo(200);

            List<Integer> returnedIds = response.jsonPath().getList("bookingid", Integer.class);

            // Должны быть в результате
            assertThat(returnedIds)
                    .as("John Smith должен быть в результате")
                    .contains(bookingIdJohnSmith);
            assertThat(returnedIds)
                    .as("Jane Smith должна быть в результате")
                    .contains(bookingIdJaneSmith);

            // НЕ должны быть в результате
            assertThat(returnedIds)
                    .as("John Doe НЕ должен быть в результате")
                    .doesNotContain(bookingIdJohnDoe);
            assertThat(returnedIds)
                    .as("Bob Johnson НЕ должен быть в результате")
                    .doesNotContain(bookingIdBobJohnson);
        });
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("DNShkolnik")

    public void testFilterByCheckinDate() {
        step("Фильтрация по checkin='2026-07-15'", () -> {
            Response response = apiClient.getBookingsWithDates("2026-07-15", null);
            assertThat(response.getStatusCode()).isEqualTo(200);

            List<Integer> returnedIds = response.jsonPath().getList("bookingid", Integer.class);

            //  John Smith (checkin=2026-06-01) НЕ должен быть в результате
            assertThat(returnedIds)
                    .as("John Smith НЕ должен быть в результате")
                    .doesNotContain(bookingIdJohnSmith);
        });
    }


    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("DNShkolnik")

    public void testFilterByCheckoutDate() {
        step("Фильтрация по checkout='2026-08-30' (проверка отсутствия заведомо неподходящих)", () -> {
            Response response = apiClient.getBookingsWithDates(null, "2026-08-30");
            assertThat(response.getStatusCode()).isEqualTo(200);

            List<Integer> returnedIds = response.jsonPath().getList("bookingid", Integer.class);

            // Проверка, что бронь с поздним выездом НЕ вернулась
            assertThat(returnedIds)
                    .as("Bob Johnson (checkout=2026-09-10) точно НЕ должен быть в результате")
                    .doesNotContain(bookingIdBobJohnson);
        });
    }

    @Test
    @Feature("Booking")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("DNShkolnik")

    public void testFilterByFirstNameAndLastName() {
        step("Фильтрация по firstName='John' и lastName='Smith' должна вернуть только John Smith", () -> {
            Response response = apiClient.getBookingsWithFilters("John", "Smith");
            assertThat(response.getStatusCode()).isEqualTo(200);

            List<Integer> returnedIds = response.jsonPath().getList("bookingid", Integer.class);

            // Должен быть в результате
            assertThat(returnedIds)
                    .as("John Smith должен быть в результате")
                    .contains(bookingIdJohnSmith);

            // НЕ должны быть в результате
            assertThat(returnedIds)
                    .as("John Doe НЕ должен быть в результате (другая фамилия)")
                    .doesNotContain(bookingIdJohnDoe);
            assertThat(returnedIds)
                    .as("Jane Smith НЕ должна быть в результате (другое имя)")
                    .doesNotContain(bookingIdJaneSmith);
            assertThat(returnedIds)
                    .as("Bob Johnson НЕ должен быть в результате")
                    .doesNotContain(bookingIdBobJohnson);
        });
    }

    @AfterEach
    public void tearDown() {
        if (!createdBookingIds.isEmpty()) {
            step("Удаление всех созданных бронирований", () -> {
                apiClient.createToken("admin", "password123");

                for (Integer bookingId : createdBookingIds) {
                    apiClient.deleteBooking(bookingId);
                    assertThat(apiClient.getBookingById(bookingId).getStatusCode())
                            .as("Бронирование с ID " + bookingId + " должно быть удалено")
                            .isEqualTo(404);
                }
            });
        }
    }
}
