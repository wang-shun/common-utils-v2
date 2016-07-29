package com.youzan.sz.common.nsq;

public abstract class NsqMessage {

    public abstract byte[] encode();

    public abstract <T extends NsqMessage> T decode(String message);
}
