/*
 * YADI (Yet Another DLMS Implementation)
 * Copyright (C) 2018 Paulo Faco (paulofaco@gmail.com)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package yadi.java.client;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import yadi.java.client.DlmsException.DlmsExceptionReason;

public class DlmsParser {
	
	/**
	 * Retrieves the DlmsType from an array of bytes
	 * @param data array of bytes containing data result from a dlms-get
	 * @return The DlmsType of the data
	 * @throws DlmsException
	 */
	public static DlmsType getTypeFromRawBytes(byte[] data) throws DlmsException {
		verify(data);
		return DlmsType.fromTag(data[0]);
	}

	/**
	 * Retrieves the String representation of the raw bytes.
	 * The string will include the type and the size of the element parsed.
	 * @param data array of bytes containing data result from a dlms-get
	 * @return A String that represents the data
	 * @throws DlmsException
	 */
	public static DlmsItem getDlmsItem(byte[] data) throws DlmsException {
		verify(data);
		DlmsType type = DlmsType.fromTag(data[0]);
		DlmsItem item = new DlmsItem(type, getString(data));
		int numberOfItems = getNumberOfItems(type,data);
		for (int i = 0; i < numberOfItems; ++i) {
			data = getNextData(data);
			parseItems(item, data);
		}
		return item;
	}

	private static void parseItems(DlmsItem parent, byte[] data) throws DlmsException {
		if (data.length == 0) {
			return;
		}
		DlmsType type = DlmsType.fromTag(data[0]);
		DlmsItem item = new DlmsItem(type, getString(data));
		parent.addChildren(item);
		int numberOfItems = getNumberOfItems(type,data);
		for (int i = 0; i < numberOfItems; ++i) {
			data = getNextData(data);
			parseItems(item, data);
		}
	}
	
	private static byte[] getNextData(byte[] data) throws DlmsException {
		if (data == null || data.length == 0) {
			return new byte[0];
		}
		DlmsType type = DlmsType.fromTag(data[0]);
		if (getNumberOfItems(type,data) == 0) {
			int offset = type.size == 0 ? getSize(data) + getOffset(data) : type.size + 1;
			return Arrays.copyOfRange(data, offset, data.length);
		}
		int offset = getOffset(data);
		return Arrays.copyOfRange(data, offset, data.length);
	}

	/**
	 * Retrieves the String representation of the element in the array of bytes
	 * @param data array of bytes containing data result from a dlms-get
	 * @return A Strig that represents the element in the data
	 * @throws DlmsException
	 */
	public static String getString(byte[] data) throws DlmsException {
		verify(data);
		DlmsType type = DlmsType.fromTag(data[0]);
		return DlmsParser.getStringValue(type, getPayload(type, data));
	}
	
	/**
	 * Retrieves the String representation of the element in the array of bytes
	 * @param type the DlmsType of the byte array
	 * @param data array of bytes containing data result from a dlms-get
	 * @return A Strig that represents the element in the data
	 * @throws DlmsException
	 */
	public static String getString(DlmsType type, byte[] payload) throws DlmsException {
		return DlmsParser.getStringValue(type, payload);
	}
	
	/**
	 * Retrieves the DateTime String representation of the element in the array of bytes
	 * @param data array of bytes containing data result from a dlms-get
	 * @return A Strig that represents the date and time in the data
	 * @throws DlmsException
	 */
	public static String getDateTimeString(byte[] data) throws DlmsException {
		verify(data);
		DlmsType type = DlmsType.fromTag(data[0]);
		return DlmsParser.getDateTimeStringValue(getPayload(type, data));
	}
	
	/**
	 * Retrieves the Date String representation of the element in the array of bytes
	 * @param data array of bytes containing data result from a dlms-get
	 * @return A Strig that represents the date in the data
	 * @throws DlmsException
	 */
	public static String getDateString(byte[] data) throws DlmsException {
		verify(data);
		DlmsType type = DlmsType.fromTag(data[0]);
		return DlmsParser.getDateStringValue(getPayload(type, data));
	}
	
	/**
	 * Retrieves the Time String representation of the element in the array of bytes
	 * @param data array of bytes containing data result from a dlms-get
	 * @return A Strig that represents the time in the data
	 * @throws DlmsException
	 */
	public static String getTimeString(byte[] data) throws DlmsException {
		verify(data);
		DlmsType type = DlmsType.fromTag(data[0]);
		return DlmsParser.getTimeStringValue(getPayload(type, data));
	}
	
	private static int getNumberOfItems(DlmsType type, byte[] data) {
		if (type.equals(DlmsType.ARRAY) ||type.equals(DlmsType.STRUCTURE)) {
			return getSize(data);
		}
		return 0;
	}

	private static void verify(byte[] data) throws DlmsException {
		if (data == null || data.length < 2) {
			throw new DlmsException(DlmsExceptionReason.INVALID_DATA);
		}
	}
	
	private static byte[] getPayload(DlmsType type, byte[] data) {
		int offset = type.size == 0 ? getOffset(data) : 1;
		int size = type.size == 0 ? getSize(data) : type.size;
		return Arrays.copyOfRange(data, offset, offset+size);
	}

	private static String getStringValue(DlmsType type, byte[] payload) throws DlmsException {
		switch (type) {
		case ARRAY:
			return bytesToHex(payload);
		case BCD:
			return bytesToHex(payload);
		case BITSTRING:
			return bytesToHex(payload);
		case BOOLEAN:
			return bytesToHex(payload);
		case DATE:
			return getDateStringValue(payload);
		case DATE_TIME:
			return getDateTimeStringValue(payload);
		case ENUM:
			return bytesToHex(payload);
		case FLOAT32:
			return Float.toString(ByteBuffer.wrap(payload).getFloat());
		case FLOAT64:
			return Double.toString(ByteBuffer.wrap(payload).getDouble());
		case INT16:
			return Integer.toString(ByteBuffer.wrap(payload).getShort());
		case INT32:
			return Integer.toString(ByteBuffer.wrap(payload).getInt());
		case INT64:
			return Long.toString(ByteBuffer.wrap(payload).getLong());
		case INT8:
			return Integer.toString(payload[0]);
		case OCTET_STRING:
			return bytesToHex(payload);
		case STRING:
			return new String(payload, Charset.forName("US-ASCII"));
		case STRUCTURE:
			return bytesToHex(payload);
		case TIME:
			return getTimeStringValue(payload);
		case UINT16:
			return Integer.toString(ByteBuffer.wrap(payload).getShort() & 0xFFFF);
		case UINT32:
			return Integer.toUnsignedString(ByteBuffer.wrap(payload).getInt());
		case UINT64:
			return Long.toUnsignedString(ByteBuffer.wrap(payload).getLong());
		case UINT8:
			return Integer.toString(payload[0] & 0xFF);
		case UTF8_STRING:
			return new String(payload, Charset.forName("UTF-8"));
		}
		throw new DlmsException(DlmsExceptionReason.NO_SUCH_TYPE);
	}

	private static int getOffset(byte[] data) {
		if ((data[1] & 0xFF) <= 0x80) {
			return 2;
		}
		return (data[1] & 0x0F) + 2;
	}

	private static int getSize(byte[] data) {
		if ((data[1] & 0xFF) <= 0x80) {
			return data[1] & 0xFF;
		}
		if (data[1] == (byte)0x81) {
			return data[2] & 0xFF;
		}
		if (data[1] == (byte)0x82) {
			return ByteBuffer.wrap(data, 2, 2).getShort();
		}
		if (data[1] == (byte)0x83) {
			return ByteBuffer.allocate(4).put((byte)0x00).put(data, 2, 3).getInt(0);
		}
		if (data[1] == (byte)0x84) {
			return ByteBuffer.wrap(data, 2, 4).getInt();
		}
		throw new IllegalArgumentException();
	}
	
	private static String bytesToHex(byte[] data) {
		StringBuilder sb = new StringBuilder();
		for (byte b : data) {
			sb.append(String.format("%02X ", b));
		}
		return sb.toString();
	}
	
	private static String getTimeStringValue(byte[] bytes) {
		if (bytes.length < 4) {
			throw new IllegalArgumentException();
		}
		String hour = getDateValue(bytes[0], "HH");
		String min = getDateValue(bytes[1], "mm");
		String sec = getDateValue(bytes[2], "SS");
		return hour+":"+min+":"+sec;
	}
	
	private static String getDateStringValue(byte[] bytes) {
		if (bytes.length < 5) {
			throw new IllegalArgumentException();
		}
		String year = getYear(bytes);
		String month = getDateValue(bytes[2], "MM");
		String day = getDateValue(bytes[3], "DD");
		return year+"/"+month+"/"+day;
	}
	
	private static String getDateTimeStringValue(byte[] bytes) {
		if (bytes.length < 8) {
			throw new IllegalArgumentException();
		}
		String year = getYear(bytes);
		String month = getDateValue(bytes[2], "MM");
		String day = getDateValue(bytes[3], "DD");
		String hour = getDateValue(bytes[5], "HH");
		String min = getDateValue(bytes[6], "mm");
		String sec = getDateValue(bytes[7], "SS");
		return year+"/"+month+"/"+day+" "+hour+":"+min+":"+sec;
	}

	private static String getYear(byte[] bytes) {
		if(bytes[0] == (byte)0xFF && bytes[1] == (byte)0xFF) {
			return "YY";
		}
		return String.format("%04d", ByteBuffer.allocate(2).put(bytes,0,2).getShort(0));
	}
	
	private static String getDateValue(byte val, String replacement ) {
		if(val == (byte)0xFF) {
			return replacement;
		}
		return String.format("%02d", val & 0xFF);
	}

}