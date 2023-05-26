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
public class StrToListStrFormatter implements Formatter<List<String>> {
    private static final List<String> separatorList = new ArrayList<>();

    static {
        separatorList.add(" ");
    }

    @Override
    public List<String> parse(String str, Locale locale) throws ParseException {
        if (StringUtils.isEmpty(str)) {
            return Collections.emptyList();
        }
        List<String> res = new ArrayList<>();
        //挨个尝试每个分隔符进行切分
        for (String separator : separatorList) {
            String[] resStr = str.split(separator);
            if (resStr.length != 1) {
                res.addAll(List.of(resStr));
                return res;
            }
        }
        //尝试把传入字符串当前整数加入集合返回
        res.add(str);
        return res;
    }

    @Override
    public String print(List<String> object, Locale locale) {
        return null;
    }
}
