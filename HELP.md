public class Iso8583msgDecoder extends ByteToMessageDecoder {
    private static final int MAX_FRAME_LENGTH = 8192;
    private static final int LENGTH_FIELD_LENGTH = 4;
    private static final int MTI_LENGTH = 4;

    private MessageFactory<IsoMessage> messageFactory;

    public Iso8583msgDecoder() {
        try {
            this.messageFactory = ConfigParser.createFromClasspathConfig("j8583.xml");
            this.messageFactory.setCharacterEncoding(StandardCharsets.UTF_8.name());
            this.messageFactory.setAssignDate(true);
        } catch (Exception e) {
            log.error("初始化ISO8583消息工厂失败", e);
            throw new RuntimeException("初始化ISO8583消息工厂失败", e); // 快速失败，避免创建无效的解码器
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
        // 检查消息工厂是否初始化
        if (messageFactory == null) {
            log.error("ISO8583消息工厂未初始化");
            ctx.fireExceptionCaught(new IllegalStateException("ISO8583消息工厂未初始化"));
            return;
        }

        // 不使用无限循环，让Netty框架负责多次调用
        // 标记当前读取位置（用于异常时回滚）
        in.markReaderIndex();

        // 1. 至少需要4字节包长字段
        if (in.readableBytes() < LENGTH_FIELD_LENGTH) {
            // 数据不足，等待更多数据
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
            log.warn("❌ 无法解析长度字段{}，丢弃当前缓存 {} 字节，跳过错误包", bodyLength, discardBytes);
            // 向上层报告错误
            out.add(new DecodingError("长度字段解析失败: " + e.getMessage()));
            return;
        }

        // 2. 检查是否有足够的数据读取完整消息
        int totalFrameLength = LENGTH_FIELD_LENGTH + bodyLength;
        int readableBytes = in.readableBytes();

        if (readableBytes < totalFrameLength) {
            // 统一半包处理逻辑
            // 检查MTI有效性，避免等待无效数据
            if (readableBytes >= LENGTH_FIELD_LENGTH + MTI_LENGTH) {
                String mtiStr = readMtiString(in, in.readerIndex() + LENGTH_FIELD_LENGTH);
                
                if (!isValidIso8583Mti1993(mtiStr)) {
                    // 非法MTI，丢弃当前缓存
                    in.skipBytes(readableBytes);
                    log.warn("❌ 半包MTI非法: '{}', 丢弃当前缓存 {} 字节", mtiStr, readableBytes);
                    out.add(new DecodingError("非法MTI: " + mtiStr));
                } else {
                    // MTI合法，重置读取位置等待更多数据
                    in.resetReaderIndex();
                    log.debug("⏳ 半包等待中: 需要={}, 当前={}", totalFrameLength, readableBytes);
                }
            } else {
                // 数据不足以读取MTI，重置读取位置等待更多数据
                in.resetReaderIndex();
                log.debug("⏳ 半包等待中: 需要={}, 当前={}", totalFrameLength, readableBytes);
            }
            return;
        }

        // 3. 读取并验证MTI
        String mtiStr = readMtiString(in, in.readerIndex() + LENGTH_FIELD_LENGTH);
        if (!isValidIso8583Mti1993(mtiStr)) {
            // 跳过长度字段和无效数据
            in.skipBytes(totalFrameLength);
            log.warn("❌ MTI非法: '{}', 跳过当前消息", mtiStr);
            out.add(new DecodingError("非法MTI: " + mtiStr));
            return;
        }

        // 4. 读取完整消息
        in.skipBytes(LENGTH_FIELD_LENGTH); // 跳过长度字段
        byte[] messageBytes = new byte[bodyLength];
        in.readBytes(messageBytes);

        try {
            IsoMessage isoMessage = messageFactory.parseMessage(messageBytes, 0);
            if (isoMessage != null) {
                out.add(isoMessage);
                log.info("✅ 解析成功 MTI: {}", String.format("%04X", isoMessage.getType()));
            } else {
                log.warn("⚠️ 解析返回 null");
                out.add(new DecodingError("ISO8583解析返回null"));
            }
        } catch (Exception e) {
            log.error("❌ 解析 ISO8583 报文失败", e);
            out.add(new DecodingError("ISO8583解析异常: " + e.getMessage()));
        }
    }

    /**
     * 读取MTI字符串
     * @param in ByteBuf输入
     * @param startIndex 开始索引
     * @return MTI字符串
     */
    private String readMtiString(ByteBuf in, int startIndex) {
        byte[] mtiBytes = new byte[MTI_LENGTH];
        in.getBytes(startIndex, mtiBytes);
        return new String(mtiBytes, StandardCharsets.US_ASCII);
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
    
    /**
     * 解码错误类，用于向上层报告解码过程中的错误
     */
    public static class DecodingError {
        private final String message;
        
        public DecodingError(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return "ISO8583解码错误: " + message;
        }
    }
}