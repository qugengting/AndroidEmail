package com.qugengting.email.eventbus;

/**
 * Created by xuruibin on 2018/5/23.
 * 描述：
 */
public class MailStatusEvent {

    public static final int TYPE_DELETE = 1;

    public static final int TYPE_READ = 2;

    private final int type;

    private final int position;
    public MailStatusEvent(int type, int position) {
        this.type = type;
        this.position = position;
    }

    public int getType() {
        return type;
    }

    public int getPosition() {
        return position;
    }
}
