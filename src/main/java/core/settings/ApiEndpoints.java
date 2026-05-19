package core.settings;

public enum ApiEndpoints {
    PING( "/ping"),
    BOOKING("/booking"),
    BOOKING_BY_ID("/booking/%d"),
    AUTH("/auth");


    private final String path;

    ApiEndpoints(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public String getPathById(int id) {
        return path + "/" + id;
    }
}
