package com.nd.shapedexamproj.entity;

/**
 * <p>  用户关注实体类 </p>
 * <p> Created by xuwenzhuo on 2014/11/19.</p>
 */
public class RelatedUserEntity {

    private int mType;
    private String mUserId;
    private String mRelatedId;
    private String mRelatedUserName;
    private String mRelatedImg;
    private String mUserDetail;

    public int getmType() {
        return mType;
    }

    public void setmType(int mType) {
        this.mType = mType;
    }

    public String getmUserId() {
        return mUserId;
    }

    public void setmUserId(String mUserId) {
        this.mUserId = mUserId;
    }

    public String getmRelatedId() {
        return mRelatedId;
    }

    public void setmRelatedId(String mRelatedId) {
        this.mRelatedId = mRelatedId;
    }

    public String getmRelatedUserName() {
        return mRelatedUserName;
    }

    public void setmRelatedUserName(String mRelatedUserName) {
        this.mRelatedUserName = mRelatedUserName;
    }

    public String getmRelatedImg() {
        return mRelatedImg;
    }

    public void setmRelatedImg(String mRelatedImg) {
        this.mRelatedImg = mRelatedImg;
    }

    public String getmUserDetail() {
        return mUserDetail;
    }

    public void setmUserDetail(String mUserDetail) {
        this.mUserDetail = mUserDetail;
    }

    /**
     * <p>判定实体对象是否相同</p>
     * @param relatedUserEntity
     * @return true 相同 false 不同
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RelatedUserEntity)) {
            return false;
        }
        RelatedUserEntity entity = (RelatedUserEntity) o;
        if (entity.mRelatedImg.equals(this.mRelatedImg)) {
            return true;
        } else {
            return false;
        }
    }
}
