package com.creditease.ns4.gear.watchdog.monitor.process.constant;

/**
 * @author 杨红岩
 * @description 退出码枚举类
 * @date 2019/1/16
 */
public enum ExitCode {
    OK,
    EXIT_1,
    FAIL_SAFE_HALT,
    BAD_CONFIG,
    BIND,
    MODIFIED,
    MEMORY,
    THREAD,
    ALARM_FREEZE,
    HEALTH,
    NETWORK,
    WATCHDOG_EXIT,
    CPU,
    UNKNOWN,
    UNKNOWN_ARGUMENT,
    SIGNAL;

    ExitCode() {
    }
}