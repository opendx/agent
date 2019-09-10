package com.daxiang.utils;

import org.apache.commons.exec.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by jiangyitao.
 */
public class ShellExecutor {

    /**
     * 同步执行命令
     *
     * @param executable
     * @param args
     * @return
     * @throws IOException
     */
    public static String execute(String executable, List<String> args) throws IOException {
        DefaultExecutor executor = new DefaultExecutor();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ByteArrayOutputStream errorStream = new ByteArrayOutputStream()) {
            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
            executor.setStreamHandler(pumpStreamHandler);
            executor.execute(createCommandLine(executable, args));
            return outputStream.toString() + errorStream.toString();
        }
    }

    /**
     * 异步执行命令
     *
     * @param executable
     * @param args
     * @return watchdog，watchdog可杀掉正在执行的进程
     * @throws IOException
     */
    public static ExecuteWatchdog executeAsyncAndGetWatchdog(String executable, List<String> args, PumpStreamHandler pumpStreamHandler) throws IOException {
        ExecuteWatchdog watchdog = new ExecuteWatchdog(Integer.MAX_VALUE);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        if (pumpStreamHandler != null) {
            executor.setStreamHandler(pumpStreamHandler);
        }
        executor.execute(createCommandLine(executable, args), new DefaultExecuteResultHandler());
        return watchdog;
    }

    private static CommandLine createCommandLine(String executable, List<String> args) {
        CommandLine commandLine = new CommandLine(executable);
        if (args != null) {
            args.forEach(arg -> commandLine.addArgument(arg));
        }
        return commandLine;
    }
}