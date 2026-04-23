package com.example.da1androidnative.data.model;

import java.util.List;

public class PaginatedReservasResponse {
    private List<ReservaResponse> content;

    public List<ReservaResponse> getContent() {
        return content;
    }

    public void setContent(List<ReservaResponse> content) {
        this.content = content;
    }
}
