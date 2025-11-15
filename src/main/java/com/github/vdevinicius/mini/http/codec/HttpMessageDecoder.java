package com.github.vdevinicius.mini.http.codec;

import com.github.vdevinicius.mini.http.core.HttpRequest;
import com.github.vdevinicius.mini.http.exception.MalformedHttpMessageException;

// TODO: Remove wildcard imports
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

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
        final var builder = HttpRequest.newBuilder();
        readHeaders();
        final var headerBytes = acc.toByteArray();
        final var reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(headerBytes), StandardCharsets.US_ASCII));
        final var requestLine = reader.readLine().split(" ");
        builder.uri(requestLine[1])
                .version(requestLine[requestLine.length-1].split("/")[1]);
        final var headers = new HashMap<String, String>();
        var header = reader.readLine();
        while (!header.isEmpty()) {
            final var headerFragments = header.split(":", 2);
            final var key = headerFragments[0].trim().toLowerCase();
            final var value = headerFragments.length > 1 ? headerFragments[1].trim() : "";
            headers.put(key, value);
            header = reader.readLine();
        }

        if (headers.containsKey("transfer-encoding")) {
            throw new UnsupportedOperationException("transfer encoding strategies are not implemented");
        }

        if ("100-continue".equals(headers.get("expect"))) {
            throw new UnsupportedOperationException("expect header not implemented");
        }

        if (!headers.containsKey("host")) {
            throw new IllegalStateException("host header not present");
        }

        var contentLength = 0;
        try {
            if (headers.containsKey("content-length")) {
                contentLength = Integer.parseInt(headers.get("content-length"));
            }
        } catch (NumberFormatException e) {
            throw new IllegalStateException("could not cast content-length header");
        }

        if (contentLength < 0) {
            throw new IllegalStateException("negative content length is not allowed");
        }

        builder.headers(headers);

        readBody(contentLength);
        final var bodyBytes = new ByteArrayOutputStream();
        bodyBytes.write(acc.toByteArray(), headerBytesReadAcc, contentLength);
        builder.method(requestLine[0]);
        builder.body(bodyBytes.toByteArray());
        return builder.build();
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
                throw new MalformedHttpMessageException("Header size exceeded the maximum permitted size of 32KiB");
            }

            bodyStartIndex = indexOfCRLFCRLF(n);
        }
    }

    private void readBody(int contentLength) throws IOException {
        if (contentLength <= 0) {
            return;
        }

        if (contentLength >= MAX_BODY_SIZE) {
            throw new MalformedHttpMessageException("Body size exceeded the maximum permitted size of 32KiB");
        }

        final var already = acc.size() - headerBytesReadAcc;
        var remaining = contentLength - already;
        while (remaining > 0) {
            final var n = this.in.read(this.buf, 0, Math.min(this.buf.length, remaining));
            this.acc.write(this.buf, 0, n);

            if (this.acc.size() >= MAX_BODY_SIZE) {
                throw new MalformedHttpMessageException("Body size exceeded the maximum permitted size of 32KiB");
            }

            remaining -= n;
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
