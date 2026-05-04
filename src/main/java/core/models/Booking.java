package core.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Booking {
    private int bookingid;

    // Конструктор
    @JsonCreator
    public Booking(@JsonProperty("bookingid") int bookingid) {
        this.bookingid = bookingid;
    }

    // Геттер и сеттер

    public int getBookingid() {
        return bookingid;
    }

    public void setBookingid(int bookingid) {
        this.bookingid = bookingid;
    }

    public class BookingById{
        private String firstname;
        private String lastname;
        private int totalprice;
        private boolean depositpaid;
        private Bookingdates bookingdates;
        private String additionalneeds;

        public String getFirstname() {
            return firstname;
        }

        public void setFirstname(String firstname) {
            this.firstname = firstname;
        }

        public String getAdditionalneeds() {
            return additionalneeds;
        }

        public void setAdditionalneeds(String additionalneeds) {
            this.additionalneeds = additionalneeds;
        }

        public Bookingdates getBookingdates() {
            return bookingdates;
        }

        public void setBookingdates(Bookingdates bookingdates) {
            this.bookingdates = bookingdates;
        }

        public boolean isDepositpaid() {
            return depositpaid;
        }

        public void setDepositpaid(boolean depositpaid) {
            this.depositpaid = depositpaid;
        }

        public int getTotalprice() {
            return totalprice;
        }

        public void setTotalprice(int totalprice) {
            this.totalprice = totalprice;
        }

        public String getLastname() {
            return lastname;
        }

        public void setLastname(String lastname) {
            this.lastname = lastname;
        }
    }
    public class Bookingdates{
        private String checkin;
        private String checkout;

        public String getCheckin() {
            return checkin;
        }

        public void setCheckin(String checkin) {
            this.checkin = checkin;
        }

        public String getCheckout() {
            return checkout;
        }

        public void setCheckout(String checkout) {
            this.checkout = checkout;
        }
    }

}


