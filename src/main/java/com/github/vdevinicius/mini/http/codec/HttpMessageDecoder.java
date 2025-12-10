package com.github.vdevinicius.mini.http.codec;

import com.github.vdevinicius.mini.http.core.HttpRequest;
import com.github.vdevinicius.mini.http.core.MiniHttpRequest;
import com.github.vdevinicius.mini.http.exception.MalformedHttpMessageException;

// TODO: Remove wildcard imports
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;

// TODO: Refactor decoder class with a ByteBuffer approach instead of a sequential InputStream
public final class HttpMessageDecoder {
    private static final int MAX_BODY_SIZE = 32 * 1024;

    private final ReadableByteChannel channel;
    private final ByteBuffer byteBuf;

    private byte matchCount = 0;
    private byte lastReadByte = 0;
    private int headerBytesReadAcc = 0;

    public HttpMessageDecoder(InputStream in, int bufferSize) {
        this.channel = Channels.newChannel(in);
        this.byteBuf = ByteBuffer.allocate(bufferSize);
    }

    public HttpRequest read() throws IOException {
        final var builder = MiniHttpRequest.newBuilder();
        final var tuple = readHeaders(builder);
        final var bodyBytes = readBody(tuple[1], tuple[0]);
        return builder.body(bodyBytes).build();
    }

    private int[] readHeaders(MiniHttpRequest.Builder builder) throws IOException {
        var doubleCRLFIndex = -1;
        while (doubleCRLFIndex == -1) {
            final var n = this.channel.read(this.byteBuf);
            if (n == -1) {
                throw new EOFException("EOF found before the end of headers (\r\n\r\n)");
            }

            this.byteBuf.flip();
            final var limit = this.byteBuf.limit();
            for (var i = 0; i <= limit - 4; i++) {
                if (byteBuf.get(i) == 13 && byteBuf.get(i + 1) == 10 && byteBuf.get(i + 2) == 13 && byteBuf.get(i + 3) == 10) {
                    doubleCRLFIndex = i + 4;
                }
            }
        }

        final var headerBuf = this.byteBuf.slice();
        headerBuf.limit(doubleCRLFIndex);
        final var reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(headerBuf.array())));
        final var requestLine = reader.readLine().split(" ");
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
            } else {
                headers.put("content-length", "0");
            }
        } catch (NumberFormatException e) {
            throw new IllegalStateException("could not cast content-length header");
        }

        if (contentLength < 0) {
            throw new IllegalStateException("negative content length is not allowed");
        }

        builder
                .method(requestLine[0])
                .path(requestLine[1])
                .version(requestLine[requestLine.length-1].split("/")[1])
                .headers(headers);

        return new int[]{contentLength, doubleCRLFIndex};
    }

    private byte[] readBody(int doubleCRLFIndex, int contentLength) throws IOException {
        if (contentLength <= 0) {
            return null;
        }

        if (contentLength >= MAX_BODY_SIZE) {
            throw new MalformedHttpMessageException("Body size exceeded the maximum permitted size of 32KiB");
        }

        this.byteBuf.position(doubleCRLFIndex);
        final var bytesRead = this.byteBuf.limit() - this.byteBuf.position();
        var remaining = contentLength - bytesRead;
        while (remaining > 0) {
            int readBytes = this.channel.read(this.byteBuf);
            remaining -= readBytes;
        }

        return this.byteBuf.slice().array();
    }
}
