package be.helmo.astracoinapi.model;

public class ResponseJson {

    private String response;

    public ResponseJson(String s) {
        this.response = s;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
