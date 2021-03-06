package com.studentloan.white.mode.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.fuiou.pay.FyPay;
import com.fuiou.pay.FyPayCallBack;
import com.fuiou.pay.util.AppConfig;
import com.fuiou.pay.util.MD5UtilString;
import com.studentloan.white.MyContacts;
import com.studentloan.white.R;
import com.studentloan.white.interfaces.DialogCallback;
import com.studentloan.white.net.HttpListener;
import com.studentloan.white.net.ServerInterface;
import com.studentloan.white.net.data.BankCard;
import com.studentloan.white.net.data.BooleanResponse;
import com.studentloan.white.net.data.Borrow;
import com.studentloan.white.net.data.BorrowResponse;
import com.studentloan.white.net.data.GetBankCardResponse;
import com.studentloan.white.net.data.StringResponse;
import com.studentloan.white.utils.DialogUtils;
import com.yolanda.nohttp.rest.Response;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.greenrobot.eventbus.EventBus;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@EActivity(R.layout.activity_refund_layout)
public class RefundActivity extends BaseActivity {
	
	@ViewById
	public TextView jkPriceTv,jkTimeTv,repartDatTv,yuqiDaysTv,refundPriceTv;

	private SimpleDateFormat dateFormat = null;

	private Borrow br;

	@Override
	@AfterViews
	public void initViews() {
		super.initViews();
		setTitleText("还款");

		dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		getUserInfo();
		getLoanInfo();

		FyPay.setDev(true);//此代码是配置jar包为环境配置，true是生产   false测试
		FyPay.init(this);


//		测试商户号：0002900F0096235
//		秘钥：5old71wihg2tqjug9kkpxnhx9hiujoqj
	}
	
	public void getLoanInfo(){
		String formatUrl = String.format(ServerInterface.BORROW_PROGRESS,userInfo.account.cellphone,userInfo.token);
		requestGet(formatUrl.hashCode(), formatUrl, BorrowResponse.class, new HttpListener<BorrowResponse>() {
			@TargetApi(Build.VERSION_CODES.N)
			@Override
			public void onSucceed(int what, Response<BorrowResponse> response) {
				if(response.isSucceed() && response.get() != null){
					if(response.get().result != null){
						br = response.get().result;
						jkPriceTv.setText(br.jieKuanJinE+"元");
						jkTimeTv.setText(br.jieKuanTianShu+"天");
						repartDatTv.setText(dateFormat.format(new Date(br.huanKuanDeadline)));
						yuqiDaysTv.setText(br.overdueDays+"天");

						int jl = userInfo.jiangLiJinE;

						if(userInfo.jiangLiJinE > 300){
							jl = 300;
						}
						String formatPrice = String.format("%.2f",br.yingHuanKuanJinE-jl);
						refundPriceTv.setText(formatPrice+"元");

					}else{
						finish();
					}
				}else{
					finish();
				}
			}

			@Override
			public void onFailed(int what, Response<BorrowResponse> response) {

			}
		},true);
	}

	@Click
	public void jkXieyiTv(){
		String formatUrl = String.format(ServerInterface.JIE_KUAN_XIE_YI,userInfo.account.cellphone,br.borrowId);
		com.studentloan.white.mode.activity.WebViewActivity_.intent(this).
				local(false).title("借款协议").url(MyContacts.BASE_URL+formatUrl).start();

	}
	@Click
	public void payBtn(){
		getBankCardList();
	}

	@Click
	public void alipayTv(){

		com.studentloan.white.mode.activity.AlipayRefundActivity_.intent(this).flags(Intent.FLAG_ACTIVITY_NEW_TASK).start();
	}

	private void getOrderNumber(final BankCard bankCard){
		String formatUrl = String.format(ServerInterface.GET_ORDER_NUMBER,userInfo.account.cellphone);
		requestGet(formatUrl.hashCode(), formatUrl, StringResponse.class, new HttpListener<StringResponse>() {
			@Override
			public void onSucceed(int what, Response<StringResponse> response) {
				if(response.isSucceed() && response.get() != null){
						if(!TextUtils.isEmpty(response.get().result)){
							pay(bankCard,response.get().result);
						}
				}
			}

			@Override
			public void onFailed(int what, Response<StringResponse> response) {

			}
		},true);
	}

