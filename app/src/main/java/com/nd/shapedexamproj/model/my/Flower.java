package com.nd.shapedexamproj.model.my;

public class Flower {

	private int source;
	private int flowerCount;
	private int receiverId;
	private String userId;
	private int flowerRelationId;
	private String sendDate;

	public int getSource() {
		return source;
	}

	public void setSource(int source) {
		this.source = source;
	}

	public int getFlowerCount() {
		return flowerCount;
	}

	public void setFlowerCount(int flowerCount) {
		this.flowerCount = flowerCount;
	}

	public int getReceiverId() {
		return receiverId;
	}

	public void setReceiverId(int receiverId) {
		this.receiverId = receiverId;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public int getFlowerRelationId() {
		return flowerRelationId;
	}

	public void setFlowerRelationId(int flowerRelationId) {
		this.flowerRelationId = flowerRelationId;
	}

	public String getSendDate() {
		return sendDate;
	}

	public void setSendDate(String sendDate) {
		this.sendDate = sendDate;
	}

}
