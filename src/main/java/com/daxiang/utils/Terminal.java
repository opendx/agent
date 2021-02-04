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
    public static final boolean IS_MACOS = OS.isFamilyMac();
    public static final int PLATFORM = IS_WINDOWS ? 1 : IS_MACOS ? 3 : 2;

    private static final String BASH = "/bin/sh";
    private static final String CMD_EXE = "cmd.exe";

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

            int exitValue = executor.execute(createCommandLine(command));
            String result = outputStream.toString() + errorStream.toString();

            if (showLog) log.info("[Terminal]{} -> {} exitValue={}", command, result, exitValue);

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

    public static ShutdownHookProcessDestroyer executeAsync(String command) throws IOException {
        return executeAsync(command, true);
    }

    public static ShutdownHookProcessDestroyer executeAsync(String command, boolean showLog) throws IOException {
        ExecuteStreamHandler executeStreamHandler = null;
        if (showLog) {
            log.info("[Terminal]{}", command);
            executeStreamHandler = new PumpStreamHandler(new LogOutputStream() {
                @Override
                protected void processLine(String line, int i) {
                    log.info("[Terminal]{}", line);
                }
            });
        }
        return executeAsync(command, executeStreamHandler);
    }

    public static ShutdownHookProcessDestroyer executeAsync(String command, ExecuteStreamHandler executeStreamHandler) throws IOException {
        Executor executor = new DaemonExecutor();
        executor.setExitValues(null);

        if (executeStreamHandler != null) {
            executor.setStreamHandler(executeStreamHandler);
        }

        ShutdownHookProcessDestroyer processDestroyer = new ShutdownHookProcessDestroyer();
        executor.setProcessDestroyer(processDestroyer);

        executor.execute(createCommandLine(command), new DefaultExecuteResultHandler());
        return processDestroyer;
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
