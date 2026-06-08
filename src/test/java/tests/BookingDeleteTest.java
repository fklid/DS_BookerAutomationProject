package tests;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import core.models.Booking;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class BookingDeleteTest {

        private APIClient apiClient;
        private ObjectMapper objectMapper;

        @BeforeEach
        public void setup() {
            apiClient = new APIClient();
            objectMapper = new ObjectMapper();
            apiClient.createToken("admin", "password123");
        }

        @Test
        @Feature("Booking")
        @Severity(SeverityLevel.CRITICAL)
        @Owner("DNShkolnik")

        public void bookingDeleteTest() throws Exception {
            Response response = apiClient.getBooking();

            String responseBody = response.getBody().asString();
            List<Booking> initBookings = objectMapper.readValue(responseBody,
                    new TypeReference<List<Booking>>() {
                    });

            Booking RandomBooking = initBookings.get(new Random().nextInt(initBookings.size()));
            int bookingId = RandomBooking.getBookingid();

            Response deleteResponse = apiClient.deleteBooking(bookingId);

            Response updatedListResponse = apiClient.getBooking();

            String updatedResponseBody = updatedListResponse.getBody().asString();
            List<Booking> updatedBooking = objectMapper.readValue(updatedResponseBody,
                    new TypeReference<List<Booking>>() {});

            for (Booking booking : updatedBooking) {
                assertThat(booking.getBookingid())
                        .as("Бронирование с этим id было удалено: " + bookingId).isNotEqualTo(bookingId);
            }

        }
    }
