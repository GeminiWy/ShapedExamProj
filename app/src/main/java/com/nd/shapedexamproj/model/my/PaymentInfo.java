package com.nd.shapedexamproj.model.my;
/**
 * 缴费项
 * @author zll
 * create in 2014-4-24
 */
public class PaymentInfo {
	/**
	 * 缴费类型(类型：1=注册建档，2=选课，3=补考)
	 */
	public String pay_type ;
	/**
	 * 缴费说明
	 */
	public String pay_info = "";
	/**
	 * 缴费创建时间
	 */
	public String add_time;
	/**
	 * 支付金额
	 */
	public String pay_amount;
	/**
	 * 订单ID号
	 */
	public String order_id = "";
	/**
	 * 支付日期
	 */
	public String pay_time;
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof PaymentInfo)){
			return false ;
		}
		PaymentInfo paymentInfo = (PaymentInfo) o;
		if(paymentInfo.order_id.equals(this.order_id) && paymentInfo.pay_info.equals(this.pay_info)){
			return true ;
		} else {
			return false ;
		}
		
	}
}
