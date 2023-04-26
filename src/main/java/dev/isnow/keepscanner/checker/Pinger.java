package dev.isnow.keepscanner.checker;


import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

class Pinger
{
    private InetSocketAddress host;
    private int timeout;
    
    void setAddress(final InetSocketAddress host) {
        this.host = host;
    }
    
    void setTimeout(final int timeout) {
        this.timeout = timeout;
    }
    
    private int readVarInt(final DataInputStream in) throws IOException {
        int i = 0;
        int j = 0;
        int k;
        do {
            k = in.readByte();
            i |= (k & 0x7F) << j++ * 7;
            if (j > 5) {
                return -1;
            }
        } while ((k & 0x80) == 0x80);
        return i;
    }
    
    private void writeVarInt(final DataOutputStream out, int paramInt) throws IOException {
        while ((paramInt & 0xFFFFFF80) != 0x0) {
            out.writeByte((paramInt & 0x7F) | 0x80);
            paramInt >>>= 7;
        }
        out.writeByte(paramInt);
    }
    
    public String fetchData() throws IOException {
        final Socket socket = new Socket();
        socket.setSoTimeout(this.timeout);
        socket.connect(this.host, this.timeout);
        final OutputStream outputStream = socket.getOutputStream();
        final DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        final InputStream inputStream = socket.getInputStream();
        final ByteArrayOutputStream b = new ByteArrayOutputStream();
        final DataOutputStream handshake = new DataOutputStream(b);
        handshake.writeByte(0);
        this.writeVarInt(handshake, 4);
        this.writeVarInt(handshake, this.host.getHostString().length());
        handshake.writeBytes(this.host.getHostString());
        handshake.writeShort(this.host.getPort());
        this.writeVarInt(handshake, 1);
        this.writeVarInt(dataOutputStream, b.size());
        dataOutputStream.write(b.toByteArray());
        dataOutputStream.writeByte(1);
        dataOutputStream.writeByte(0);
        final DataInputStream dataInputStream = new DataInputStream(inputStream);
        final int size = this.readVarInt(dataInputStream);
        final int id = this.readVarInt(dataInputStream);
        final int length = this.readVarInt(dataInputStream);
        if (size < 0 || id < 0 || length <= 0) {
            this.closeAll(b, dataInputStream, handshake, dataOutputStream, outputStream, inputStream, socket);
            return null;
        }
        final byte[] in = new byte[length];
        dataInputStream.readFully(in);
        this.closeAll(b, dataInputStream, handshake, dataOutputStream, outputStream, inputStream, socket);
        return new String(in);
    }
    
    public void closeAll(final Closeable... closeables) throws IOException {
        for (final Closeable closeable : closeables) {
            closeable.close();
        }
    }
}
