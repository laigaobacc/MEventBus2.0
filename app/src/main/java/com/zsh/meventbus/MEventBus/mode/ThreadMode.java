package com.zsh.meventbus.MEventBus.mode;

public enum  ThreadMode {
    //事件的处理和发送在相同的进程
    POSTING,
    //事件的处理会在UI线程中执行
    MAIN,
    //事件的处理会后台执行
    BACKGROUND,
    //异步线程执行
    ASYNC
}
