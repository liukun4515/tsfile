package cn.edu.tsinghua.tsfile.file.metadata.statistics;

import cn.edu.tsinghua.tsfile.common.utils.ByteBufferUtil;
import cn.edu.tsinghua.tsfile.common.utils.BytesUtils;
import cn.edu.tsinghua.tsfile.file.utils.ReadWriteToBytesUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * @author CGF
 */
public class BooleanStatistics extends Statistics<Boolean> {
	private boolean max;
	private boolean min;
	private boolean first;
	private double sum;
	private boolean last;

	@Override
	public void setMinMaxFromBytes(byte[] minBytes, byte[] maxBytes) {
		max = BytesUtils.bytesToBool(maxBytes);
		min = BytesUtils.bytesToBool(minBytes);
	}

	@Override
	public void updateStats(boolean value) {
		if (isEmpty) {
			initializeStats(value, value, value, 0, value);
			isEmpty = false;
		} else {
			updateStats(value, value, value, 0, value);
			isEmpty = false;
		}
	}

	private void updateStats(boolean minValue, boolean maxValue, boolean firstValue, double sumValue,
			boolean lastValue) {
		if (!minValue && min) {
			min = minValue;
		}
		if (maxValue && !max) {
			max = maxValue;
		}
		this.last = lastValue;
	}

	@Override
	public Boolean getMax() {
		return max;
	}

	@Override
	public Boolean getMin() {
		return min;
	}

	@Override
	public Boolean getFirst() {
		return first;
	}

	@Override
	public double getSum() {
		return sum;
	}
	
	@Override
	public Boolean getLast(){
		return last;
	}

	@Override
	public ByteBuffer getMaxBytebuffer() { return ByteBufferUtil.bytes(max); }

	@Override
	public ByteBuffer getMinBytebuffer() { return ByteBufferUtil.bytes(min); }

	@Override
	public ByteBuffer getFirstBytebuffer() {
		return ByteBufferUtil.bytes(first);
	}

	@Override
	public ByteBuffer getSumBytebuffer() {
		return ByteBufferUtil.bytes(sum);
	}

	@Override
	public ByteBuffer getLastBytebuffer() {
		return ByteBufferUtil.bytes(last);
	}


	@Override
	protected void mergeStatisticsValue(Statistics<?> stats) {
		BooleanStatistics boolStats = (BooleanStatistics) stats;
		if (isEmpty) {
			initializeStats(boolStats.getMin(), boolStats.getMax(), boolStats.getFirst(), boolStats.getSum(),
					boolStats.getLast());
			isEmpty = false;
		} else {
			updateStats(boolStats.getMin(), boolStats.getMax(), boolStats.getFirst(), boolStats.getSum(),
					boolStats.getLast());
		}
	}

	public void initializeStats(boolean min, boolean max, boolean firstValue, double sumValue, boolean lastValue) {
		this.min = min;
		this.max = max;
		this.first = firstValue;
		this.last = lastValue;
	}

	@Override
	public byte[] getMaxBytes() {
		return BytesUtils.boolToBytes(max);
	}

	@Override
	public byte[] getMinBytes() {
		return BytesUtils.boolToBytes(min);
	}

	@Override
	public byte[] getFirstBytes() {
		return BytesUtils.boolToBytes(first);
	}

	@Override
	public byte[] getSumBytes() {
		return BytesUtils.doubleToBytes(sum);
	}

	@Override
	public byte[] getLastBytes() {
		return BytesUtils.boolToBytes(last);
	}


	@Override
	public int sizeOfDatum() {
		return 1;
	}

	@Override
	public String toString() {
		return "[max:" + max + ",min:" + min + ",first:" + first + ",sum:" + sum + ",last:" + last + "]";
	}

	@Override
	void fill(InputStream inputStream) throws IOException {
		this.min = ReadWriteToBytesUtils.readBool(inputStream);
		this.max = ReadWriteToBytesUtils.readBool(inputStream);
		this.first = ReadWriteToBytesUtils.readBool(inputStream);
		this.last = ReadWriteToBytesUtils.readBool(inputStream);
		this.sum = ReadWriteToBytesUtils.readDouble(inputStream);
	}


}
