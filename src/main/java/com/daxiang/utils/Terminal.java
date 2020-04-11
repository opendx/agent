package com.daxiang.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.exec.*;
import org.springframework.util.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class Terminal {

    public static final boolean IS_WINDOWS = OS.isFamilyWindows();
    private static final String BASH = "/bin/sh";
    private static final String CMD_EXE = "cmd.exe";

    /**
     * 同步执行命令
     *
     * @param command
     * @return
     * @throws IOException
     */
    public static String execute(String command) throws IOException {
        return execute(command, true);
    }

    public static String execute(String command, boolean showLog) throws IOException {
        Executor executor = new DaemonExecutor();
        executor.setExitValues(null);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ByteArrayOutputStream errorStream = new ByteArrayOutputStream()) {

            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);
            executor.setStreamHandler(pumpStreamHandler);

            if (showLog) {
                log.info("[==>]{}", command);
            }
            executor.execute(createCommandLine(command));

            String result = outputStream.toString() + errorStream.toString();
            if (showLog) {
                log.info("[<==]{}", result);
            }

            if (!StringUtils.isEmpty(result)) {
                if (result.endsWith("\r\n")) {
                    result = result.substring(0, result.length() - 2);
                } else if (result.endsWith("\n")) {
                    result = result.substring(0, result.length() - 1);
                }
            }

            return result;
        }
    }

    /**
     * 异步执行命令
     *
     * @param command
     * @return watchdog，watchdog可杀掉正在执行的进程
     * @throws IOException
     */
    public static ExecuteWatchdog executeAsyncAndGetWatchdog(String command) throws IOException {
        return executeAsyncAndGetWatchdog(command, true);
    }

    public static void executeAsync(String command, PumpStreamHandler pumpStreamHandler) throws IOException {
        Executor executor = new DaemonExecutor();
        executor.setExitValues(null);
        executor.setStreamHandler(pumpStreamHandler);

        executor.execute(createCommandLine(command), new DefaultExecuteResultHandler());
    }

    public static ExecuteWatchdog executeAsyncAndGetWatchdog(String command, boolean showLog) throws IOException {
        Executor executor = new DaemonExecutor();
        executor.setExitValues(null);

        ExecuteWatchdog watchdog = new ExecuteWatchdog(ExecuteWatchdog.INFINITE_TIMEOUT);
        executor.setWatchdog(watchdog);

        if (showLog) {
            log.info("[==>]{}", command);
            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(new LogOutputStream() {
                @Override
                protected void processLine(String line, int i) {
                    log.info("[<==]{}", line);
                }
            });
            executor.setStreamHandler(pumpStreamHandler);
        }

        executor.execute(createCommandLine(command), new DefaultExecuteResultHandler());
        return watchdog;
    }

    private static CommandLine createCommandLine(String command) {
        if (command == null) {
            throw new IllegalArgumentException("command can not be null!");
        }

        CommandLine commandLine;
        if (IS_WINDOWS) {
            commandLine = new CommandLine(CMD_EXE);
            commandLine.addArgument("/C");
        } else {
            commandLine = new CommandLine(BASH);
            commandLine.addArgument("-c");
        }

        commandLine.addArgument(command, false);
        return commandLine;
    }
}