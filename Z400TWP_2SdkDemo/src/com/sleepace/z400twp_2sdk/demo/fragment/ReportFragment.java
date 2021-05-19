package com.sleepace.z400twp_2sdk.demo.fragment;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.manager.DeviceType;
import com.sleepace.sdk.util.SdkLog;
import com.sleepace.sdk.util.TimeUtil;
import com.sleepace.sdk.wifidevice.WiFiDeviceSdkHelper;
import com.sleepace.sdk.wifidevice.bean.ResponseData;
import com.sleepace.sdk.z400twp_2.constants.SleepConfig;
import com.sleepace.sdk.z400twp_2.constants.SleepStatusType;
import com.sleepace.sdk.z400twp_2.domain.Analysis;
import com.sleepace.sdk.z400twp_2.domain.Detail;
import com.sleepace.sdk.z400twp_2.domain.HistoryData;
import com.sleepace.sdk.z400twp_2.domain.HistoryDataList;
import com.sleepace.sdk.z400twp_2.domain.Summary;
import com.sleepace.z400twp_2sdk.demo.R;
import com.sleepace.z400twp_2sdk.demo.bean.CvPoint;
import com.sleepace.z400twp_2sdk.demo.util.DensityUtil;
import com.sleepace.z400twp_2sdk.demo.view.graphview.GraphView;
import com.sleepace.z400twp_2sdk.demo.view.graphview.GraphViewSeries;
import com.sleepace.z400twp_2sdk.demo.view.graphview.GraphViewStyle;
import com.sleepace.z400twp_2sdk.demo.view.graphview.LineGraphView;
import com.sleepace.z400twp_2sdk.demo.view.graphview.LineGraphView.BedBean;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ReportFragment extends BaseFragment {

	private LayoutInflater inflater;
	private LinearLayout reportLayout;
	private Button btnShort, btnLong, btnLastReport;
	private HistoryData shortData, longData;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	private DateFormat timeFormat = new SimpleDateFormat("HH:mm");
	private ProgressDialog progressDialog;
	private WiFiDeviceSdkHelper wifiDeviceSdkHelper;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		this.inflater = inflater;
		View view = inflater.inflate(R.layout.fragment_data, null);
//		SdkLog.log(TAG + " onCreateView-----------");
		wifiDeviceSdkHelper = WiFiDeviceSdkHelper.getInstance(mActivity.getApplicationContext());
		findView(view);
		initListener();
		initUI();
		return view;
	}

	protected void findView(View root) {
		// TODO Auto-generated method stub
		super.findView(root);
		btnShort = (Button) root.findViewById(R.id.btn_sleep_short);
		btnLong = (Button) root.findViewById(R.id.btn_sleep_long);
		btnLastReport = (Button) root.findViewById(R.id.btn_last_report);
		reportLayout = (LinearLayout) root.findViewById(R.id.layout_chart);
	}

	protected void initListener() {
		// TODO Auto-generated method stub
		super.initListener();
		btnShort.setOnClickListener(this);
		btnLong.setOnClickListener(this);
		btnLastReport.setOnClickListener(this);
	}

	protected void initUI() {
		// TODO Auto-generated method stub
		mActivity.setTitle(R.string.report);
		initDemoData();
		
		progressDialog = new ProgressDialog(mActivity);
		progressDialog.setIcon(android.R.drawable.ic_dialog_info);
		progressDialog.setMessage(getString(R.string.tips_loading));
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
	}
	
	
	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		if (v == btnShort) {
			initShortReportView(shortData);
		} else if (v == btnLong) {
			initLongReportView(longData);
		} else if(v == btnLastReport) {
			progressDialog.show();
			int startTime = (int) (System.currentTimeMillis() / 1000);
//			int startTime = 1594310399;
			HashMap<String, Object> args = new HashMap<String, Object>();
			args.put("startTime", startTime);
			args.put("num", 1);
			args.put("order", 0);
			wifiDeviceSdkHelper.getSleepReport(args, new IResultCallback() {
				@Override
				public void onResultCallback(final CallbackData cd) {
					// TODO Auto-generated method stub
					SdkLog.log(TAG+" getSleepReport cd:" + cd);
					mActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							progressDialog.dismiss();
							reportLayout.removeAllViews();
							if(cd.isSuccess()) {
								Gson gson = new Gson();
								Object obj = cd.getResult();
								String str = gson.toJson(obj);
								HistoryDataList dataList = gson.fromJson(str, HistoryDataList.class);
								SdkLog.log(TAG+" getSleepReport dataList:" + dataList);
								List<HistoryData> list = null;
								if(dataList != null) {
									list = dataList.getHistory();
								}
								if(list != null && list.size() > 0) {
									HistoryData historyData = list.get(0);
									Analysis analysis = historyData.getAnalysis();
									if(analysis.getReportFlag() == 1) {//长报告
										initLongReportView(historyData);
									}else if(analysis.getReportFlag() == 2){//短报告
										initShortReportView(historyData);
									}
								}
							}else {
								Toast.makeText(mActivity, R.string.failure, Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
			});
		}
	}
	
	private void initDemoData() {
		// TODO Auto-generated method stub
		shortData = createShortReportData(1591072247, 183);
		longData = createLongReportData(1590678829, 491);
	}

	private void initShortReportView(HistoryData historyData) {
		reportLayout.removeAllViews();
		
		Analysis analysis = historyData.getAnalysis();
		
		View view = inflater.inflate(R.layout.layout_short_report, null);
		TextView tvCollectDate = (TextView) view.findViewById(R.id.tv_collect_date);
		TextView tvSleepTime = (TextView) view.findViewById(R.id.tv_sleep_time);
		TextView tvSleepDuration = (TextView) view.findViewById(R.id.tv_sleep_duration);
		TextView tvAvgHeartRate = (TextView) view.findViewById(R.id.tv_avg_heartrate);
		TextView tvAvgBreathRate = (TextView) view.findViewById(R.id.tv_avg_breathrate);
		if (analysis != null) {
			int starttime = analysis.getFallsleepTimeStamp();
			int endtime = starttime + analysis.getDuration() * 60;
			tvCollectDate.setText(dateFormat.format(new Date(starttime * 1000l)));
			tvSleepTime.setText(timeFormat.format(new Date(starttime * 1000l)) + "(" + getString(R.string.starting_point) + ")-"
					+ timeFormat.format(new Date(endtime * 1000l)) + "(" + getString(R.string.end_point) + ")");
			int duration = historyData.getSummary().getRecordCount();
			int hour = duration / 60;
			int minute = duration % 60;
			tvSleepDuration.setText(hour + getString(R.string.unit_h) + minute + getString(R.string.unit_m));
			tvAvgHeartRate.setText(analysis.getAverageHeartBeatRate() + getString(R.string.unit_heart));
			tvAvgBreathRate.setText(analysis.getAverageBreathRate() + getString(R.string.unit_respiration));
		}
		reportLayout.addView(view);
	}

	private void initLongReportView(HistoryData historyData) {
		reportLayout.removeAllViews();
		
		Analysis analysis = historyData.getAnalysis();
		
		View view = inflater.inflate(R.layout.layout_long_report, null);
		LinearLayout mainGraph = (LinearLayout) view.findViewById(R.id.layout_chart);
		GraphView.GraphViewData[] mainData = getSleepGraphData(historyData.getDetail(), historyData.getAnalysis(), 60, DeviceType.DEVICE_TYPE_Z2);

		int think = (int) (DensityUtil.dip2px(mActivity, 1) * 0.8);
		final LineGraphView main_graph = new LineGraphView(mActivity, "");
		if (mainData == null) {
			mainData = new GraphView.GraphViewData[0];
		}

		GraphViewSeries series = new GraphViewSeries("", new GraphViewSeries.GraphViewSeriesStyle(getResources().getColor(R.color.COLOR_2), think), mainData);
		main_graph.addSeries(series);
		main_graph.isMySelft = true;
		if (mainData.length > 0) {
			main_graph.setViewPort(mainData[0].getX(), mainData[mainData.length - 1].getX());
		} else {
			main_graph.setViewPort(0, 10);
		}

		main_graph.setMinMaxY(-3, 2);
		main_graph.setVerticalLabels(
				new String[] { "", getString(R.string.wake_), getString(R.string.light_), getString(R.string.mid_), getString(R.string.deep_), "" });

		main_graph.setBeginAndOffset(historyData.getSummary().getStartTime(), TimeUtil.getTimeZoneSecond(), 0);
		main_graph.setScalable(false);
		main_graph.setScrollable(false);
		main_graph.setShowLegend(false);
		main_graph.setMainPoint(points);
		main_graph.setDrawBackground(true);
		main_graph.testVLabel = "wake";
		main_graph.setPauseData(apneaPauseList, heartPauseList);

		// 说明没有 数据
		if (mainData.length == 0) {
			main_graph.setHorizontalLabels(new String[] { "1", "2", "3", "4", "5", "6", "7" });
		}

		GraphViewStyle gvs = main_graph.getGraphViewStyle();
		gvs.setVerticalLabelsAlign(Paint.Align.CENTER);
		gvs.setTextSize(DensityUtil.sp2px(mActivity, 12));
		gvs.setGridColor(Color.parseColor("#668492a6"));
		gvs.setHorizontalLabelsColor(getResources().getColor(R.color.COLOR_3));
		gvs.setVerticalLabelsColor(getResources().getColor(R.color.COLOR_3));
		gvs.setLegendBorder(DensityUtil.dip2px(mActivity, 12));
		gvs.setNumVerticalLabels(4);
		gvs.setVerticalLabelsWidth(DensityUtil.dip2px(mActivity, 40));
		gvs.setNumHorizontalLabels(7);
		gvs.setLegendWidth(DensityUtil.dip2px(mActivity, 30));
		main_graph.setBedBeans(bedBeans);
		main_graph.setSleepUpIn(SleepInUP);
		mainGraph.removeAllViews();
		mainGraph.addView(main_graph);
		// main_graph.setOnHeartClickListener(heartClick);
//		main_graph.setOnGraphViewScrollListener(new GraphView.OnGraphViewScrollListener() {
//			@Override
//			public void onTouchEvent(MotionEvent event, GraphView v) {
//				main_graph.onMyTouchEvent(event);
//			}
//		});
		// main_graph.setTouchDisallowByParent(true);

		TextView tvCollectDate = (TextView) view.findViewById(R.id.tv_collect_date);
		TextView tvSleepScore = (TextView) view.findViewById(R.id.tv_sleep_score);
		LinearLayout layoutDeductionPoints = (LinearLayout) view.findViewById(R.id.layout_deduction_points);
		TextView tvSleepTime = (TextView) view.findViewById(R.id.tv_sleep_time);
		TextView tvSleepDuration = (TextView) view.findViewById(R.id.tv_sleep_duration);
		TextView tvAsleepDuration = (TextView) view.findViewById(R.id.tv_fall_asleep_duration);
		TextView tvAvgHeartRate = (TextView) view.findViewById(R.id.tv_avg_heartrate);
		TextView tvAvgBreathRate = (TextView) view.findViewById(R.id.tv_avg_breathrate);
		TextView tvBreathPause = (TextView) view.findViewById(R.id.tv_respiration_pause);
		TextView tvDeepSleepPer = (TextView) view.findViewById(R.id.tv_deep_sleep_proportion);
		TextView tvMidSleepPer = (TextView) view.findViewById(R.id.tv_medium_sleep_proportion);
		TextView tvLightSleepPer = (TextView) view.findViewById(R.id.tv_light_sleep_proportion);
		TextView tvWakeSleepPer = (TextView) view.findViewById(R.id.tv_Sober_proportion);
		TextView tvWakeTimes = (TextView) view.findViewById(R.id.tv_wake_times);
		TextView tvTurnTimes = (TextView) view.findViewById(R.id.tv_turn_times);
		TextView tvBodyTimes = (TextView) view.findViewById(R.id.tv_Body_times);
		TextView tvOutTimes = (TextView) view.findViewById(R.id.tv_out_times);

		if (analysis != null) {
			tvCollectDate.setText(dateFormat.format(new Date(historyData.getSummary().getStartTime() * 1000l)));
			tvSleepScore.setText(String.valueOf(analysis.getSleepScore()));

			int fallSleep = analysis.getFallsleepTimeStamp();
			int wakeUp = analysis.getWakeupTimeStamp();
			tvSleepTime.setText(timeFormat.format(new Date(fallSleep * 1000l)) + "(" + getString(R.string.asleep_point) + ")-"
					+ timeFormat.format(new Date(wakeUp * 1000l)) + "(" + getString(R.string.awake_point) + ")");
			int duration = analysis.getDuration();
			int hour = duration / 60;
			int minute = duration % 60;
			tvSleepDuration.setText(hour + getString(R.string.unit_h) + minute + getString(R.string.unit_m));
			tvAvgHeartRate.setText(analysis.getAverageHeartBeatRate() + getString(R.string.unit_heart));
			tvAvgBreathRate.setText(analysis.getAverageBreathRate() + getString(R.string.unit_respiration));

			List<DeductItems> list = new ArrayList<DeductItems>();
			
			if (analysis.getMd_heart_high_decrease_scale() > 0 && analysis.getMd_heart_low_decrease_scale() > 0) {// 心率不齐
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.heartrate_not_near);
				item.score = analysis.getMd_heart_high_decrease_scale() + analysis.getMd_heart_low_decrease_scale();
				list.add(item);
			} else if (analysis.getMd_heart_high_decrease_scale() > 0) {// 心率过速
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.heartrate_too_fast);
				item.score = analysis.getMd_heart_high_decrease_scale();
				list.add(item);
			} else if (analysis.getMd_heart_low_decrease_scale() > 0) {// 心率过缓
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.heartrate_too_low);
				item.score = analysis.getMd_heart_low_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_breath_high_decrease_scale() > 0) {// 呼吸过速
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.deduction_breathe_fast);
				item.score = analysis.getMd_breath_high_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_breath_low_decrease_scale() > 0) {// 呼吸过缓
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.deduction_breathe_slow);
				item.score = analysis.getMd_breath_low_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_body_move_decrease_scale() != 0) {// 躁动不安
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.restless);
				item.score = analysis.getMd_body_move_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_leave_bed_decrease_scale() > 0) {// 起夜过多
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.up_night_more);
				item.score = analysis.getMd_leave_bed_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_sleep_time_increase_scale() > 0) {// 睡眠时间过长
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.actual_sleep_long);
				item.score = analysis.getMd_sleep_time_increase_scale();
				list.add(item);
			}

			if (analysis.getMd_sleep_time_decrease_scale() > 0) {// 睡眠时间过短
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.actual_sleep_short);
				item.score = analysis.getMd_sleep_time_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_perc_deep_decrease_scale() > 0) {// 深睡眠时间不足
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.deep_sleep_time_too_short);
				item.score = analysis.getMd_perc_deep_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_fall_asleep_time_decrease_scale() > 0) {// 入睡时间长
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.fall_asleep_hard);
				item.score = analysis.getMd_fall_asleep_time_decrease_scale();
				list.add(item);
			}

			if (analysis.getBreathPauseTimes() > 0 && analysis.getMd_breath_stop_decrease_scale() > 0) {// 呼吸暂停
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.abnormal_breathing);
				item.score = analysis.getMd_breath_stop_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_heart_stop_decrease_scale() > 0) {// 心跳骤停
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.heart_pause);
				item.score = analysis.getMd_heart_stop_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_start_time_decrease_scale() > 0) {// 上床时间较晚
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.start_sleep_time_too_latter);
				item.score = analysis.getMd_start_time_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_wake_cnt_decrease_scale() > 0) {// 清醒次数较多
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.wake_times_too_much);
				item.score = analysis.getMd_wake_cnt_decrease_scale();
				list.add(item);
			}

			if (analysis.getMd_perc_effective_sleep_decrease_scale() > 0) {// 良性睡眠扣分
				DeductItems item = new DeductItems();
				item.desc = getString(R.string.benign_sleep);
				item.score = analysis.getMd_perc_effective_sleep_decrease_scale();
				list.add(item);
			}

			int size = list.size();
			if(size > 0){
				for(int i=0;i<size;i++){
					View pointView = inflater.inflate(R.layout.layout_deduction_points, null);
					TextView tvDesc = (TextView) pointView.findViewById(R.id.tv_deduction_desc);
					TextView tvScore = (TextView) pointView.findViewById(R.id.tv_deduction_score);
					tvDesc.setText((i+1)+"."+list.get(i).desc);
					tvScore.setText("-" + Math.abs(list.get(i).score));
					layoutDeductionPoints.addView(pointView);
				}
			}
			
			hour = analysis.getFallAlseepAllTime() / 60;
			minute = analysis.getFallAlseepAllTime() % 60;
			tvAsleepDuration.setText(hour + getString(R.string.unit_h) + minute + getString(R.string.unit_m));
			int idx = 0;
			StringBuffer sb = new StringBuffer();
			if (analysis.getBreathPauseTimes() > 0) {
				idx = 0;
				int stime = historyData.getSummary().getStartTime();
				Detail detail = historyData.getDetail();
				int[] status = detail.getStatus();
				int len = status.length;
				for (int i = 0; i < len; i++) {
					if (analysis.getBreathRateStatusAry()[i] > 0) {
						idx++;
						sb.append(getString(R.string.sequence, String.valueOf(idx)) + "\t\t\t");
						int time = stime + i * 60;
						sb.append(timeFormat.format(new Date(time * 1000l)) + "\t\t\t");
						sb.append(analysis.getBreathRateStatusAry()[i] + getString(R.string.unit_s) + "\n");
					}
				}

				if (sb.length() > 0) {
					sb.delete(sb.lastIndexOf("\n"), sb.length());
				}
				tvBreathPause.setText(sb.toString());
			} else {
				tvBreathPause.setText(R.string.nothing);
			}

			tvDeepSleepPer.setText(analysis.getDeepSleepPerc() + "%");
			tvMidSleepPer.setText(analysis.getInSleepPerc() + "%");
			tvLightSleepPer.setText(analysis.getLightSleepPerc() + "%");
			tvWakeSleepPer.setText(analysis.getWakeSleepPerc() + "%");
			tvWakeTimes.setText(analysis.getWakeTimes() + getString(R.string.unit_times));
			tvTurnTimes.setText(analysis.getTrunOverTimes() + getString(R.string.unit_times));
			tvBodyTimes.setText(analysis.getBodyMovementTimes() + getString(R.string.unit_times));
			tvOutTimes.setText(analysis.getLeaveBedTimes() + getString(R.string.unit_times));
		}

		reportLayout.addView(view);

	}
	
	
	class DeductItems {
		String desc;
		int score;
	}
	

	private HistoryData createShortReportData(int starttime, int count) {
		HistoryData historyData = new HistoryData();
		Summary summ = new Summary();
		summ.setStartTime(starttime);
		summ.setRecordCount(count);
		historyData.setSummary(summ);

//		Detail detail = new Detail();
//		int[] heartRate = new int[] { 60, 62, 64, 62, 64, 63, 66, 68, 68, 68, 68, 59, 64, 64, 65, 63, 67, 67, 67, 63, 69, 70, 71, 72, 68, 70, 72, 71, 71, 69,
//				71, 66, 65, 67, 68, 65, 63, 62, 70, 66, 65, 57, 65, 66, 64, 68, 67, 66, 65, 67, 68, 66, 68, 68, 68, 66, 68, 66, 67, 67, 68, 67, 67, 67, 66, 68,
//				67, 67, 67, 66, 67, 69, 69, 63, 73, 69, 74, 71, 72, 74, 74, 75, 74, 73, 73, 72, 76, 72, 70, 70, 72, 73, 73, 68, 70, 71, 66, 70, 74, 73, 76, 67,
//				72, 71, 65, 65, 65, 71, 69, 64, 68, 65, 64, 67, 66, 61, 60, 65, 66, 68, 67, 60, 63, 64, 63, 66, 76, 76, 75, 79, 78, 67, 66, 67, 66, 70, 66, 64,
//				66, 72, 61, 64, 70, 64, 62, 66, 68, 73, 70, 66, 63, 61, 62, 72, 64, 74, 75, 72, 65, 71, 65, 58, 70, 74, 69, 74 };
//
//		int[] breathRate = new int[] { 12, 14, 14, 14, 14, 14, 14, 15, 14, 15, 15, 15, 14, 14, 14, 13, 13, 13, 14, 16, 13, 15, 15, 15, 14, 12, 14, 12, 12, 14,
//				14, 13, 11, 14, 13, 13, 14, 12, 11, 12, 13, 12, 12, 16, 16, 15, 14, 15, 14, 14, 15, 15, 15, 14, 14, 14, 14, 14, 15, 14, 15, 15, 15, 14, 15, 16,
//				15, 14, 15, 14, 15, 15, 15, 15, 13, 16, 13, 13, 12, 13, 13, 13, 12, 13, 12, 13, 14, 13, 13, 13, 14, 13, 12, 13, 12, 13, 13, 14, 15, 13, 15, 15,
//				16, 14, 19, 11, 12, 13, 12, 12, 13, 16, 17, 15, 14, 14, 16, 15, 15, 14, 13, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14, 15, 14, 13, 15, 14, 13,
//				13, 13, 13, 13, 13, 14, 14, 15, 15, 13, 14, 13, 16, 16, 16, 14, 15, 15, 12, 14, 16, 15, 13, 18, 20, 20, 18, 16 };
//
//		int[] status = new int[] { 72, 72, 72, 12, 8, 12, 78, 72, 72, 72, 78, 78, 72, 72, 72, 78, 72, 8, 8, 14, 8, 8, 8, 14, 8, 14, 8, 8, 8, 14, 14, 12, 78, 76,
//				72, 72, 76, 76, 78, 72, 8, 12, 78, 72, 78, 72, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 40, 46,
//				110, 104, 72, 78, 78, 72, 76, 72, 72, 72, 72, 8, 8, 8, 8, 8, 76, 72, 72, 72, 104, 104, 104, 104, 104, 104, 72, 72, 72, 78, 78, 78, 72, 72, 72,
//				72, 78, 76, 78, 78, 12, 12, 12, 8, 14, 14, 12, 12, 12, 12, 12, 12, 14, 14, 12, 14, 14, 14, 12, 12, 14, 12, 12, 12, 12, 8, 12, 8, 12, 12, 12, 8,
//				14, 12, 8, 14, 8, 12, 12, 14, 14, 12, 14, 72, 76, 78, 76, 76, 12, 14, 12, 8, 12 };
//		int[] statusValue = new int[] { 0, 0, 0, 1, 0, 1, 1, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, 1, 3, 1, 1, 0, 0, 1, 1, 2, 0,
//				0, 3, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 0, 1, 1, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0,
//				0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 2, 0, 0, 0, 0, 1, 4, 1, 1, 5, 3, 4, 0, 1, 3, 3, 3, 5, 5, 5, 3, 1, 1, 8, 1, 1, 2, 2, 1, 1, 4, 2,
//				1, 1, 0, 1, 0, 3, 3, 1, 0, 1, 6, 0, 1, 0, 7, 8, 3, 1, 3, 1, 0, 1, 1, 5, 1, 1, 2, 2, 0, 1 };
//
//		detail.setHeartRate(heartRate);
//		detail.setBreathRate(breathRate);
//		detail.setStatus(status);
//		detail.setStatusValue(statusValue);
//		historyData.setDetail(detail);

		Analysis analysis = new Analysis();
		analysis.setReportFlag(2); //短报告
		analysis.setFallsleepTimeStamp(1591073687); //入睡时间戳
		analysis.setFallAlseepAllTime(24); //入睡时长
		analysis.setDuration(34); //睡眠时长
		analysis.setAverageHeartBeatRate(68); //平均心率
		analysis.setAverageBreathRate(18); //平均呼吸
		
		historyData.setAnalysis(analysis);
		return historyData;
	}

	
	
	private HistoryData createLongReportData(int starttime, int count) {
		HistoryData historyData = new HistoryData();
		Summary summ = new Summary();
		summ.setStartTime(starttime);
		summ.setRecordCount(count);
		historyData.setSummary(summ);

		Detail detail = new Detail();
		int[] heartRate = new int[] {61,61,68,70,65,69,62,65,67,66,75,72,69,69,65,63,65,67,70,74,72,71,70,71,72,73,75,71,75,75,72,73,71,73,70,74,71,69,70,70,74,64,71,70,69,68,71,70,73,69,73,71,71,70,71,72,69,64,69,72,63,66,72,74,75,74,70,74,65,70,69,64,64,56,64,68,66,67,58,67,67,70,67,70,66,67,75,75,75,73,75,74,73,74,72,73,74,74,73,73,73,73,73,71,73,72,70,71,71,72,71,69,71,68,71,68,69,66,71,71,66,72,68,71,72,71,70,69,66,69,70,72,70,73,69,68,69,69,67,70,69,68,70,71,73,72,72,72,75,75,74,75,75,75,74,75,76,71,73,74,71,72,72,69,68,72,70,71,72,72,70,71,72,72,70,67,67,65,71,68,67,67,67,69,68,70,69,70,64,67,68,67,65,68,67,72,68,68,69,65,66,64,57,62,61,57,63,66,66,68,67,68,74,67,65,67,68,68,66,67,69,68,63,69,68,67,63,68,69,68,70,73,65,70,70,68,69,74,68,72,66,57,66,64,73,72,74,71,65,63,60,56,66,73,73,71,72,68,74,77,74,72,75,74,76,77,74,76,72,72,74,74,74,72,72,72,73,72,69,68,68,67,64,68,66,67,66,67,66,66,67,65,68,66,65,66,68,67,68,67,65,67,66,67,65,67,65,63,64,66,68,68,68,66,64,65,67,67,67,67,67,66,65,66,66,66,65,66,67,64,64,65,67,65,69,66,66,70,70,66,60,65,65,67,67,66,71,69,71,68,65,68,72,68,65,67,68,70,74,69,69,71,70,72,71,69,69,70,68,70,70,67,71,68,69,69,70,70,69,70,70,69,70,70,70,69,69,70,70,70,69,70,70,70,65,73,64,64,69,71,70,68,68,68,71,69,67,68,69,67,65,65,66,65,64,68,68,67,60,62,63,57,69,68,68,71,66,65,66,66,71,71,66,66,64,61,66,61,64,65,60,63,63,59,64,63,68,62,63,64,63,65,66,62,68,65,66,67,68,68,71,70,71,68,68,72,69,72,69,69,69,71,70,68,70,70,70,71,71,69,70,71,69,70,69,68,70,64,64,65,64};

		int[] breathRate = new int[] {13,13,12,16,16,14,13,15,15,17,20,19,20,19,20,20,20,18,19,19,20,20,20,21,21,21,21,21,21,21,21,21,21,22,21,21,21,22,22,21,21,21,21,21,21,22,22,21,21,20,21,21,21,22,21,20,21,21,20,21,20,20,21,20,21,20,22,22,20,21,20,20,20,20,20,21,20,21,20,17,15,13,12,12,13,14,19,19,20,19,20,20,20,20,21,19,19,21,20,21,21,21,21,19,20,20,20,20,21,21,21,21,21,21,21,21,21,20,21,21,21,20,20,20,21,21,20,21,20,21,21,21,20,21,20,19,19,19,17,19,19,19,18,20,19,17,20,21,20,21,20,20,20,19,19,19,18,20,19,19,20,20,20,19,19,19,18,19,20,20,21,20,20,20,19,16,16,19,18,19,19,18,19,19,19,20,20,20,20,20,20,20,20,20,20,20,20,20,20,19,20,20,19,19,20,20,20,20,20,19,20,20,20,19,21,19,18,19,19,19,20,19,18,19,20,18,16,16,19,22,19,20,21,17,17,17,20,17,16,21,15,19,19,21,20,21,22,20,19,14,16,18,17,21,24,20,23,22,21,22,22,23,20,19,19,19,19,19,19,20,19,20,20,20,20,19,20,20,15,18,19,15,18,17,19,19,19,19,19,19,20,20,20,20,20,20,20,19,20,20,20,19,20,19,19,20,20,20,19,19,20,19,18,19,19,19,19,20,19,17,16,18,19,19,18,18,18,20,20,17,19,20,20,20,20,19,21,20,20,21,19,21,22,21,19,20,21,20,21,19,21,22,13,22,18,19,17,19,19,19,18,18,18,22,19,18,19,19,19,19,20,20,20,19,19,20,20,19,19,20,20,20,20,20,20,20,20,21,20,20,20,20,20,19,20,20,20,20,19,19,17,18,17,18,16,18,18,18,19,18,13,14,19,19,17,18,19,19,19,19,19,21,18,18,18,19,18,18,20,20,20,17,20,23,22,21,21,20,21,21,18,21,20,24,24,20,19,20,22,21,20,20,23,22,21,20,19,17,20,21,20,20,19,21,20,19,20,21,20,18,18,19,20,20,19,19,20,19,19,20,20,20,21,21,21,21,20,20,19,19,19};

		int[] status = new int[] {6,6,6,6,6,6,6,6,6,0,0,6,4,0,0,0,0,6,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,6,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,6,0,0,0,0,0,6,6,0,0,0,6,0,0,0,0,6,0,0,0,0,0,0,0,0,6,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,6,4,4,6,4,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,6,6,0,6,0,0,0,0,6,0,0,0,6,6,0,0,6,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,6,0,0,0,4,0,6,0,0,0,0,6,0,0,0,0,0,6,0,6,0,6,0,0,0,0,6,4,0,0,6,6,0,0,0,0,0,4,0,0,4,0,0,4,6,6,4,0,0,6,0,0,6,0,6,0,0,6,0,0,6,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,6,0,0,6,6,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,6,0,0,0,0,0,0,6,6,0,6,0,0,0,6,0,0,6,0,0,6,0,0,0,4,0,0,0,0,0,0,0,0,0,0,6,0,0,0,6,0,0,0,0,0,6,0,6,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,6,0,0,0,6,0,6,0,0,0,4,6,0,0,0,0,6,6,0,0,0,4,0,0,0,0,0,0,6,0,0,0,6,0,0,0,0,0,6,0,0,0,0,0,0,0,0,6,0,0,0,0,6,0,0,0,0,0,0,0,0,0,0,6,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,6,0,0,0,0,0,0,0,6,0,0,0,4};

		int[] statusValue = new int[] {1,1,2,1,2,1,1,1,1,0,0,1,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,2,1,0,0,0,1,0,0,0,0,2,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,1,0,1,0,0,0,0,1,0,0,0,1,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,1,0,0,0,0,1,0,0,0,0,0,2,0,1,0,1,0,0,0,0,1,1,0,0,1,1,0,0,0,0,0,4,0,0,1,0,0,1,1,1,1,0,0,1,0,0,1,0,1,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,1,0,2,0,0,0,1,0,0,1,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,1,0,1,0,0,0,1,1,0,0,0,0,1,1,0,0,0,1,0,0,0,0,0,0,1,0,0,0,2,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,1};

		detail.setHeartRate(heartRate);
		detail.setBreathRate(breathRate);
		detail.setStatus(status);
		detail.setStatusValue(statusValue);
		historyData.setDetail(detail);
		
//		Analysis analysis = AnalysisUtil.analysData(summ, detail, 0, DeviceType.DEVICE_TYPE_Z2);
//		SdkLog.log(TAG+" createLongReportData analysis:" + analysis.getAlgorithmVer());
		
		Analysis analysis = new Analysis();
		analysis.setReportFlag(1); //长报告
		analysis.setFallsleepTimeStamp(1590679549);
		analysis.setFallAlseepAllTime(12);
		analysis.setDuration(473);
//		analysis.setWake(18);
		analysis.setSleepScore(80);
		analysis.setLightSleepAllTime(162);
		analysis.setInSleepAllTime(243);
		analysis.setDeepSleepAllTime(68);
		analysis.setDeepSleepPerc(13);
		analysis.setInSleepPerc(49);
		analysis.setLightSleepPerc(32);
		analysis.setWakeSleepPerc(6);
		analysis.setMaxBreathRate(24);
		analysis.setMinBreathRate(12);
		analysis.setAverageBreathRate(20);
		analysis.setBreathRateFastAllTime(7);
		analysis.setMaxHeartBeatRate(77);
		analysis.setMinHeartBeatRate(56);
		analysis.setAverageHeartBeatRate(69);
		analysis.setBodyMovementTimes(21);
		analysis.setTrunOverTimes(65);
		
		analysis.setMd_breath_high_decrease_scale((short)1); //呼吸过速扣分项
		analysis.setMd_body_move_decrease_scale((short)3); //躁动不安扣分项
		analysis.setMd_start_time_decrease_scale((short)1); //太晚睡觉扣分项
		analysis.setMd_wake_cnt_decrease_scale((short)8); //清醒次数扣分项
		analysis.setMd_perc_effective_sleep_decrease_scale((short)7); //良性睡眠扣分项
		
		int fallsleepTimeStamp = summ.getStartTime() + analysis.getFallAlseepAllTime() * 60;
		int wakeupTimeStamp = fallsleepTimeStamp + analysis.getDuration() * 60;
		
		analysis.setFallsleepTimeStamp(fallsleepTimeStamp);
		analysis.setWakeupTimeStamp(wakeupTimeStamp);
		
		analysis.setSleepCurveArray(new float[]{0.0f,-0.02390194f,-0.048755884f,-0.0092663765f,-0.030327082f,-0.022876978f,-0.009778738f,-0.015239954f,-0.015892982f,-0.03026104f,-0.023267984f,0.0f,0.41088653f,0.4838357f,0.5624666f,0.64701104f,0.73793054f,0.83545613f,0.9388273f,1.0477833f,1.1262244f,1.2055237f,1.2852142f,1.3655045f,1.4461082f,1.5266601f,1.6068257f,1.6861652f,1.7642564f,1.8406931f,1.9150833f,1.9868163f,2.0555234f,2.1218374f,2.1858997f,2.2477946f,2.307791f,2.3659873f,2.4218159f,2.4749823f,2.5255094f,2.5729349f,2.6172369f,2.6583753f,2.695948f,2.7298849f,2.7603347f,2.7874355f,2.8110838f,2.8308628f,2.8468835f,2.8592496f,2.8681924f,2.8739429f,2.8758907f,2.8742669f,2.8697643f,2.8626142f,2.8530471f,2.8403378f,2.824717f,2.8061848f,2.784741f,2.7606168f,2.734348f,2.703791f,2.6689448f,2.6298099f,2.5863864f,2.5402822f,2.4904256f,2.4368162f,2.3805265f,2.321556f,2.2609775f,2.1971824f,2.1301706f,2.0599422f,1.9864975f,1.909836f,1.8321024f,1.7532966f,1.6734186f,1.5924685f,1.5099101f,1.4268156f,1.3431852f,1.2579463f,1.1710991f,1.0826436f,0.9974048f,0.9164548f,0.83979344f,0.76742077f,0.69933677f,0.6366136f,0.58181906f,0.53495336f,0.49601603f,0.4650073f,0.44395876f,0.4342246f,0.43580508f,0.4486997f,0.47290897f,0.5052161f,0.54725766f,0.5990336f,0.6605439f,0.73314285f,0.81575847f,0.90582323f,1.003337f,1.1082995f,1.2193568f,1.3337998f,1.4495975f,1.565395f,1.6805153f,1.793604f,1.9046612f,2.0082695f,2.1028948f,2.1885366f,2.2638412f,2.328808f,2.3817236f,2.4225876f,2.4514f,2.4695153f,2.4776106f,2.4729352f,2.4568431f,2.4300117f,2.3929386f,2.3456233f,2.2907748f,2.2299273f,2.163081f,2.0919492f,2.0156758f,1.9368311f,1.8571296f,1.7757138f,1.6925844f,1.6094548f,1.5288961f,1.4517655f,1.3797771f,1.3146445f,1.2580822f,1.2083759f,1.1655257f,1.1295315f,1.0986793f,1.0721122f,1.047259f,1.0224059f,0.99840975f,0.97441363f,0.9487035f,0.92213655f,0.89385533f,0.86214614f,0.8261521f,0.7850158f,0.7404516f,0.69245934f,0.6427531f,0.5930469f,0.54334044f,0.49620533f,0.45078397f,0.40536284f,0.3616557f,0.31966257f,0.27938342f,0.24253201f,0.20910907f,0.17911386f,0.15254664f,0.13304996f,0.12062383f,0.11355424f,0.11376929f,0.12298298f,0.14398122f,0.17762065f,0.2256155f,0.28710866f,0.3639214f,0.4560535f,0.5617912f,0.6738498f,0.7922287f,0.9124291f,1.0308081f,1.145286f,1.2558626f,1.358378f,1.4523149f,1.5345482f,1.6040429f,1.6605402f,1.7040402f,1.7329803f,1.7468429f,1.7456282f,1.7366211f,1.7198215f,1.6988716f,1.673513f,1.6458257f,1.615292f,1.5824294f,1.5472379f,1.5089413f,1.4685745f,1.4279491f,1.3868061f,1.3448869f,1.3021914f,1.2589784f,1.2152479f,1.1720349f,1.1288218f,1.0863851f,1.0452423f,1.0061696f,0.96916676f,0.93423414f,0.90214777f,0.8729079f,0.8449619f,0.81882715f,0.79450345f,0.77173257f,0.75077295f,0.7326596f,0.7163577f,0.7023845f,0.6905534f,0.6803472f,0.67084503f,0.6637008f,0.6600938f,0.6601677f,0.66458416f,0.6733432f,0.68486285f,0.70013547f,0.71993756f,0.74441266f,0.77285695f,0.8052702f,0.842314f,0.8843188f,0.9312849f,0.9838736f,1.0401007f,1.0993044f,1.1621463f,1.2266421f,1.2914685f,1.3586103f,1.427075f,1.4968628f,1.5676428f,1.6390843f,1.7098643f,1.7793213f,1.8467938f,1.9119511f,1.9747932f,2.0356507f,2.094193f,2.1484356f,2.19904f,2.2469985f,2.2916493f,2.3329928f,2.3703673f,2.403442f,2.4322171f,2.4580154f,2.4808369f,2.5000203f,2.5165577f,2.5311105f,2.5430174f,2.5526092f,2.560547f,2.567162f,2.5734463f,2.5800614f,2.587007f,2.594945f,2.6032135f,2.6114824f,2.619751f,2.6254094f,2.629119f,2.6302183f,2.6280456f,2.6217432f,2.6113105f,2.5967479f,2.5768666f,2.5510044f,2.5185006f,2.4778352f,2.429008f,2.3726807f,2.309184f,2.237758f,2.1610131f,2.0789492f,1.9915664f,1.897345f,1.7962849f,1.6883862f,1.5751684f,1.459671f,1.342654f,1.2241174f,1.1071006f,0.9916034f,0.8776257f,0.76668763f,0.66106844f,0.5607681f,0.46864343f,0.38584852f,0.3139031f,0.25432658f,0.20827365f,0.17422414f,0.14950418f,0.13450837f,0.12981343f,0.13389993f,0.14792228f,0.17188f,0.20425367f,0.24352312f,0.29026628f,0.34047174f,0.39298534f,0.4478073f,0.50493765f,0.56437635f,0.62612295f,0.69133234f,0.75769544f,0.82463574f,0.89215326f,0.9579396f,1.0219947f,1.0843184f,1.1449109f,1.2043494f,1.2637879f,1.3243805f,1.3849732f,1.4455658f,1.5050043f,1.5632886f,1.6204188f,1.6787031f,1.7381415f,1.7981571f,1.8599038f,1.9233818f,1.988591f,2.054377f,2.1195862f,2.1842184f,2.2471192f,2.3082888f,2.3677273f,2.4242802f,2.4767938f,2.5252676f,2.5685482f,2.6060839f,2.636249f,2.6590433f,2.6744666f,2.682519f,2.684355f,2.6788723f,2.663868f,2.6393418f,2.6053464f,2.5618815f,2.5095506f,2.4495077f,2.381753f,2.3062863f,2.22476f,2.139378f,2.0501401f,1.9559444f,1.8578929f,1.7559853f,1.6535268f,1.5505176f,1.4469572f,1.3439479f,1.2431419f,1.1461921f,1.0541998f,0.9660635f,0.881783f,0.80025697f,0.7231376f,0.65262866f,0.58983135f,0.53364444f,0.48406744f,0.43952513f,0.40222073f,0.37340903f,0.35309076f,0.33961272f,0.33187366f,0.32814407f,0.33078074f,0.33978367f,0.35515332f,0.3739817f,0.3965757f,0.42293525f,0.45431566f,0.48946166f,0.5277457f,0.5679126f,0.6112175f,0.6576605f,0.7072413f,0.7593324f,0.81330657f,0.8679085f,0.92439294f,0.9827602f,1.0442656f,1.1063987f,1.167904f,1.2275265f,1.2865216f,1.3448888f,1.4045113f,1.464134f,1.5237564f,1.583379f,1.6436291f,1.7057621f,1.771033f,1.8369317f,1.9022027f,1.9668462f,2.030862f,2.0942502f,2.157011f,2.2170706f,2.2744288f,2.321446f,2.356866f,2.380471f,2.3922608f,2.3922355f,2.380395f,2.3590374f,2.328801f,2.2898407f,2.240669f,2.179567f,2.1044376f,1.9323946f,1.7599146f,1.5869977f,1.4206567f,1.2608914f,1.1079205f,0.948879f,0.0f,-0.003945112f,-0.03508711f,-0.040005922f,-0.015654087f,-0.03130102f});
		analysis.setSleepCurveStatusArray(new short[]{0,0,0,0,0,0,0,0,0,0,0,0,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,3,3,3,3,3,3,3,3,3,3,3,3,3,3,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,2,1,0,0,0,0,0,0});
		
		int len = summ.getRecordCount();
		int[] breathRateStatusAry = new int[len];
		int[] heartRateStatusAry = new int[len];
		int[] leftBedStatusAry = new int[len];
		int[] turnOverStatusAry = new int[len];
		for(int i=0; i<len; i++){
			byte state = (byte) (detail.getStatus()[i] & 7);
			if(state == SleepStatusType.SLEEP_B_STOP) { // 呼吸暂停点
				breathRateStatusAry[i] = detail.getStatusValue()[i];
			}else if (state == SleepStatusType.SLEEP_H_STOP) { // 心跳暂停点
				heartRateStatusAry[i] = detail.getStatusValue()[i];
			}else if (state == SleepStatusType.SLEEP_LEAVE && detail.getStatusValue()[i] > 0) { // 有效离床
				leftBedStatusAry[i] = detail.getStatusValue()[i];
			}else if (state == SleepStatusType.SLEEP_TURN_OVER) { // 翻身
				turnOverStatusAry[i] = detail.getStatusValue()[i];
			}
		}
		analysis.setBreathRateStatusAry(breathRateStatusAry);
		analysis.setHeartRateStatusAry(heartRateStatusAry);
		analysis.setLeftBedStatusAry(leftBedStatusAry);
		analysis.setTurnOverStatusAry(turnOverStatusAry);
		
		historyData.setAnalysis(analysis);
		return historyData;
	}

	private List<CvPoint> points = new ArrayList<CvPoint>();
	private List<LineGraphView.BedBean> bedBeans = new ArrayList<LineGraphView.BedBean>();
	private List<LineGraphView.BedBean> SleepInUP = new ArrayList<LineGraphView.BedBean>();
	/**
	 * 描述：呼吸暂停的集合
	 */
	private List<GraphView.GraphViewData> apneaPauseList = new ArrayList<GraphView.GraphViewData>();
	/**
	 * 描述：心跳暂停的集合
	 */
	private List<GraphView.GraphViewData> heartPauseList = new ArrayList<GraphView.GraphViewData>();

	/**
	 * 新的睡眠曲线中离床的起始点，单位是分钟
	 */
	private int leaveBedStart = 0;

	
	/**
	 * <h3>新版 算出 睡眠周期图的数据结构</h3>
	 * 
	 * @param analysis
	 * @param timeStep
	 * @return
	 */
	private GraphView.GraphViewData[] getNewSleepGraphData(Detail detail, Analysis analysis, int timeStep, DeviceType deviceType) {
		GraphView.GraphViewData[] mainData = new GraphView.GraphViewData[analysis.getSleepCurveArray().length + 1];
		// 是手机监测的新版
		for (int i = 0; i < analysis.getSleepCurveArray().length; i++) {
			// 清醒，潜睡，中睡，深睡 手机给的是 0,1,2,3； ron画图的列表是: 1,0,-1,-2
			mainData[i] = new GraphView.GraphViewData(i * timeStep, 1 - analysis.getSleepCurveArray()[i]);
		}

		mainData[analysis.getSleepCurveArray().length] = new GraphView.GraphViewData(analysis.getSleepCurveArray().length * timeStep, 1);
		SleepInUP.clear();
		heartPauseList.clear();
		apneaPauseList.clear();
		bedBeans.clear();
		if (analysis.getSleepCurveStatusArray() != null && analysis.getSleepCurveStatusArray().length > 0) {
			for (int i = 0; i < analysis.getSleepCurveStatusArray().length; i++) {

				if ((analysis.getSleepCurveStatusArray()[i] & SleepConfig.NewSleepInPoint) == SleepConfig.NewSleepInPoint) { // 入睡点
					LineGraphView.BedBean sleepIn = new LineGraphView.BedBean();
					sleepIn.setData(new GraphView.GraphViewData(i * timeStep, 0));
					sleepIn.setX(i * timeStep);
					sleepIn.setStatus(BedBean.SLEEPIN);
					sleepIn.setY(0);
					SleepInUP.add(sleepIn);
				}

				if ((analysis.getSleepCurveStatusArray()[i] & SleepConfig.NewWakeUpPoint) == SleepConfig.NewWakeUpPoint) { // 清醒点
					LineGraphView.BedBean waleUp = new LineGraphView.BedBean();
					waleUp.setData(new GraphView.GraphViewData(i * timeStep, 0));
					waleUp.setX(i * timeStep);
					waleUp.setStatus(BedBean.SLEEPUP);
					waleUp.setY(0);
					SleepInUP.add(waleUp);
				}
				// 纽扣没有呼吸暂停和心率暂停
				if (deviceType != DeviceType.DEVICE_TYPE_SLEEPDOT) {
					if (analysis.getHeartRateStatusAry()[i] > 0) { // 心跳暂停点
						GraphView.GraphViewData heartPause = new GraphView.GraphViewData(i * timeStep, mainData[i].valueY);
						heartPause.setApneaRate(detail.getBreathRate()[i]);
						heartPause.setHeartRate(detail.getHeartRate()[i]);
						heartPause.setStatus(3);
						heartPause.setStatusValue(analysis.getHeartRateStatusAry()[i]);
						heartPauseList.add(heartPause);
					}
					if (analysis.getBreathRateStatusAry()[i] > 0) { // 呼吸暂停点
						GraphView.GraphViewData breathPause = new GraphView.GraphViewData(i * timeStep, mainData[i].valueY);
						breathPause.setApneaRate(detail.getBreathRate()[i]);
						breathPause.setHeartRate(detail.getHeartRate()[i]);
						breathPause.setStatus(2);
						breathPause.setStatusValue(analysis.getBreathRateStatusAry()[i]);
						apneaPauseList.add(breathPause);
					}
				}
				
				if (analysis.getLeftBedStatusAry()[i] > 0) { // 离床点
					if (i > 0) {
						if (analysis.getLeftBedStatusAry()[i - 1] == 0) {
							LineGraphView.BedBean wakeUp = new LineGraphView.BedBean();
							wakeUp.setX(i * timeStep);
							wakeUp.setY((float) mainData[i].getY());
							wakeUp.setData(new GraphView.GraphViewData(i * timeStep, (float) mainData[i].getY()));
							wakeUp.setWake(true);
							bedBeans.add(wakeUp);
							leaveBedStart = i;
						}
					}

					if (i + 1 < analysis.getSleepCurveStatusArray().length) {
						if (analysis.getLeftBedStatusAry()[i + 1] == 0) {
							LineGraphView.BedBean wakeIn = new LineGraphView.BedBean();
							wakeIn.setX(i * timeStep);
							wakeIn.setY((float) mainData[i].getY());
							wakeIn.setData(new GraphView.GraphViewData(i * timeStep, (float) mainData[i].getY()));
							wakeIn.setWake(false);
							wakeIn.setStatusValue((i - leaveBedStart) * 60);
							bedBeans.add(wakeIn);
						}
					}
					// 如果本身就是最后一个离床点，判断前一个是否是离床点，如果有连续两个离床点则认为是离床
					// 0,0,0,0,0,0,4,4,0,0,0,0,4,4,4,4,4,0,0,0,0,0,0,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4不加这个的话对这种情况会少一个离床点
					if (i + 1 == analysis.getSleepCurveStatusArray().length) {
						if (analysis.getLeftBedStatusAry()[i - 1]  > 0) {
							LineGraphView.BedBean wakeIn = new LineGraphView.BedBean();
							wakeIn.setX(i * timeStep);
							wakeIn.setY((float) mainData[i].getY());
							wakeIn.setData(new GraphView.GraphViewData(i * timeStep, (float) mainData[i].getY()));
							wakeIn.setWake(false);
							wakeIn.setStatusValue((i - leaveBedStart) * 60);
							bedBeans.add(wakeIn);
						}
					}

				}
			}
		}
		return mainData;
	}

	/**
	 * <p>
	 * 分析detail
	 * </p>
	 * 
	 * @param analysis
	 * @param timeStep
	 */
	public GraphView.GraphViewData[] getSleepGraphData(Detail detail, Analysis analysis, int timeStep, DeviceType deviceType) {
		if (analysis == null || analysis.getSleepCurveArray() == null || analysis.getSleepCurveArray().length == 0
				|| analysis.getSleepCurveStatusArray() == null || analysis.getSleepCurveStatusArray().length == 0){
			return null;
		}
		return getNewSleepGraphData(detail, analysis, timeStep, deviceType);
	}

	/**
	 * <p>
	 * 由于datas是按照x轴为时间轴的， 保证第一个数 是小于 提供值x的值，就是最近的值
	 * </p>
	 */
	public static GraphView.GraphViewData findNear(GraphView.GraphViewData[] datas, int x) {
		if (datas == null) {
			return null;
		}
		if (datas.length == 0)
			return null;

		if (datas[0].getX() > x)
			return null;

		for (int i = 0; i < datas.length; i++) {
			if (datas[i].getX() >= x)
				return datas[i];
		}
		return null;
	}

	/**
	 * <p>
	 * 由于datas是按照x轴为时间轴的， 保证第一个数 是小于 提供值x的值，就是最近的值
	 * </p>
	 */
	public static GraphView.GraphViewData findNear(GraphView.GraphViewData[] datas, int x, List<GraphView.GraphViewData> dt) {
		if (datas == null) {
			return null;
		}
		if (datas.length == 0)
			return null;

		if (datas[0].getX() > x)
			return null;

		for (int i = 0; i < datas.length; i++) {
			if (datas[i].getX() >= x) {
				if (dt != null)
					for (GraphView.GraphViewData gv : dt) {
						if (gv.getX() == datas[i].getX()) {
							if (i + 1 < datas.length) {
								return datas[i + 1];
							}
						}
					}
				return datas[i];
			}
		}
		return null;
	}

}




















