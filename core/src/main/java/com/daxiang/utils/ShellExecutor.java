package com.daxiang.utils;

import org.apache.commons.exec.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by jiangyitao.
 */
public class ShellExecutor {

    public static String execute(String cmd, List<String> args) throws IOException {
        CommandLine commandLine = new CommandLine(cmd);
        if (args != null) {
            args.forEach(arg -> commandLine.addArgument(arg));
        }
        return execute(commandLine);
    }

    public static String execute(String cmd) throws IOException {
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
     * 执行命令
     *
     * @param cmd
     * @return watchdog，watchdog可杀掉正在执行的进程
     * @throws IOException
     */
    public static ExecuteWatchdog excuteCmdAndGetWatchdog(String cmd) throws IOException {
        CommandLine commandLine = CommandLine.parse(cmd);
        ExecuteWatchdog watchdog = new ExecuteWatchdog(Integer.MAX_VALUE);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        executor.execute(commandLine, new DefaultExecuteResultHandler());
        return watchdog;
    }
}