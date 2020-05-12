package com.daxiang.core.mobile.ios;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * Created by jiangyitao.
 */
@Slf4j
public class WdaMjpegInputStream extends DataInputStream {

    /**
     * JPEG
     * - 文件头标识 (2 bytes): 0xff, 0xd8 (SOI) (JPEG 文件标识)
     * - 文件结束标识 (2 bytes): 0xff, 0xd9 (EOI)
     */
    private static final byte[] START_OF_IMAGE = {(byte) 0xFF, (byte) 0xD8};
    private static final byte[] END_OF_IMAGE = {(byte) 0xFF, (byte) 0xD9};

    private static final String CONTENT_LENGTH = "Content-Length".toLowerCase();

    private static final int HEADER_MAX_LENGTH = 100;
    // frame = header + image
    private static final int FRAME_MAX_LENGTH = HEADER_MAX_LENGTH + 100000;

    public WdaMjpegInputStream(InputStream in) {
        super(new BufferedInputStream(in, FRAME_MAX_LENGTH));
    }

    private int getStartOfSequence(DataInputStream in, byte[] sequence) throws IOException {
        int end = getEndOfSeqeunce(in, sequence);
        return end < 0 ? -1 : end - sequence.length;
    }

    private int getEndOfSeqeunce(DataInputStream in, byte[] sequence) throws IOException {
        int s = 0;
        byte c;
        for (int i = 0; i < FRAME_MAX_LENGTH; i++) {
            c = (byte) in.readUnsignedByte();
            if (c == sequence[s]) {
                s++;
                if (s == sequence.length) {
                    return i + 1;
                }
            } else {
                s = 0;
            }
        }
        return -1;
    }

    private int parseContentLength(byte[] headerBytes) throws IOException, NumberFormatException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(headerBytes);
             InputStreamReader isr = new InputStreamReader(bais);
             BufferedReader br = new BufferedReader(isr)) {

            String line;
            while ((line = br.readLine()) != null) {
                if (line.toLowerCase().startsWith(CONTENT_LENGTH)) { // Content-Length: 22517
                    String[] parts = line.split(":");
                    if (parts.length == 2) {
                        return Integer.parseInt(parts[1].trim());
                    }
                }
            }
        }

        return 0;
    }

    /**
     * 从wda mjpeg流中读取图片
     *
     * @return
     * @throws IOException
     */
    public ByteBuffer readImg() throws IOException {
        mark(FRAME_MAX_LENGTH);

        int headerLength = getStartOfSequence(this, START_OF_IMAGE);
        reset();

        byte[] header = new byte[headerLength];
        readFully(header);

        int imgLength;
        try {
            imgLength = parseContentLength(header);
        } catch (NumberFormatException e) {
            imgLength = getEndOfSeqeunce(this, END_OF_IMAGE);
        }

        if (imgLength == 0) {
            log.error("Invalid MJPEG stream, imgLength is 0");
        }

        reset();
        byte[] img = new byte[imgLength];
        skipBytes(headerLength);
        readFully(img);

        return ByteBuffer.wrap(img);
    }

}