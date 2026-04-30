package com.example.common.domain;

import java.util.List;

public class PageResult<T> {
    private List<T> list;
    private int total;
    private int page;
    private int size;
    private int totalPages;
    
    public PageResult() {}
    
    public PageResult(List<T> list, int total, int page, int size) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) total / size);
    }
    
    public List<T> getList() { return list; }
    public void setList(List<T> list) { this.list = list; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public int getTotalPages() { return totalPages; }
    public void setTotalPages(int totalPages) { this.totalPages = totalPages; }
}
