package com.esq.rbac.web.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author fazia
 * Reference: http://tutorials.jenkov.com/java-servlets/gzip-servlet-filter.html
 * The GZipServletOutputStream is what compresses the content written to it. 
 * It does so by using a GZIPOutputStream internally, which is a standard Java class.
 */
public class GZipServletOutputStream extends ServletOutputStream {
	private GZIPOutputStream gzipOutputStream = null;

	public GZipServletOutputStream(OutputStream output) throws IOException {
		super();
		this.gzipOutputStream = new GZIPOutputStream(output);
	}

	@Override
	public void close() throws IOException {
		this.gzipOutputStream.close();
	}

	@Override
	public void flush() throws IOException {
		this.gzipOutputStream.flush();
	}

	@Override
	public void write(byte b[]) throws IOException {
		this.gzipOutputStream.write(b);
	}

	@Override
	public void write(byte b[], int off, int len) throws IOException {
		this.gzipOutputStream.write(b, off, len);
	}

	@Override
	public void write(int b) throws IOException {
		this.gzipOutputStream.write(b);
	}

	@Override
	public boolean isReady() {
		return false;
	}

	@Override
	public void setWriteListener(WriteListener writeListener) {

	}
}
