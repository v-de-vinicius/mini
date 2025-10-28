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

    private byte matchCount = 0;
    private byte lastReadByte = 0;
    private int bytesReadAcc = 0;

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
        var bodyStartIndex = -1;
        while (bodyStartIndex == -1) {
            final var n = this.in.read(this.buf);
            if (n == -1) {
                throw new EOFException("EOF found before the end of headers");
            }

            this.acc.write(this.buf, 0, n);

            if (this.acc.size() > MAX_HEADER_SIZE) {
                throw new InvalidHttpMessageException("Header size exceeded the maximum permitted size of 32KiB", HttpResponseStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
            }

            bodyStartIndex = indexOfCRLFCRLF(n);
        }
    }

    private void readBody() throws IOException {
        var n = this.in.read(this.buf);
        while (n > 0) {
            this.acc.write(this.buf, 0, n);

            if (this.acc.size() > MAX_BODY_SIZE) {
                throw new InvalidHttpMessageException("Body size exceeded the maximum permitted size of 32KiB", HttpResponseStatus.PAYLOAD_TOO_LARGE);
            }

            n = this.in.read(this.buf);
        }
    }

    private int indexOfCRLFCRLF(int bytesRead) {
        if (bytesRead <= 0) {
            return -1;
        }

        for (byte currByte : this.buf) {
            if (matchCount == 4) {
                return bytesReadAcc;
            }

            bytesReadAcc += 1;
            // If the current byte isn't \r nor \n, proceed to the next iteration.
            if (currByte != 13 && currByte != 10) {
                // Reset match count to find \r\n\r\n in another position.
                matchCount = 0;
                lastReadByte = 0;
                continue;
            }

            if (currByte == 10 && lastReadByte == 13) {
                matchCount += 1;
                continue;
            }

            lastReadByte = currByte;
            matchCount += 1;
        }

        return -1;
    }
}
