package com.qzk.upload.result;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description rest返回类型
 * @Date 2023-01-29-14-53
 * @Author qianzhikang
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RestResult<T> {

    private Integer code;
    private String msg;
    private T data;

    RestResult(String msg) {
        this.msg = msg;
    }


    RestResult(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }


    public RestResult<T> success() {
        return new RestResult<>(RestCode.SUCCESS.getCode(), RestCode.SUCCESS.getMessage());
    }

    public RestResult<T> success(Integer code) {
        return new RestResult<>(code, RestCode.SUCCESS.getMessage());
    }

    public RestResult<T> success(String msg) {
        return new RestResult<>(RestCode.SUCCESS.getCode(), msg);
    }

    public RestResult<T> success(T data) {
        return new RestResult<>(RestCode.SUCCESS.getCode(), RestCode.SUCCESS.getMessage(), data);
    }
    public RestResult<T> success(Integer code, String msg) {
        return new RestResult<>(code, msg);
    }


    public RestResult<T> success(String msg,T data) {
        return new RestResult<>(RestCode.SUCCESS.getCode(), msg, data);
    }

    public RestResult<T> success(Integer code, String msg, T data) {
        return new RestResult<>(code, msg, data);
    }


    public RestResult<T> error() {
        return new RestResult<>(RestCode.ERROR.getCode(), RestCode.ERROR.getMessage());
    }

    public RestResult<T> error(String msg) {
        return new RestResult<>(RestCode.ERROR.getCode(), msg);
    }

    public RestResult<T> error(Integer code,String msg) {
        return new RestResult<>(code, msg);
    }

    public RestResult<T> error(Integer code, String msg, T data) {
        return new RestResult<>(code, msg, data);
    }
}
