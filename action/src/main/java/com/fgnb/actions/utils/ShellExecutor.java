package com.fgnb.actions.utils;

import org.apache.commons.exec.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by jiangyitao.
 */
public class ShellExecutor {


    /**
     * 执行命令
     *
     * @param cmd
     * @throws IOException
     */
    public static void exec(String cmd) throws IOException {
        new DefaultExecutor().execute(CommandLine.parse(cmd));
    }

    /**
     * 执行命令返回执行结果
     *
     * @param cmd
     * @return
     * @throws IOException
     */
    public static String execReturnResult(String cmd) throws IOException {

        CommandLine commandLine = CommandLine.parse(cmd);
        DefaultExecutor executor = new DefaultExecutor();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(outputStream, errorStream);

        executor.setStreamHandler(pumpStreamHandler);
        try {
            executor.execute(commandLine);
            String resp = outputStream.toString() + errorStream.toString();
            return resp;
        } finally {
            outputStream.close();
            errorStream.close();
        }

    }
}
