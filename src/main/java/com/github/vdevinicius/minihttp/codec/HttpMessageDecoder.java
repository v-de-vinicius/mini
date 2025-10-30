package com.github.vdevinicius.minihttp.codec;

import com.github.vdevinicius.minihttp.HttpMethod;
import com.github.vdevinicius.minihttp.HttpRequest;
import com.github.vdevinicius.minihttp.HttpResponseStatus;
import com.github.vdevinicius.minihttp.InvalidHttpMessageException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class HttpMessageDecoder {
    private static final int MAX_HEADER_SIZE = 32 * 1024;
    private static final int MAX_BODY_SIZE = 32 * 1024;

    private final byte[] buf;
    private final InputStream in;
    private final ByteArrayOutputStream acc;

    private byte matchCount = 0;
    private byte lastReadByte = 0;
    private int headerBytesReadAcc = 0;

    public HttpMessageDecoder(InputStream in, int bufferSize) {
        this.buf = new byte[bufferSize];
        this.in = in;
        this.acc = new ByteArrayOutputStream();
    }

    public HttpRequest read() throws IOException {
        readHeaders();
        final var headerBytes = new ByteArrayInputStream(acc.toByteArray(), 0, headerBytesReadAcc);
        final var headerReader = new BufferedReader(new InputStreamReader(headerBytes, StandardCharsets.US_ASCII));
        final var requestLine = headerReader.readLine();
        final var method = requestLine.split(" ")[0];
        final var headers = new HashMap<String, String>();
        var header = headerReader.readLine();
        while (!header.isEmpty()) {
            final var headerFragments = header.split(":", 2);
            final var key = headerFragments[0].trim().toLowerCase();
            final var value = headerFragments.length > 1 ? headerFragments[1].trim() : "";
            // Set-Cookie header must be treated separately according to RFC 6265 - So we're supporting it as a single value header.
            if (headers.containsKey(key) && !key.equals("set-cookie")) {
                headers.put(key, headers.get(key) + "," + value);
            } else {
                headers.put(key, value);
            }
            header = headerReader.readLine();
        }
        readBody();
        return new HttpRequest() {
            @Override
            public HttpMethod method() {
                return HttpMethod.valueOf(method);
            }

            @Override
            public Map<String, String> headers() {
                return headers;
            }
        };
    }

    public int getBodyStartIndex() {
        return headerBytesReadAcc;
    }

    private void readHeaders() throws IOException {
        var bodyStartIndex = -1;
        while (bodyStartIndex == -1) {
            final var n = this.in.read(this.buf);
            if (n == -1) {
                throw new EOFException("EOF found before the end of headers (\r\n\r\n)");
            }

            this.acc.write(this.buf, 0, n);

            if (this.acc.size() > MAX_HEADER_SIZE) {
                throw new InvalidHttpMessageException("Header size exceeded the maximum permitted size of 32KiB", HttpResponseStatus.REQUEST_HEADER_FIELDS_TOO_LARGE);
            }

            bodyStartIndex = indexOfCRLFCRLF(n);
        }
    }

    private void readBody() throws IOException {
        final var remaining = acc.size() - headerBytesReadAcc;
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
                return headerBytesReadAcc;
            }

            headerBytesReadAcc += 1;
            // If the current byte isn't \r nor \n, proceed to the next iteration.
            if (currByte != 13 && currByte != 10) {
                matchCount = 0;
                lastReadByte = 0;
                continue;
            }

            if (lastReadByte == 13 && currByte != 10) {
                matchCount = 0;
                continue;
            }

            lastReadByte = currByte;
            matchCount += 1;
        }

        return -1;
    }
}
