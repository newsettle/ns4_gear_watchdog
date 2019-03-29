package com.creditease.ns4.gear.watchdog.monitor.notify.constant;

/**
 * @author yaqiangzhao
 * 2019/1/25
 */
public class MESSAGE {
    /**
     * 报警邮件发送情况分类
     */
    public enum TYPE {
        /**
         * 启动 -> 1
         */
        START(1, "启动"),
        /**
         * 停止 -> 2
         */
        STOP(2, "停止"),
        /**
         * 异常 -> 3
         */
        ERROR(3, "异常");
        private int code;
        private String value;

        TYPE(int code, String value) {
            this.code = code;
            this.value = value;
        }

        public int getCode() {
            return this.code;
        }

        public String getValue() {
            return this.value;
        }
    }
}
