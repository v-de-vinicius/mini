package com.github.vdevinicius.minihttp;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class HttpMessageDecoder {
    private final InputStream inputStream;
    private final byte[] buffer;

    private Map<String, String> headers;

    public HttpMessageDecoder(final InputStream inputStream, final int bufferSize) {
        this.inputStream = inputStream;
        this.buffer = new byte[bufferSize];
    }

    public Map<String, String> getHeaders() throws Throwable {
       if (headers != null)  {
           return headers;
       }

       var endIndex = -1;
       final var acc = new ByteArrayOutputStream();
       while (endIndex == -1) {
           final var readBytes = inputStream.read(buffer);
           if (readBytes == -1) {
               throw new IllegalStateException("EOF found before end of headers");
           }
           acc.write(buffer, 0, readBytes);
           if (acc.size() > 32768) {
               throw new IllegalStateException("Header size greater than 32KiB");
           }
           endIndex = indexOfCRLFCRLF(acc.toByteArray(), acc.size());
       }

       inputStream.reset();
       inputStream.readNBytes(endIndex);

       final var accBytes = acc.toByteArray();
       final var headerBytes = new ByteArrayInputStream(accBytes, 0, endIndex);
       final var reader = new BufferedReader(new InputStreamReader(headerBytes));
       final var headers = new HashMap<String, String>();
       reader.readLine();
       var header = reader.readLine();
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
           header = reader.readLine();
       }

       return this.headers = headers;
    }

    private static int indexOfCRLFCRLF(final byte[] content, final int len) {
        if (len < 4) return -1;
        for (int i = 0; i <= len - 4; i++) {
            if (content[i] == 13 && content[i+1] == 10 && content[i+2] == 13 && content[i+3] == 10) {
                return i + 4;
            }
        }
        return -1;
    }
}
