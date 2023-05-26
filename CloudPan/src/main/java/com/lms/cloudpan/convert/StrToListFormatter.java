package com.lms.cloudpan.convert;

import lombok.extern.slf4j.Slf4j;
import org.springframework.format.Formatter;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author 大忽悠
 * @create 2023/2/17 17:14
 */
@Slf4j
public class StrToListFormatter implements Formatter<List<Integer>> {
    private static final List<String> separatorList = new ArrayList<>();
    private static final String RANGE_SEPARATOR = "~";

    static {
        separatorList.add("-");
        separatorList.add(",");
        separatorList.add(" ");
    }

    @Override
    public List<Integer> parse(String str, Locale locale) throws ParseException {
        if (StringUtils.isEmpty(str)) {
            return Collections.emptyList();
        }
        List<Integer> res = new ArrayList<>();
        //挨个尝试每个分隔符进行切分
        for (String separator : separatorList) {
            String[] resStr = str.split(separator);
            if (resStr.length != 1) {
                return doConvert(res, resStr);
            }
        }
        //尝试范围解析
        String[] resStr = str.split(RANGE_SEPARATOR);
        if (resStr.length == 2) {
            return doRangeConvert(res, resStr);
        }
        //尝试把传入字符串当前整数加入集合返回
        addToRes(res, str);
        return res;
    }

    private List<Integer> doRangeConvert(List<Integer> res, String[] resStr) {
        try {
            int begin = Integer.parseInt(resStr[0]);
            int end = Integer.parseInt(resStr[1]);
            for (int i = begin; i <=end; i++) {
                 res.add(i);
            }
        } catch (Exception e) {
            log.error("字符串转换成List<Integer>过程中抛出异常: ", e);
        }
        return res;
    }

    private List<Integer> doConvert(List<Integer> res, String[] resStr) {
        for (String intStr : resStr) {
            addToRes(res, intStr);
        }
        return res;
    }

    private void addToRes(List<Integer> res, String intStr) {
        try {
            res.add(Integer.valueOf(intStr));
        } catch (Exception e) {
            log.error("字符串转换成List<Integer>过程中抛出异常: ", e);
        }
    }

    @Override
    public String print(List<Integer> listInt, Locale locale) {
        return null;
    }
}
