package com.qugengting.email.eventbus;

/**
 * Created by xuruibin on 2018/5/23.
 * 描述：
 */
public class MailSearchEvent {
    private String searchKey;

    public String getSearchKey() {
        return searchKey;
    }

    public MailSearchEvent(String searchKey) {
        this.searchKey = searchKey;
    }
}
