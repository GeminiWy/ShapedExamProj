package com.nd.shapedexamproj.entity;

import java.util.List;

/**
 * <p>  RelatedUserListEntity 关注结果对象，里面包含一个关注对象列表 </p>
 * <p> Created by xuwenzhuo on 2014/11/19.</p>
 */
public class RelatedUserListEntity {

    private List<RelatedUserEntity> mRelatedUserList;
    private int mFlag;
    private int mTotalSize;
    private String mInfo;

    public List<RelatedUserEntity> getmRelatedUserList() {
        return mRelatedUserList;
    }

    public void setmRelatedUserList(List<RelatedUserEntity> mRelatedUserList) {
        this.mRelatedUserList = mRelatedUserList;
    }

    public int getmFlag() {
        return mFlag;
    }

    public void setmFlag(int mFlag) {
        this.mFlag = mFlag;
    }

    public int getmTotalSize() {
        return mTotalSize;
    }

    public void setmTotalSize(int mTotalSize) {
        this.mTotalSize = mTotalSize;
    }

    public String getmInfo() {
        return mInfo;
    }

    public void setmInfo(String mInfo) {
        this.mInfo = mInfo;
    }
}
