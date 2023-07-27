package com.huomiao.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author Zixuan.Yang
 * @date 2023/7/27 11:03
 */
@Data
public class TaskVo {

    private String name;

    private String url;

    private String m3u8;

    private int state;

    private String msg;

    private Date creatTime;

    private int eTime;
}
