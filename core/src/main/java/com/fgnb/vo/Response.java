package com.fgnb.vo;

/**
 * Created by jiangyitao.
 */
public class Response<T> {

    private String status;
    private String msg;
    private T data;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public static Response success(Object data) {
        Response response = new Response();
        response.setMsg("success");
        response.setStatus("1");
        response.setData(data);
        return response;
    }

    public static Response success(String msg) {
        Response response = new Response();
        response.setMsg(msg);
        response.setStatus("1");
        response.setData(null);
        return response;
    }

    public static Response success(String msg, Object data) {
        Response response = new Response();
        response.setMsg(msg);
        response.setStatus("1");
        response.setData(data);
        return response;
    }

    public static Response fail(Object data){
        Response response = new Response();
        response.setMsg("fail");
        response.setStatus("0");
        response.setData(data);
        return response;
    }
    public static Response fail(String msg){
        Response response = new Response();
        response.setMsg(msg);
        response.setStatus("0");
        response.setData(null);
        return response;
    }
    public static Response fail(String msg, Object data){
        Response response = new Response();
        response.setMsg(msg);
        response.setStatus("0");
        response.setData(data);
        return response;
    }

    public static Response error(Object data){
        Response response = new Response();
        response.setMsg("error");
        response.setStatus("-1");
        response.setData(data);
        return response;
    }
    public static Response error(String msg){
        Response response = new Response();
        response.setMsg(msg);
        response.setStatus("-1");
        response.setData(null);
        return response;
    }
    public static Response error(String msg, Object data){
        Response response = new Response();
        response.setMsg(msg);
        response.setStatus("-1");
        response.setData(data);
        return response;
    }
}
