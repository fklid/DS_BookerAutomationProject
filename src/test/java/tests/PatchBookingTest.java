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

import java.util.HashMap;
import java.util.Map;

import static io.qameta.allure.Allure.step;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PatchBookingTest {

    private APIClient apiClient;
    private ObjectMapper objectMapper;
    // Исходное бронирование (создаем его полностью)
    private NewBooking originalBooking;
    private Integer bookingId;
    private NewBooking returnedBooking;

@BeforeEach
public void setup() {
    apiClient = new APIClient();
    objectMapper = new ObjectMapper();
    // Готовим полное оригинальное бронирование
    originalBooking = new NewBooking();
    originalBooking.setFirstname("John");
    originalBooking.setLastname("Smith");
    originalBooking.setTotalprice(100);
    originalBooking.setDepositpaid(true);
    originalBooking.setBookingdates(new BookingDates("2026-06-01", "2026-06-10"));
    originalBooking.setAdditionalneeds("Breakfast");
}

@Test
@Feature("Booking")
@Severity(SeverityLevel.CRITICAL)
@Owner("DNShkolnik")

public void testPartialUpdateBooking() throws Exception {

    // Создаем оригинальное бронирование
    Response createResponse = step("Создание оригинального бронирования POST ", () -> {
        String requestBody = objectMapper.writeValueAsString(originalBooking);
        return apiClient.createBooking(requestBody);
    });

    // Проверяем успешность создания
    step("Проверка статус-кода созданного бронирования ", () ->
            assertThat(createResponse.getStatusCode()).isEqualTo(200)
    );

    // Получаем ID созданного бронирования
    bookingId = step("Получение id созданного бронирования ", () ->
            createResponse.jsonPath().getInt("bookingid")
    );

    // Получаем токен авторизации
    step("Запрос токена на обновление ", () ->
            apiClient.createToken("admin", "password123")
    );

    Response patchResponse = step("Send PATCH request with HashMap", () -> {
        // Создаем Map ТОЛЬКО с полями, которые нужно обновить
        Map<String, Object> patchPayload = new HashMap<>();
        patchPayload.put("firstname", "Johnny");
        patchPayload.put("totalprice", 200);
        // Можно добавить больше полей, если нужно:
        //patchPayload.put("totalprice", 200);

        String requestBody = objectMapper.writeValueAsString(patchPayload);
        return apiClient.patchBooking(bookingId, requestBody);
    });

    // Поверяем, что обновление прошло успешно
    step("Проверка статус-кода обновленного бронирования ", () ->
            assertThat(patchResponse.getStatusCode()).isEqualTo(200)
    );

    // Получаем обновленное бронирования через GET
    returnedBooking = step("Получение обновленного бронирования с проверкой статус-кода ", () -> {
        Response getResponse = apiClient.getBookingById(bookingId);
        assertThat(getResponse.getStatusCode()).isEqualTo(200);
        return objectMapper.readValue(getResponse.getBody().asString(), NewBooking.class);
    });


    step("Проверка,что только обновленные параметры изменены ", () -> {

        assertEquals("Johnny", returnedBooking.getFirstname(),
                "Имя должно было измениться на 'Johnny' ");
        assertEquals(originalBooking.getLastname(), returnedBooking.getLastname(),
                "Фамилия осталась прежней ");
        assertEquals(200, returnedBooking.getTotalprice(),
                "Цена должна измениться ");
        assertEquals(originalBooking.isDepositpaid(), returnedBooking.isDepositpaid(),
                "Статус остался прежней ");
        assertEquals(originalBooking.getBookingdates().getCheckin(), returnedBooking.getBookingdates().getCheckin(),
                "Дата осталась прежней ");
        assertEquals(originalBooking.getBookingdates().getCheckout(), returnedBooking.getBookingdates().getCheckout(),
                "Дата осталась прежней ");
        assertEquals(originalBooking.getAdditionalneeds(), returnedBooking.getAdditionalneeds(),
                "Дополнительные условия остались прежние ");
    });
}

@AfterEach
public void tearDown() {
        step("Delete booking after test", () -> {
            apiClient.createToken("admin", "password123");
            apiClient.deleteBooking(bookingId);
            assertThat(apiClient.getBookingById(bookingId).getStatusCode()).isEqualTo(404);
        });
}
}