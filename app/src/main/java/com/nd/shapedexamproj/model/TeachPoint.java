package com.nd.shapedexamproj.model;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * 具体的教学点
 * @version 1.0.0 
 * @author Abay Zhuang <br/>
 *		   Create at 2014-5-27
 * 修改者，修改日期，修改内容。
 */
public class TeachPoint implements Parcelable{
	private String dept_id;
	private String name;
	private String address;
	private String phone;

	public String getDept_id() {
		return dept_id;
	}

	public void setDept_id(String dept_id) {
		this.dept_id = dept_id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public TeachPoint() {

	}

	public TeachPoint(Parcel source) {
		dept_id = source.readString();
		name = source.readString();
		address = source.readString();
		phone = source.readString();

	}

	// 实现Parcelable的方法writeToParcel，将ParcelableDate序列化为一个Parcel对象
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(dept_id);
		dest.writeString(name);
		dest.writeString(address);
		dest.writeString(phone);
	
	}

	// 实例化静态内部对象CREATOR实现接口Parcelable.Creator
	public static final Creator<TeachPoint> CREATOR = new Creator<TeachPoint>() {

		@Override
		public TeachPoint[] newArray(int size) {
			return new TeachPoint[size];
		}

		// 将Parcel对象反序列化为ParcelableDate
		@Override
		public TeachPoint createFromParcel(Parcel source) {
			return new TeachPoint(source);
		}
	};
}
