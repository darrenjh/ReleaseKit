package com.xlhd.kit;

/**
 * Created by yangjianhui on 2020-06-26.
 */
public class BaseResponse<T> {
  private int code;
  private String message;
  private T data;
  private String req_id;

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public String getReq_id() {
    return req_id;
  }

  public void setReq_id(String req_id) {
    this.req_id = req_id;
  }
}
