package com.daxiang.utils;

import org.apache.commons.exec.*;
import org.openqa.selenium.Platform;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by jiangyitao.
 */
public class ShellExecutor {

    public static final boolean IS_WINDOWS = Platform.getCurrent().is(Platform.WINDOWS);

    public static String execute(String cmd, List<String> args) throws IOException {
        cmd = handleCmd(cmd);
        CommandLine commandLine = new CommandLine(cmd);
        if (args != null) {
            args.forEach(arg -> commandLine.addArgument(arg));
        }
        return execute(commandLine);
    }

    public static String execute(String cmd) throws IOException {
        cmd = handleCmd(cmd);
        return execute(CommandLine.parse(cmd));
    }

    private static String execute(CommandLine commandLine) throws IOException {
        DefaultExecutor executor = new DefaultExecutor();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ByteArrayOutputStream errorStream = new ByteArrayOutputStream()) {
            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
            executor.setStreamHandler(pumpStreamHandler);
            executor.execute(commandLine);
            return outputStream.toString() + errorStream.toString();
        }
    }

    /**
     * 异步执行命令
     *
     * @param cmd
     * @return watchdog，watchdog可杀掉正在执行的进程
     * @throws IOException
     */
    public static ExecuteWatchdog excuteAsyncAndGetWatchdog(String cmd, PumpStreamHandler pumpStreamHandler) throws IOException {
        cmd = handleCmd(cmd);
        CommandLine commandLine = CommandLine.parse(cmd);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(Integer.MAX_VALUE);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        if (pumpStreamHandler != null) {
            executor.setStreamHandler(pumpStreamHandler);
        }
        executor.execute(commandLine, new DefaultExecuteResultHandler());
        return watchdog;
    }

    private static String handleCmd(String cmd) {
        if (StringUtils.isEmpty(cmd)) {
            throw new IllegalArgumentException("cmd can not be empty!");
        }
        if (IS_WINDOWS) {
            cmd = "cmd /C " + cmd;
        }
        return cmd;
    }
}