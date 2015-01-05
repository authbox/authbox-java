package io.authbox.api;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

class AuthboxHttpServletResponseWriter extends Writer {
    private CharArrayWriter buffer;
    private Writer underlyingWriter;
    private AuthboxHttpServletResponseWrapper wrapper;

    public AuthboxHttpServletResponseWriter(CharArrayWriter buffer, Writer underlyingWriter, AuthboxHttpServletResponseWrapper wrapper) {
        super();
        this.buffer = buffer;
        this.underlyingWriter = underlyingWriter;
        this.wrapper = wrapper;
    }

    public void close() throws IOException {
        if (wrapper.isBuffering()) {
            buffer.close();
        }
        underlyingWriter.close();
    }

    public void flush() throws IOException {
        // Since we don't do this very often it's probably ok.
        if (!wrapper.isBuffering()) {
            underlyingWriter.flush();
        }
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        if (wrapper.isBuffering()) {
            buffer.write(cbuf, off, len);
        } else {
            underlyingWriter.write(cbuf, off, len);
        }
    }
}

public class AuthboxHttpServletResponseWrapper extends HttpServletResponseWrapper {
    private CharArrayWriter buffer;
    private boolean shouldBuffer;

    public boolean isBuffering() {
        if (!this.shouldBuffer || getContentType() == null) {
            return false;
        }

        return getContentType().startsWith("text/html") || getContentType().startsWith("application/xhtml+xml");
    }

    public String getBufferedResponse(String snippet) throws IOException {
        String bufferedResponse = buffer.toString();
        int bodyCloseTagIndex = bufferedResponse.toLowerCase().indexOf("</body>");

        if (bodyCloseTagIndex == -1) {
            return bufferedResponse;
        }

        CharArrayWriter caw = new CharArrayWriter();
        caw.write(bufferedResponse.substring(0, bodyCloseTagIndex - 1));
        if (snippet != null) {
            caw.write(snippet);
        }
        caw.write(bufferedResponse.substring(bodyCloseTagIndex));
        return caw.toString();
    }

    public AuthboxHttpServletResponseWrapper(HttpServletResponse response, boolean shouldBuffer) {
        super(response);
        buffer = new CharArrayWriter();
        this.shouldBuffer = shouldBuffer;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter(new AuthboxHttpServletResponseWriter(buffer, super.getWriter(), this));
    }
}
