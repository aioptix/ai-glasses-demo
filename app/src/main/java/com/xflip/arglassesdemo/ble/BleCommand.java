package com.xflip.arglassesdemo.ble;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class BleCommand {

    private static final byte HEAD = (byte) 0xAE;
    private static byte SERIAL_NUMBER = Byte.MIN_VALUE;

    private static final byte BLE_VER = 0x01;

    public static final String SERVICE_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static final String NOTIFY_CHARACTERISTIC = "0000ffe1-0000-1000-8000-00805f9b34fb";
    public static final String WRITE_CHARACTERISTIC = "0000ffe2-0000-1000-8000-00805f9b34fb";

    private static int CRC16_IBM(byte[] buffer) {
        int wCRCin = 0x0000;
        int wCPoly = 0xa001;
        int bitMax = 8;
        for (byte b : buffer) {
            wCRCin ^= ((int) b & 0x00ff);
            for (int j = 0; j < bitMax; j++) {
                if ((wCRCin & 0x0001) != 0) {
                    wCRCin >>= 1;
                    wCRCin ^= wCPoly;
                } else {
                    wCRCin >>= 1;
                }
            }
        }
        return wCRCin ^= 0x0000;
    }

    /**
     * Splicing final command
     * @param currentSerialNumber  current serial number
     * @param needCrc16Command     cmd+payload
     * @return Splicing final command
     */
    private static byte[] getFinalCommand(byte currentSerialNumber, byte[] needCrc16Command) {
        byte[] headCommand = new byte[6];
        headCommand[0] = HEAD;
        headCommand[1] = currentSerialNumber;
        headCommand[2] = BLE_VER;
        headCommand[3] = (byte) (needCrc16Command.length-1);
        int crc16 = CRC16_IBM(needCrc16Command);
        headCommand[4] = (byte) ((crc16>>8) & 0xFF);
        headCommand[5] = (byte) (crc16 & 0xFF);
        byte[] finalCommand = new byte[6+needCrc16Command.length];
        System.arraycopy(headCommand, 0, finalCommand, 0, headCommand.length);
        System.arraycopy(needCrc16Command, 0, finalCommand, 6, needCrc16Command.length);
        return finalCommand;
    }

    /**
     * Set screen off time
     * @param time 1~65535 sec
     * @return Set screen off time command
     */
    public static byte[] setTurnOffScreenTime(int time) {
        // cmd+payload
        byte[] needCrc16Command = new byte[3];
        needCrc16Command[0] = (byte) 0xD1;
        needCrc16Command[1] = (byte) ((time>>8) & 0xFF);
        needCrc16Command[2] = (byte) (time & 0xFF);

        return getFinalCommand(getCurrentSerialNumber(),needCrc16Command);
    }

    /**
     * volume adjustment
     * @param volumeLevel 0-15 level
     * @return volume adjustment command
     */
    public static byte[] adjustVolume(int volumeLevel) {
        byte[] needCrc16Command = new byte[2];
        needCrc16Command[0] = (byte) 0xD2;
        needCrc16Command[1] = (byte) volumeLevel;
        return getFinalCommand(getCurrentSerialNumber(), needCrc16Command);
    }

    /**
     * Adjust brightness
     * @param brightnessLevel 1~32
     * @return Adjust brightness command
     */
    public static byte[] adjustBrightness(int brightnessLevel) {
        byte[] needCrc16Command = new byte[2];
        needCrc16Command[0] = (byte) 0xD3;
        needCrc16Command[1] = (byte) brightnessLevel;
        return getFinalCommand(getCurrentSerialNumber(), needCrc16Command);
    }


    /**
     * English
     */
    public static final byte LANGUAGE_ENGLISH = 0x01;
    /**
     * German
     */
    public static final byte LANGUAGE_GERMAN = 0x02;
    /**
     * Japanese
     */
    public static final byte LANGUAGE_JAPANESE = 0x03;
    /**
     * Chinese
     */
    public static final byte LANGUAGE_CHINESE = 0x04;
    /**
     * French
     */
    public static final byte LANGUAGE_FRENCH = 0x05;
    /**
     * Korean
     */
    public static final byte LANGUAGE_KOREAN = 0x06;
    /**
     * Spanish
     */
    public static final byte LANGUAGE_SPANISH = 0x07;

    /**
     * Set glasses locale
     * @param language language to set
     * @return Set glasses locale command
     */
    public static byte[] setLanguage(byte language) {
        byte[] needCrc16Command = new byte[2];
        needCrc16Command[0] = (byte) 0xD4;
        needCrc16Command[1] = language;
        return getFinalCommand(getCurrentSerialNumber(), needCrc16Command);
    }

    /**
     * Notification
     * @param title           title
     * @param content         content
     * @param belongSoftware  Software
     * @param timestamp       Timestamp of push message (seconds)
     * @return                Notification command
     */
    public static ArrayList<byte[]>  setNotifyMessage(String title,
                                                      String content,
                                                      int belongSoftware,
                                                      long timestamp) {
        return setNotifyMessage(title, content, belongSoftware, String.valueOf(timestamp));
    }


    private static final int MAX_PAYLOAD_LENGTH = 168;

    public static final int NOTIFY_MESSAGE_STYLE_DEFAULT = 0;
    public static final int NOTIFY_MESSAGE_STYLE_ONE = 1;
    public static final int NOTIFY_MESSAGE_STYLE_TWO = 2;
    public static final int NOTIFY_MESSAGE_STYLE_THREE = 3;

    /**
     * Notification
     * @param title           title
     * @param content         content
     * @param belongSoftware  Software
     * @param timestamp       Timestamp of push message (seconds)
     * @return                Notification command
     */
    public static ArrayList<byte[]>  setNotifyMessage(String title,
                                                      String content,
                                                      int belongSoftware,
                                                      String timestamp) {
        return setNotifyMessage(title, content, belongSoftware, timestamp, NOTIFY_MESSAGE_STYLE_DEFAULT);
    }

    /**
     * incoming call
     */
    public final static int BELONG_SOFTWARE_CALL = 0;
    /**
     * SMS notification
     */
    public final static int BELONG_SOFTWARE_MESSAGE = 1;
    /**
     * Skype notification
     */
    public final static int BELONG_SOFTWARE_SKYPE = 2;
    /**
     * Line notification
     */
    public final static int BELONG_SOFTWARE_LINE = 3;
    /**
     * kakaotalk notification
     */
    public final static int BELONG_SOFTWARE_KAKAOTALK = 4;
    /**
     * facebook notification
     */
    public final static int BELONG_SOFTWARE_FACEBOOK = 5;
    /**
     * twitter（x）notification
     */
    public final static int BELONG_SOFTWARE_X = 6;
    /**
     * whatsapp notification
     */
    public final static int BELONG_SOFTWARE_WHATSAPP = 7;
    /**
     * linkedin notification
     */
    public final static int BELONG_SOFTWARE_LINKEDIN = 8;
    /**
     * viber notification
     */
    public final static int BELONG_SOFTWARE_VIBER = 9;
    /**
     * instagram notification
     */
    public final static int BELONG_SOFTWARE_INSTAGRAM = 10;
    /**
     * messenger notification
     */
    public final static int BELONG_SOFTWARE_MESSENGER = 11;
    /**
     * Wechat notification
     */
    public final static int BELONG_SOFTWARE_WECHAT = 12;
    /**
     * QQ notification
     */
    public final static int BELONG_SOFTWARE_QQ = 13;
    /**
     * DingDing notification
     */
    public final static int BELONG_SOFTWARE_DINGDING = 14;
    /**
     * Other opp
     */
    public final static int BELONG_SOFTWARE_OTHER = 15;


    /**
     * Notification
     * @param title           title
     * @param content         content
     * @param belongSoftware  software
     *                        {@link BleCommand#BELONG_SOFTWARE_CALL incoming call}
     *                        {@link BleCommand#BELONG_SOFTWARE_MESSAGE SMS notification}
     *                        {@link BleCommand#BELONG_SOFTWARE_SKYPE Skype notification}
     *                        {@link BleCommand#BELONG_SOFTWARE_LINE Line notification}\
     *                        {@link BleCommand#BELONG_SOFTWARE_KAKAOTALK kakaotalk notification}
     *                        {@link BleCommand#BELONG_SOFTWARE_FACEBOOK facebook notification}
     *                        {@link BleCommand#BELONG_SOFTWARE_X twitter(X) notification}
     *                        {@link BleCommand#BELONG_SOFTWARE_WHATSAPP whatsapp notification}
     *                        {@link BleCommand#BELONG_SOFTWARE_LINKEDIN linkedin notification}
     *                        {@link BleCommand#BELONG_SOFTWARE_VIBER viber notification}
     *                        {@link BleCommand#BELONG_SOFTWARE_INSTAGRAM instagram notification}
     *                        {@link BleCommand#BELONG_SOFTWARE_MESSENGER messenger notification}
     *                        {@link BleCommand#BELONG_SOFTWARE_WECHAT WeChat notification}
     *                        {@link BleCommand#BELONG_SOFTWARE_QQ QQ notification}
     *                        {@link BleCommand#BELONG_SOFTWARE_DINGDING DingDing notification}
     *                        {@link BleCommand#BELONG_SOFTWARE_OTHER Other app}
     * @param timestamp       Timestamp of push message (seconds)
     * @param uiStyle         ui style。default is {@link BleCommand#NOTIFY_MESSAGE_STYLE_DEFAULT default style}
     *                        {@link BleCommand#NOTIFY_MESSAGE_STYLE_ONE Style one}
     *                        {@link BleCommand#NOTIFY_MESSAGE_STYLE_TWO Style two}
     *                        {@link BleCommand#NOTIFY_MESSAGE_STYLE_THREE Style three}
     * @return                Notification command
     */
    public static ArrayList<byte[]>  setNotifyMessage(String title,
                                                      String content,
                                                      int belongSoftware,
                                                      String timestamp,
                                                      int uiStyle) {

        byte use = (byte) 0xD5;
        byte currentSerialNumber = getCurrentSerialNumber();
        ArrayList<byte[]> willSendByteList;
        String keyValueData = "t:" + title + ";c:" + content + ";b:" + belongSoftware + ";m:" + timestamp + ";u:" + uiStyle;
        byte[] allWillSendPayload = keyValueData.getBytes(StandardCharsets.UTF_8);
        if (allWillSendPayload.length / MAX_PAYLOAD_LENGTH == 0) {
            // Only one data pack
            willSendByteList = new ArrayList<>(1);
            int payloadLength = allWillSendPayload.length;
            byte[] needCrc16Command = new byte[3+payloadLength];
            needCrc16Command[0] = use;
            // Pack count
            needCrc16Command[1] = 0x01;
            // Which pack
            needCrc16Command[2] = 0x01;
            System.arraycopy(allWillSendPayload, 0, needCrc16Command, 3, payloadLength);
            willSendByteList.add(getFinalCommand(currentSerialNumber, needCrc16Command));
        } else {
            int willSendListLength = (allWillSendPayload.length / MAX_PAYLOAD_LENGTH) + 1;
            willSendByteList = new ArrayList<>(willSendListLength);
            // Assemble data
            for (int i=0; i<willSendListLength; i++) {
                if (i == willSendListLength-1) {
                    // Last pack
                    int lastPayloadLength = allWillSendPayload.length % MAX_PAYLOAD_LENGTH;
                    byte[] needCrc16Command = new byte[3+lastPayloadLength];
                    needCrc16Command[0] = use;
                    // Pack count
                    needCrc16Command[1] = (byte) willSendListLength;;
                    // Which pack
                    needCrc16Command[2] = (byte) (i+1);
                    System.arraycopy(allWillSendPayload, (i * MAX_PAYLOAD_LENGTH), needCrc16Command, 3, lastPayloadLength);
                    willSendByteList.add(getFinalCommand(currentSerialNumber, needCrc16Command));
                } else {
                    byte[] needCrc16Command = new byte[3+MAX_PAYLOAD_LENGTH];
                    needCrc16Command[0] = use;
                    // Pack count
                    needCrc16Command[1] = (byte) willSendListLength;;
                    // Which pack
                    needCrc16Command[2] = (byte) (i+1);
                    System.arraycopy(allWillSendPayload, (i * MAX_PAYLOAD_LENGTH), needCrc16Command, 3, MAX_PAYLOAD_LENGTH);
                    willSendByteList.add(getFinalCommand(currentSerialNumber, needCrc16Command));
                }
            }
        }
        return willSendByteList;
    }

    /**
     * Time Setting (24 hour clock)
     * @param year   Year
     * @param month  Month
     * @param day    Day
     * @param hour   Hour
     * @param minute Minute
     * @param second Second
     * @return Time Setting command
     */
    public static byte[] setTime(int year, int month, int day, int hour, int minute, int second) {
        byte[] needCrc16Command = new byte[7];
        needCrc16Command[0] = (byte) 0xD7;
        needCrc16Command[1] = (byte) (year-2000);
        month = getRealMonth(month);
        needCrc16Command[2] = (byte) month;
        day = getRealDay(year, month, day);
        needCrc16Command[3] = (byte) day;
        hour = getRealHour(hour);
        needCrc16Command[4] = (byte) hour;
        minute = getRealMinute(minute);
        needCrc16Command[5] = (byte) minute;
        second = getRealSecond(second);
        needCrc16Command[6] = (byte) second;
        return getFinalCommand(getCurrentSerialNumber(), needCrc16Command);
    }

    private static int getRealHour(int hour) {
        return Math.max(Math.min(hour, 23), 0);
    }

    private static int getRealMinute(int minute) {
        return Math.max(Math.min(minute, 59), 0);
    }

    private static int getRealSecond(int second) {
        return Math.max(Math.min(second, 59), 0);
    }

    private static boolean leapYear(int year) {
        return ((year%4==0) && (year%100!=0)) || (year % 400 == 0);
    }

    private static int getRealDay(int year, int month, int day) {
        if (day < 1) {
            return 1;
        }
        if (month == 1
                || month == 3
                || month == 5
                || month == 7
                || month == 8
                || month == 10
                || month == 12) {
            return Math.min(day, 31);
        } else if (month == 2) {
            if (leapYear(year)) {
                return Math.min(day, 29);
            } else {
                return Math.min(day, 28);
            }
        } else {
            return Math.min(day, 30);
        }
    }

    private static int getRealMonth(int month) {
        if (month > 12) {
            return  12;
        }
        return Math.max(month, 1);
    }

    /**
     * Set the display style of UI
     * @param style display style
     * @param uiId  ui ID
     * @return et the display style of UI command
     */
    public static byte[] setUIStyle(byte style, byte uiId) {
        byte[] needCrc16Command = new byte[3];
        needCrc16Command[0] = (byte) 0xD8;
        needCrc16Command[1] = style;
        needCrc16Command[2] = uiId;

        return getFinalCommand(getCurrentSerialNumber(), needCrc16Command);
    }

    /**
     * Get device information
     * @return Get device information command
     */
    public static byte[] getDeviceInfo() {
        byte[] needCrc16Command = new byte[1];
        needCrc16Command[0] = (byte) 0xD9;

        return getFinalCommand(getCurrentSerialNumber(), needCrc16Command);
    }

    /**
     * Set up message notifications
     * @param skypeOpen       Is skype enabled
     * @param lineOpen        Is line enabled
     * @param phoneCallOpen   Is incoming call enabled
     * @param messageOpen     Is SMS enabled
     * @param wechatOpen      Is WeChat enabled
     * @param qqOpen          Is QQ enabled
     * @param kakaotalkOpen   Is kakaotalk enabled
     * @param facebookOpen    Is Facebook enabled
     * @param twitterOpen     Is twitter enabled
     * @param whatsappOpen    Is WhatsApp enabled
     * @param linkedinOpen    Is LinkedIn enabled
     * @param viberOpen       Is Viber enabled
     * @param instagramOpen   Is Instagram enabled
     * @param messengerOpen   Is messenger enabled
     * @param otherAppOpen    Is other app enabled
     * @param dingdingOpen    Is DingDing enabled
     * @return Set up message notifications command
     */
    public static byte[] setMessageNotification(boolean skypeOpen,
                                                boolean lineOpen,
                                                boolean phoneCallOpen,
                                                boolean messageOpen,
                                                boolean wechatOpen,
                                                boolean qqOpen,
                                                boolean kakaotalkOpen,
                                                boolean facebookOpen,
                                                boolean twitterOpen,
                                                boolean whatsappOpen,
                                                boolean linkedinOpen,
                                                boolean viberOpen,
                                                boolean instagramOpen,
                                                boolean messengerOpen,
                                                boolean otherAppOpen,
                                                boolean dingdingOpen) {

        byte[] needCrc16Command = new byte[18];
        needCrc16Command[0] = (byte) 0xDD;
        needCrc16Command[1] = 0x01;
        needCrc16Command[2] = (byte) (skypeOpen?0x01:0x00);
        needCrc16Command[3] = (byte) (lineOpen?0x01:0x00);
        needCrc16Command[4] = (byte) (phoneCallOpen?0x01:0x00);
        needCrc16Command[5] = (byte) (messageOpen?0x01:0x00);
        needCrc16Command[6] = (byte) (wechatOpen?0x01:0x00);
        needCrc16Command[7] = (byte) (qqOpen?0x01:0x00);
        needCrc16Command[8] = (byte) (kakaotalkOpen?0x01:0x00);
        needCrc16Command[9] = (byte) (facebookOpen?0x01:0x00);
        needCrc16Command[10] = (byte) (twitterOpen?0x01:0x00);
        needCrc16Command[11] = (byte) (whatsappOpen?0x01:0x00);
        needCrc16Command[12] = (byte) (linkedinOpen?0x01:0x00);
        needCrc16Command[13] = (byte) (viberOpen?0x01:0x00);
        needCrc16Command[14] = (byte) (instagramOpen?0x01:0x00);
        needCrc16Command[15] = (byte) (messengerOpen?0x01:0x00);
        needCrc16Command[16] = (byte) (otherAppOpen?0x01:0x00);
        needCrc16Command[17] = (byte) (dingdingOpen?0x01:0x00);

        return getFinalCommand(getCurrentSerialNumber(), needCrc16Command);
    }

    /**
     * Get message notification settings
     * @return Get message notification settings command
     */
    public static byte[] getMessageNotification() {
        byte[] needCrc16Command = new byte[2];
        needCrc16Command[0] = (byte) 0xDD;
        needCrc16Command[1] = 0x02;

        return getFinalCommand(getCurrentSerialNumber(), needCrc16Command);
    }

    /**
     * Get sensor data
     * @return Get sensor data command
     */
    public static byte[] getSensorData() {

        byte[] needCrc16Command = new byte[1];
        needCrc16Command[0] = (byte) 0xDE;

        return getFinalCommand(getCurrentSerialNumber(), needCrc16Command);
    }


    /**
     * Read and write mode
     */
    public static final byte AI_READING_AND_WRITING_MODE = 0x01;

    /**
     * Driving mode
     */
    public static final byte AI_DRIVING_MODE = 0x02;

    /**
     * Gaming mode
     */
    public static final byte AI_GAMING_MODE = 0x03;

    /**
     * Translation mode
     */
    public static final byte AI_TRANSLATION_MODE = 0x04;

    /**
     * Cooking mode
     */
    public static final byte AI_COOKING_MODE = 0x05;

    /**
     * Morse code mode
     */
    public static final byte AI_MORSE_CODE_MODE = 0x06;

    /**
     * Team mode
     */
    public static final byte AI_TEAM_MODE = 0x07;

    /**
     * Yoga mode
     */
    public static final byte AI_YOGA_MODE = 0x08;

    /**
     * ChatGPT mode
     */
    public static final byte AI_CHATGPT_MODE = 0x09;

    /**
     * Phone mode
     */
    public static final byte AI_PHONE_MODE = 0x0A;

    /**
     * Set data related to AI mode
     * @param addedOtherDevice Whether to add additional pickup equipment
     * @param otherDeviceType  Other device types
     * @param aiMode           Custom Mode
     *                         {@link BleCommand#AI_READING_AND_WRITING_MODE Reading and Writing mode}
     *                         {@link BleCommand#AI_DRIVING_MODE Driving mode}
     *                         {@link BleCommand#AI_GAMING_MODE Gaming mode}
     *                         {@link BleCommand#AI_TRANSLATION_MODE Translation mode}
     *                         {@link BleCommand#AI_COOKING_MODE Cooking mode}
     *                         {@link BleCommand#AI_MORSE_CODE_MODE Morse code mode}
     *                         {@link BleCommand#AI_TEAM_MODE Team mode}
     *                         {@link BleCommand#AI_YOGA_MODE Yoga mode}
     *                         {@link BleCommand#AI_CHATGPT_MODE ChatGPT mode}
     *                         {@link BleCommand#AI_PHONE_MODE Phone mode}
     * @return                 Set data related to AI mode command
     */
    public static byte[] setAI(boolean addedOtherDevice, byte otherDeviceType, byte aiMode) {

        byte[] needCrc16Command = new byte[4];
        needCrc16Command[0] = (byte) 0xDA;
        needCrc16Command[1] = (byte) (addedOtherDevice?0x01:0x00);
        needCrc16Command[2] = otherDeviceType;
        needCrc16Command[3] = aiMode;

        return getFinalCommand(getCurrentSerialNumber(), needCrc16Command);
    }

    /**
     * AI replies to the text content of the glasses
     * @param question Question
     * @param replyContent Content of AI reply
     * @return AI replies to the text content of the glasses command
     */
    public static ArrayList<byte[]> aiReply(String question, String replyContent) {
        byte use = (byte) 0xDC;
        byte currentSerialNumber = getCurrentSerialNumber();
        ArrayList<byte[]> willSendByteList;
        String keyValueData = "q:" + question + ";c:" + replyContent;
        byte[] allWillSendPayload = keyValueData.getBytes(StandardCharsets.UTF_8);
        if (allWillSendPayload.length / MAX_PAYLOAD_LENGTH == 0) {
            // Only one data pack
            willSendByteList = new ArrayList<>(1);
            int payloadLength = allWillSendPayload.length;
            byte[] needCrc16Command = new byte[3+payloadLength];
            needCrc16Command[0] = use;
            // Pack count
            needCrc16Command[1] = 0x01;
            // Which pack
            needCrc16Command[2] = 0x01;
            System.arraycopy(allWillSendPayload, 0, needCrc16Command, 3, payloadLength);
            willSendByteList.add(getFinalCommand(currentSerialNumber, needCrc16Command));
        } else {
            int willSendListLength = (allWillSendPayload.length / MAX_PAYLOAD_LENGTH) + 1;
            willSendByteList = new ArrayList<>(willSendListLength);
            // Assemble data
            for (int i=0; i<willSendListLength; i++) {
                if (i == willSendListLength-1) {
                    // Last pack
                    int lastPayloadLength = allWillSendPayload.length % MAX_PAYLOAD_LENGTH;
                    byte[] needCrc16Command = new byte[3+lastPayloadLength];
                    needCrc16Command[0] = use;
                    // Pack count
                    needCrc16Command[1] = (byte) willSendListLength;;
                    // Which pack
                    needCrc16Command[2] = (byte) (i+1);
                    System.arraycopy(allWillSendPayload, (i * MAX_PAYLOAD_LENGTH), needCrc16Command, 3, lastPayloadLength);
                    willSendByteList.add(getFinalCommand(currentSerialNumber, needCrc16Command));
                } else {
                    byte[] needCrc16Command = new byte[3+MAX_PAYLOAD_LENGTH];
                    needCrc16Command[0] = use;
                    // Pack count
                    needCrc16Command[1] = (byte) willSendListLength;;
                    // Which pack
                    needCrc16Command[2] = (byte) (i+1);
                    System.arraycopy(allWillSendPayload, (i * MAX_PAYLOAD_LENGTH), needCrc16Command, 3, MAX_PAYLOAD_LENGTH);
                    willSendByteList.add(getFinalCommand(currentSerialNumber, needCrc16Command));
                }
            }
        }
        return willSendByteList;
    }

    /**
     * Set the Bluetooth name of the device
     * @param name The Bluetooth name to be set is limited to a maximum of 168 characters. If the transmission characters are too long, they will be cropped.
     * @return Set the Bluetooth name of the device command
     */
    public static byte[] setDeviceName(String name) {
        byte[] nameByte = name.getBytes(StandardCharsets.UTF_8);
        // The maximum limit is 168.
        // If the transmission characters are too long, they will be cropped.
        if (nameByte.length > MAX_PAYLOAD_LENGTH) {
            byte[] tmp = new byte[MAX_PAYLOAD_LENGTH];
            System.arraycopy(nameByte, 0, tmp, 0, MAX_PAYLOAD_LENGTH);
            nameByte = tmp;
        }
        byte[] needCrc16Command = new byte[1+nameByte.length];
        needCrc16Command[0] = (byte) 0xDF;
        System.arraycopy(nameByte, 0, needCrc16Command, 1, nameByte.length);
        return getFinalCommand(getCurrentSerialNumber(), needCrc16Command);
    }

    /**
     * Serial Number
     * @return Each time an instruction is completed, the next instruction is incremented by 1.
     */
    private static byte getCurrentSerialNumber() {
        SERIAL_NUMBER++;
        if (SERIAL_NUMBER == Byte.MAX_VALUE) {
            SERIAL_NUMBER = Byte.MIN_VALUE;
        }
        return SERIAL_NUMBER;
    }

    /**
     * Get the high and low bytes of int in big endian mode (two bits, discard the first two bytes)
     * @param value int value
     * @return High and low byte array in big endian mode
     */
    private static byte[] getHighAndLowByte(int value) {
        byte high = (byte) ((value>>8) & 0xFF);
        byte low = (byte) (value & 0xFF);
        return new byte[] {high, low};
    }

}
