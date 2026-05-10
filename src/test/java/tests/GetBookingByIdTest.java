package tests;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.clients.APIClient;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;

public class GetBookingByIdTest {

    private APIClient apiClient;
    private ObjectMapper objectMapper;

    // Инициализация API клиента перед каждым тестом
    @BeforeEach
    public void setup() {
        apiClient = new APIClient();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testGetBookingById() throws Exception {
        // Выполняем запрос к эндпоинту /booking через APIClient
        Response response = apiClient.getBooking( );

        // Проверяем, что статус-код ответа равен 200
        assertThat(response.getStatusCode()).isEqualTo(200);
    }

    @Test
    public void testGetBookingByIdBodyCheck() throws Exception {

        Response response = apiClient.getBooking(5);

        step(   "Проверки параметров ответа", () -> {
            assertThat(response.getStatusCode()).isEqualTo(200);
        }
        );
    }




}