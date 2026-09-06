package org.voxelware.coretuff.Features.Moderation.PunishmentEngine;

import java.util.List;

public class PagedResult<T> {

    private final List<T> items;
    private final int total;
    private final int page;
    private final int pageSize;
    private final int totalPages;

    public PagedResult(List<T> items, int total, int page, int pageSize) {
        this.items = items;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = pageSize > 0 ? (int) Math.ceil((double) total / pageSize) : 0;
    }

    public List<T> getItems() { return items; }
    public int getTotal() { return total; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public int getTotalPages() { return totalPages; }
}
