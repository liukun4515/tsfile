package cn.edu.tsinghua.tsfile.timeseries.write.series;

import cn.edu.tsinghua.tsfile.common.utils.Binary;
import cn.edu.tsinghua.tsfile.common.utils.PublicBAOS;
import cn.edu.tsinghua.tsfile.encoding.encoder.Encoder;
import cn.edu.tsinghua.tsfile.file.utils.ReadWriteToBytesUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;

/**
 * This function is used to write time-value into a time series. It consists of a time encoder, a
 * value encoder and respective OutputStream.
 *
 * @author kangrong
 */
public class ValueWriter {
    // time
    private Encoder timeEncoder;
    private PublicBAOS timeOut;
    // value
    private Encoder valueEncoder;
    private PublicBAOS valueOut;

    //private PublicBAOS timeSizeOut;
    private int timeSize;

    public ValueWriter() {
        this.timeOut = new PublicBAOS();
        this.valueOut = new PublicBAOS();
       // this.timeSizeOut = new PublicBAOS();
    }

    public void write(long time, boolean value) throws IOException {
        timeEncoder.encode(time, timeOut);
        valueEncoder.encode(value, valueOut);
    }

    public void write(long time, short value) throws IOException {
        timeEncoder.encode(time, timeOut);
        valueEncoder.encode(value, valueOut);
    }

    public void write(long time, int value) throws IOException {
        timeEncoder.encode(time, timeOut);
        valueEncoder.encode(value, valueOut);
    }

    public void write(long time, long value) throws IOException {
        timeEncoder.encode(time, timeOut);
        valueEncoder.encode(value, valueOut);
    }

    public void write(long time, float value) throws IOException {
        timeEncoder.encode(time, timeOut);
        valueEncoder.encode(value, valueOut);
    }

    public void write(long time, double value) throws IOException {
        timeEncoder.encode(time, timeOut);
        valueEncoder.encode(value, valueOut);
    }

    public void write(long time, BigDecimal value) throws IOException {
        timeEncoder.encode(time, timeOut);
        valueEncoder.encode(value, valueOut);
    }

    public void write(long time, Binary value) throws IOException {
        timeEncoder.encode(time, timeOut);
        valueEncoder.encode(value, valueOut);
    }

    /**
     * flush all data remained in encoders.
     *
     * @throws IOException
     */
    private void prepareEndWriteOnePage() throws IOException {
        timeEncoder.flush(timeOut);
        valueEncoder.flush(valueOut);
        timeOut.flush();
        valueOut.flush();
    }

//    /**
//     * getBytes return data what it has been written in form of <code>ListByteArrayOutputStream</code>.
//     *
//     * @return - list byte array output stream containing time size, time stream and value stream.
//     * @throws IOException exception in IO
//     */
//    public ListByteArrayOutputStream getBytes() throws IOException {
//        prepareEndWriteOnePage();
//        ReadWriteStreamUtils.writeUnsignedVarInt(timeOut.size(), timeSizeOut);
//        return new ListByteArrayOutputStream(timeSizeOut, timeOut, valueOut);
//    }

    /**
     * getBytes return data what it has been written in form of <code>size of time list, time list, value list</code>
     * @return a new readable Bytebuffer whose position is 0.
     * @throws IOException
     * author hxd
     */
    public ByteBuffer getBytes() throws IOException {
        prepareEndWriteOnePage();
        ByteBuffer buffer= ByteBuffer.allocate(timeOut.size()+valueOut.size()+32);
        int length1=ReadWriteToBytesUtils.writeUnsignedVarInt(timeOut.size(),buffer);//FIXME: why do we use a var-length int.
        buffer.put(timeOut.getBuf(),0, timeOut.size());
        buffer.put(valueOut.getBuf(),0, valueOut.size());
        buffer.flip();
        return buffer;
        //return new ListByteArrayOutputStream(timeSizeOut, timeOut, valueOut);
    }


    /**
     * calculate max possible memory size it occupies, including time outputStream and value outputStream
     *
     * @return allocated size in time, value and outputStream
     */
    public long estimateMaxMemSize() {
        return timeOut.size() + valueOut.size() + timeEncoder.getMaxByteSize() + valueEncoder.getMaxByteSize();
    }

    /**
     * reset data in ByteArrayOutputStream
     */
    public void reset() {
        timeOut.reset();
        valueOut.reset();
    }

    public void setTimeEncoder(Encoder encoder) {
        this.timeEncoder = encoder;
    }

    public void setValueEncoder(Encoder encoder) {
        this.valueEncoder = encoder;
    }

}
