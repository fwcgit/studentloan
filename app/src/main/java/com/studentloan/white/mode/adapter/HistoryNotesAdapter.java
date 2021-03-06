package com.studentloan.white.mode.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.studentloan.white.R;
import com.studentloan.white.net.data.Borrow;
import com.studentloan.white.utils.ConvertUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressLint("InflateParams")
public class HistoryNotesAdapter extends BaseAdapter {
	private Context context;
	private List<Borrow> list = new ArrayList<Borrow>();

	public HistoryNotesAdapter(Context context) {
		super();
		this.context = context;
	}

	@Override
	public int getCount() {
		return list == null ? 0 : list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder h = null;
		if(convertView == null){
			h = new Holder();
			convertView = LayoutInflater.from(context).inflate(R.layout.item_history_notes_layout, null);
			h.hyPriceTv = (TextView) convertView.findViewById(R.id.hyPriceTv);
			h.jkPriceTv = (TextView) convertView.findViewById(R.id.jkPriceTv);
			h.jkDateTv = (TextView) convertView.findViewById(R.id.jkDateTv);
			h.hkDateTv = (TextView) convertView.findViewById(R.id.hkDateTv);
			h.sjhkDateTv = (TextView) convertView.findViewById(R.id.sjhkDateTv);
			h.statusTv = (TextView) convertView.findViewById(R.id.statusTv);
			convertView.setTag(h);
		}else{
			h = (Holder) convertView.getTag();
		}


		Borrow borrow = list.get(position);

		h.hyPriceTv.setText(borrow.huanKuanJinE+" 元");
		h.jkPriceTv.setText("借款金额："+borrow.jieKuanJinE+" 元");
		h.jkDateTv.setText("借款日期："+ ConvertUtils.dateTimeToStr(new Date(borrow.jieKuanRiQi),"yyyy年MM月dd日"));

		h.sjhkDateTv.setVisibility(View.GONE);
		h.hkDateTv.setVisibility(View.VISIBLE);

		if(borrow.huanKuanDeadline != null){
			h.hkDateTv.setText("应还款日期："+ConvertUtils.dateTimeToStr(new Date(borrow.huanKuanDeadline),"yyyy年MM月dd日"));
		}

		if(borrow.jieKuanZhuangTai < 0){

			h.statusTv.setText("借款失败");
			h.hkDateTv.setVisibility(View.GONE);
			h.sjhkDateTv.setVisibility(View.GONE);
			h.statusTv.setTextColor(0xffff0000);

		}else if(borrow.jieKuanZhuangTai == 0 || borrow.jieKuanZhuangTai == 1){

			h.statusTv.setText("等待放款");
			h.hkDateTv.setVisibility(View.GONE);
			h.statusTv.setTextColor(0xffa2a2a2);

		}else if(borrow.jieKuanZhuangTai == 2){
			h.hyPriceTv.setText(borrow.yingHuanKuanJinE+" 元");
			h.statusTv.setText("立即还款>");
			h.statusTv.setTextColor(0xff27aa29);

		}else if(borrow.jieKuanZhuangTai == 3){
			//h.hyPriceTv.setText(borrow.+" 元");
			h.statusTv.setText("已还清");
			h.statusTv.setTextColor(0xffa2a2a2);
			h.sjhkDateTv.setVisibility(View.VISIBLE);
			h.sjhkDateTv.setText("实际还款日期："+ConvertUtils.dateTimeToStr(new Date(borrow.huanKuanRiQi),"yyyy年MM月dd日"));

		}

		return convertView;
	}
	
	public class Holder{
		TextView hyPriceTv,jkPriceTv,jkDateTv,hkDateTv,sjhkDateTv,statusTv;
	}
	
	public void setHistoryList(List<Borrow> list,boolean ref) {
		if(ref){
			this.list = list;
		}else{
			this.list.addAll(list);
		}
		
		notifyDataSetChanged();
	}
}
