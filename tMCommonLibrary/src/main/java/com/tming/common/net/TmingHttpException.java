package com.tming.common.net;

/**
 *   In case the Twitter server returned HTTP error code, you can get the HTTP status code using getStatusCode() method.
 * author:  kun
 * mail : kun77416@gmail.com
 * Date: 2010-11-8
 * Time: 18:23:21
 */
public class TmingHttpException extends Exception{
	private static final long serialVersionUID = 1L;
	private int statusCode = -1;

    /**
     * Catch Expception BY String
     * @param msg
     */
    public TmingHttpException(String msg){
        super(msg);
    }

    /**
     * Catch Expception BY Exception Object
     * @param cause
     */
    public TmingHttpException(Exception cause){
      super(cause);
    }

    /**
     * Catch Expception BY String msg  and statusCode
     * @param msg
     * @param statusCode
     */
    public TmingHttpException(String msg,int statusCode){
        super(msg);
        this.statusCode= statusCode;
    }

    /**
     * Catch Expception BY  msg  and statusCode And case Exception
     * @param msg
     * @param cause
     */
    public TmingHttpException(String msg,Exception cause){
        super(msg,cause);
    }
    /**
     * Catch Expception BY  msg  and statusCode And case Exception
     * @param msg
     * @param statusCode
     */
    public TmingHttpException(String msg,Exception cause,int statusCode){
        super(msg,cause);
        this.statusCode = statusCode;
    }
}