	private void pay(BankCard bankCard,String mchntOrdId){

		int jl = userInfo.jiangLiJinE;

		if(userInfo.jiangLiJinE > 300){
			jl = 300;
		}
		final int jlNoti = jl;

		String mchnt_key = "fh46fyu0lezippityvtrrdwbv4cus33v";
		String mchnt_cd = "0002900F0395202";
		String amount = ((int)((br.yingHuanKuanJinE-jl) * 100d))+"";

		//String mchntOrdId = userInfo.account.accountId + "_"+(System.currentTimeMillis() / 1000);
		String Sing = MD5UtilString.MD5Encode("02" + "|" + "2.0" + "|"
				+ mchnt_cd + "|"
				+ mchntOrdId+ "|"
				+ userInfo.account.accountId+ "|"
				+ amount + "|"
				+ bankCard.bankCardNum + "|" + (MyContacts.BASE_URL+userInfo.backUrl) + "|"
				+ userInfo.identification.name + "|"
				+ userInfo.identification.idCard + "|"
				+ "0|" +mchnt_key);
		Bundle bundle =new Bundle();
		bundle.putString(AppConfig.MCHNT_CD, mchnt_cd);
		bundle.putString(AppConfig.MCHNT_AMT, amount);
		bundle.putString(AppConfig.MCHNT_BACK_URL, (MyContacts.BASE_URL+userInfo.backUrl));
		bundle.putString(AppConfig.MCHNT_BANK_NUMBER, bankCard.bankCardNum);
		bundle.putString(AppConfig.MCHNT_ORDER_ID, mchntOrdId);
		bundle.putString(AppConfig.MCHNT_USER_IDCARD_TYPE,"0");
		bundle.putString(AppConfig.MCHNT_USER_ID,userInfo.account.accountId+"");
		bundle.putString(AppConfig.MCHNT_USER_IDNU,userInfo.identification.idCard);
		bundle.putString(AppConfig.MCHNT_USER_NAME,userInfo.identification.name);
		bundle.putString(AppConfig.MCHNT_SING_KEY,Sing);
		bundle.putString(AppConfig.MCHNT_SDK_SIGNTP,"MD5");
		bundle.putString(AppConfig.MCHNT_SDK_TYPE,"02");
		bundle.putString(AppConfig.MCHNT_SDK_VERSION,"2.0");

		int i= FyPay.pay(this, bundle, new FyPayCallBack() {

			@Override
			public void onPayComplete(String arg0, String arg1, Bundle arg2) {

			}

			@Override
			public void onPayBackMessage(String msg) {
				// TODO Auto-generated method stub
				Log.e("fuiou", "----------extraData:"+msg.toString());

				String ser = "<RESPONSECODE>";
				int start = msg.indexOf(ser);

				if(start < 0) return;

				String code = msg.substring(start+ser.length(), start+ser.length()+4);

				if(code.equals("0000")){

					notifationJiangliE(jlNoti);
					showToast("支付成功");

					EventBus.getDefault().post(new String("success"));

					finish();
				}else{
					showToast("支付失败");
				}
			}
		});

	}

	/***
	 * 获取所有银行银行卡
	 */
	private void getBankCardList(){
		String formatUrl = String.format(ServerInterface.GET_BANK_CARD_LIST,userInfo.account.cellphone);
		requestGet(formatUrl.hashCode(), formatUrl, GetBankCardResponse.class, new HttpListener<GetBankCardResponse>() {
			@Override
			public void onSucceed(int what, Response<GetBankCardResponse> response) {


				if(response.isSucceed() && response.get() != null){
					List<BankCard> result = response.get().result;
					DialogUtils.getInstance().selectBankCard(RefundActivity.this, result, new DialogCallback() {
						@Override
						public void typeStr(String type) {
							if(TextUtils.isEmpty(type)){
								showToast("请选择银行卡!");
								return;
							}
							BankCard bc = new BankCard();
							bc.bankName = type.split(",")[0];
							bc.bankCardNum = type.split(",")[1];
							getOrderNumber(bc);
						}
					});

				}
			}

			@Override
			public void onFailed(int what, Response<GetBankCardResponse> response) {

			}
		},true);
	}

	private void notifationJiangliE(final int jl){
		String formatUrl = String.format(ServerInterface.NOTIFATION_JIAANGLIJIN_E,
				userInfo.account.cellphone,br.borrowId,jl,userInfo.token);
		requestPut(formatUrl.hashCode(), null,formatUrl,BooleanResponse.class,new HttpListener<BooleanResponse>(){
			@Override
			public void onSucceed(int what, Response<BooleanResponse> response) {
				if(response.isSucceed() && response.get() != null){
					userInfo.jiangLiJinE = userInfo.jiangLiJinE - jl;
				}
			}

			@Override
			public void onFailed(int what, Response<BooleanResponse> response) {

			}
		},false);
	}
}
