package com.daxiang.utils;

import org.apache.commons.exec.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
public class Terminal {

    public static final boolean IS_WINDOWS = OS.isFamilyWindows();
    private static final String BASH = "/bin/sh";
    private static final String CMD_EXE = "cmd.exe";

    /**
     * 同步执行命令
     *
     * @param args
     * @return
     * @throws IOException
     */
    public static String execute(String... args) throws IOException {
        DefaultExecutor executor = new DefaultExecutor();
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ByteArrayOutputStream errorStream = new ByteArrayOutputStream()) {
            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
            executor.setStreamHandler(pumpStreamHandler);
            executor.execute(createCommandLine(args));
            return outputStream.toString() + errorStream.toString();
        }
    }

    /**
     * 异步执行命令
     *
     * @param args
     * @return watchdog，watchdog可杀掉正在执行的进程
     * @throws IOException
     */
    public static ExecuteWatchdog executeAsyncAndGetWatchdog(PumpStreamHandler pumpStreamHandler, String... args) throws IOException {
        ExecuteWatchdog watchdog = new ExecuteWatchdog(Integer.MAX_VALUE);
        DefaultExecutor executor = new DefaultExecutor();
        executor.setWatchdog(watchdog);
        if (pumpStreamHandler != null) {
            executor.setStreamHandler(pumpStreamHandler);
        }
        executor.execute(createCommandLine(args), new DefaultExecuteResultHandler());
        return watchdog;
    }

    private static CommandLine createCommandLine(String... args) {
        if (args == null) {
            throw new IllegalArgumentException("args can not be null!");
        }

        CommandLine commandLine;
        if (IS_WINDOWS) {
            commandLine = new CommandLine(CMD_EXE);
            commandLine.addArgument("/C");
        } else {
            commandLine = new CommandLine(BASH);
            // todo test
            commandLine.addArgument("-c");
        }

        for (String arg : args) {
            commandLine.addArgument(arg);
        }
        return commandLine;
    }
}