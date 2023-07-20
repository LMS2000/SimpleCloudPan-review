package com.lms.cloudpan.utis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.contants.HttpCode;
import com.lms.result.ResultData;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class ResponseUtil {
    /**
     * flag为true表示正确返回，flag为false表示错误返回
     * @param response
     * @param httpCode
     * @param flag
     */

    private static final ObjectMapper OBJECT_MAPPER=new ObjectMapper();
    public static void renderString(HttpServletResponse response, HttpCode httpCode,boolean flag)
    {
        try
        {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            ResultData result=null;
            if(flag){
                result=ResultData.success();

            }else{
                result=ResultData.error(httpCode,httpCode.getMessage(),null);
            }
            OBJECT_MAPPER.writeValue(response.getWriter(),result);
//            response.getWriter().print(result);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }


    /**
     * flag为true表示正确返回，flag为false表示错误返回
     * @param response
     * @param httpCode
     * @param flag
     */
    public static void renderString(HttpServletResponse response, HttpCode httpCode,boolean flag,Object data)
    {
        try
        {
            response.setStatus(200);
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            ResultData result=null;
            if(flag){
                result=ResultData.success();
            }else{
                result=ResultData.error(httpCode,httpCode.getMessage(),null);
            }
            result.put("data",data);
            OBJECT_MAPPER.writeValue(response.getWriter(),result);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
