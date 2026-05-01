package com.example.da1androidnative.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PaginatedActivityHistoryResponse {
    @SerializedName("content")
    private List<ActivityHistoryResponse> content;
    @SerializedName("number")
    private Integer number;
    @SerializedName("size")
    private Integer size;
    @SerializedName("last")
    private Boolean last;
    @SerializedName("totalPages")
    private Integer totalPages;
    @SerializedName("totalElements")
    private Long totalElements;
    @SerializedName("first")
    private Boolean first;
    @SerializedName("numberOfElements")
    private Integer numberOfElements;
    @SerializedName("empty")
    private Boolean empty;

    public List<ActivityHistoryResponse> getContent() {
        return content;
    }

    public void setContent(List<ActivityHistoryResponse> content) {
        this.content = content;
    }

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Boolean getLast() {
        return last;
    }

    public void setLast(Boolean last) {
        this.last = last;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }

    public Boolean getFirst() {
        return first;
    }

    public void setFirst(Boolean first) {
        this.first = first;
    }

    public Integer getNumberOfElements() {
        return numberOfElements;
    }

    public void setNumberOfElements(Integer numberOfElements) {
        this.numberOfElements = numberOfElements;
    }

    public Boolean getEmpty() {
        return empty;
    }

    public void setEmpty(Boolean empty) {
        this.empty = empty;
    }
}
