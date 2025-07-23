public class Iso8583msgDecoder extends ByteToMessageDecoder {
    private static final int MAX_FRAME_LENGTH = 8192;
    private static final int LENGTH_FIELD_LENGTH = 4;
    private static final int MTI_LENGTH = 4;

    MessageFactory<IsoMessage> messageFactory;

    public Iso8583msgDecoder() {
        try {
            this.messageFactory = ConfigParser.createFromClasspathConfig("j8583.xml");
            this.messageFactory.setCharacterEncoding(StandardCharsets.UTF_8.name());
            this.messageFactory.setAssignDate(true);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Decodes ISO8583 message from [ByteBuf].
     * <p>
     * <p>
     * Message body starts immediately, no length header
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        while (true) {
            // 标记当前读取位置（用于异常时回滚）
            in.markReaderIndex();

            // 1. 至少需要4字节包长字段
            if (in.readableBytes() < 4) {
                if (in.readableBytes() > 0) {
                    log.warn("❌至少需要4字节包长字段");
                }
                return;
            }

            int bodyLength = 0;
            try {
                bodyLength = parseLengthField(in);
                if (bodyLength <= 0 || bodyLength > MAX_FRAME_LENGTH) {
                    throw new IllegalArgumentException("非法长度: " + bodyLength);
                }
            } catch (Exception e) {
                in.resetReaderIndex();
                int discardBytes = in.readableBytes();
                // 丢弃当前缓冲区所有数据
                in.skipBytes(discardBytes);
                log.info("❌ 无法解析长度字段{}，丢弃当前缓存 {} 字节，跳过错误包", bodyLength, discardBytes);
                // 直接退出等待下次新数据，不继续解析
                return;
            }

            // 2. 不够完整包体，等待更多数据
            int totalFrameLength = LENGTH_FIELD_LENGTH + bodyLength;
            int readableBytes = in.readableBytes();

            if (readableBytes < totalFrameLength) {
                // 半包，但先看看是否有足够数据读MTI
                if (readableBytes >= LENGTH_FIELD_LENGTH + MTI_LENGTH) {
                    // 读取MTI，判断是否合法
                    int mtiStartIndex = in.readerIndex() + LENGTH_FIELD_LENGTH;
                    byte[] mtiBytes = new byte[MTI_LENGTH];
                    in.getBytes(mtiStartIndex, mtiBytes);
                    String mtiStr = new String(mtiBytes, StandardCharsets.US_ASCII);

                    if (!isValidIso8583Mti1993(mtiStr)) {
                        // 非法 MTI，丢弃本次缓存
                        in.skipBytes(readableBytes);
                        log.warn("❌ 半包MTI非法: '{}', 丢弃当前缓存 {} 字节，跳过错误包", mtiStr, readableBytes);
                    } else {
                        // MTI合法，继续等真正的半包
                        in.resetReaderIndex();
                        log.info("⏳ 半包等待中: total={} 当前={}", totalFrameLength, readableBytes);
                    }
                } else {
                    // 甚至连MTI都没读够，继续等待
                    in.resetReaderIndex();
                    log.info("⏳ 半包等待中（MTI未满）: total={} 当前={}", totalFrameLength, readableBytes);
                }
                return; // 或 continue 看你整体代码逻辑
            }

            // 先peek报文体的前4字节，解析MTI
            int mtiStartIndex = in.readerIndex() + LENGTH_FIELD_LENGTH;
            byte[] mtiBytes = new byte[MTI_LENGTH];
            in.getBytes(mtiStartIndex, mtiBytes);

            // 3. 读取数据
            // 跳过长度字段，读取报文体
            in.skipBytes(LENGTH_FIELD_LENGTH); // 跳过长度字段
            byte[] messageBytes = new byte[bodyLength];
            in.readBytes(messageBytes);

            try {
                IsoMessage isoMessage = messageFactory.parseMessage(messageBytes, 0);
                if (isoMessage != null) {
                    out.add(isoMessage);
                    Console.log("✅ 解析成功 MTI: {}", String.format("%04X", isoMessage.getType()));
                } else {
                    log.warn("⚠️ 解析返回 null");
                }
            } catch (Exception e) {
                log.error("❌ 解析 ISO8583 报文失败", e);
            }
        }
    }

    /**
     * 解析4字节 ASCII 长度字段（不移动 readerIndex）
     */
    private int parseLengthField(ByteBuf in) throws Exception {
        byte[] lengthBytes = new byte[LENGTH_FIELD_LENGTH];
        in.getBytes(in.readerIndex(), lengthBytes);
        String lengthStr = new String(lengthBytes, StandardCharsets.US_ASCII).trim();

        if (!lengthStr.matches("\\d+")) {
            throw new NumberFormatException("长度字段非法: " + lengthStr);
        }
        return Integer.parseInt(lengthStr);
    }

    /**
     * 判断是否是有效的ISO8583 MTI（1993标准）
     * MTI必须是4位数字，且格式：1[0-6][0-3][0-9]
     * @param mti MTI字符串
     * @return true 有效，false 无效
     */
    public boolean isValidIso8583Mti1993(String mti) {
        if (mti == null) return false;
        String cleanMti = mti.trim();
        return cleanMti.matches("1[0-6][0-3][0-9]");
    }
}