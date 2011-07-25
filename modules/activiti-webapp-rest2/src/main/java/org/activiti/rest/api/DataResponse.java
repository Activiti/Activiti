package org.activiti.rest.api;

public class DataResponse {
  
  Object data;
  long total;
  int start;
  String sort;
  String order;
  int size;

  public Object getData() {
    return data;
  }

  public DataResponse setData(Object data) {
    this.data = data;
    return this;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public int getStart() {
    return start;
  }

  public void setStart(int start) {
    this.start = start;
  }

  public String getSort() {
    return sort;
  }

  public void setSort(String sort) {
    this.sort = sort;
  }

  public String getOrder() {
    return order;
  }

  public void setOrder(String order) {
    this.order = order;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }
}
