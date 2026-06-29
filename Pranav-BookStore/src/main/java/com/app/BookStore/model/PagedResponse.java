package com.app.BookStore.model;

import lombok.Getter;

import java.util.List;

@Getter
public class PagedResponse<T> {

    private final List<T> data;

    private final int page;

    private final int size;

    private final long totalElements;

    private final int totalPages;

    private final String sortBy;

    private final String sortDir;

    private final boolean isFirst;

    private final boolean isLast;

    public PagedResponse(List<T> data, int page, int size,
                         long totalElements, String sortBy, String sortDir) {
        this.data          = data;
        this.page          = page;
        this.size          = size;
        this.totalElements = totalElements;
        this.sortBy        = sortBy;
        this.sortDir       = sortDir;
        this.totalPages    = (size == 0) ? 0 : (int) Math.ceil((double) totalElements / size);
        this.isFirst       = (page == 0);
        this.isLast        = (page >= this.totalPages - 1);
    }
}
