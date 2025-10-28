package com.github.vdevinicius.minihttp;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class HttpMessageReader {
    private static final int MAX_HEADER_SIZE = 32 * 1024;
    private static final int MAX_BODY_SIZE = 32 * 1024;

    private final byte[] buf;
    private final InputStream in;
    private final ByteArrayOutputStream acc;

    private int bodyStartIndex = -1;

    public HttpMessageReader(InputStream in, int bufferSize) {
        this.buf = new byte[bufferSize];
        this.in = in;
        this.acc = new ByteArrayOutputStream();
    }

    public byte[] read() throws IOException {
        readHeaders();
        readBody();
        return this.acc.toByteArray();
    }

    private void readHeaders() throws IOException {
        while (bodyStartIndex == -1) {
            final var n = this.in.read(this.buf);
            if (n == -1) {
                throw new EOFException("EOF found before the end of headers");
            }

            this.acc.write(this.buf, 0, n);

            if (this.acc.size() > MAX_HEADER_SIZE) {
                throw new InvalidHttpMessageException("Header size exceeded the maximum permitted size of 32KiB", HttpResponseStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
            }

            bodyStartIndex = indexOfCRLFCRLF();
        }
    }

    private void readBody() throws IOException {
        var content = this.in.read(this.buf);
        while (content > 0) {
            this.acc.write(this.buf, 0, content);

            if (this.acc.size() > MAX_BODY_SIZE) {
                throw new InvalidHttpMessageException("Body size exceeded the maximum permitted size of 32KiB", HttpResponseStatus.PAYLOAD_TOO_LARGE);
            }

            content = in.read(this.buf);
        }
    }

    private int indexOfCRLFCRLF() {
        final var n = acc.toByteArray();
        final var len = acc.size();
        if (len < 4) return -1;

        for (int i = 0; i <= len - 4; i++) {
            if (n[i] == 13 && n[i + 1] == 10 && n[i + 2] == 13 && n[i + 3] == 10) {
                return i + 4;
            }
        }

        return -1;
    }
}
