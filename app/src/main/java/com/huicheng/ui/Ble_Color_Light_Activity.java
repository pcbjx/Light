/*
*buil：2017-4-9
 *  author:张泽伟
* email：659620728@qq.com
* tel：15915878086
*
* */

package com.huicheng.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.huicheng.MainActivity;
import com.huicheng.R;
import com.huicheng.service.BluetoothLeService;
import zhou.colorpalette.core.ColorPalette;



public class Ble_Color_Light_Activity extends Activity {

	private final static String TAG = Ble_Activity.class.getSimpleName();
	//??4.0?UUID,??0000ffe1-0000-1000-8000-00805f9b34fb?????????????08?????UUID
	public static String HEART_RATE_MEASUREMENT = "0000ffe1-0000-1000-8000-00805f9b34fb";
	//public static String HEART_RATE_MEASUREMENT = "00001800-0000-1000-8000-00805f9b34fb";

	public static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";;
	public static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
	public static String EXTRAS_DEVICE_RSSI = "RSSI";
	//??????
	//private int mConnected = 0;
	private String status = "disconnected";
	//????
	private String mDeviceName;
	//????
	private String mDeviceAddress;
	//?????
	private String mRssi;
	private Bundle b;
	private String rev_str = "";
	//??service,?????????
	private static BluetoothLeService mBluetoothLeService;

	LinearLayout rootLayout;
	private ColorPalette colorPalette;
	private int cur_color;

	private MyToolBar myToolBar;// �Զ���toolbar
	private Button bt_connect_status;
    int connect_state = 2;

	data_struct my_datastruct;
	//on /off
	Button [] btSwitchAllList = new Button[2];
	static byte lightSwitch =  0;
	//方向选择 左-前-右-全部
	Button [] btPostionList = new Button[4];
	static int position = 0xff;
	//模式选择 //色盘-呼吸-七彩-闪烁
	Button [] btModeList = new Button[4];
	static int lightMode = 0xff;

	SeekBar seekBarSpeed;
	static byte speed = 3;

	SeekBar seekBarBright ;
	static byte light_bright = 50;


	static  final int cmd_switch_status = 1;
	static  final int cmd_data_status = 4;

	private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
	//?????
	private static BluetoothGattCharacteristic target_chara = null;
	private Handler mhandler = new Handler();
	private Handler myHandler = new Handler()
	{
		// 2.????????
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
				// ???????
				case 1:
				{
					//myToolBar.updateRightButton(msg.arg1);
					update_bt_conect_status(msg.arg1);
					break;
				}

			}
			super.handleMessage(msg);
		}

	};

	private  void update_bt_conect_status(int bt_status)
	{
		connect_state = bt_status;
		if (bt_status== my_datastruct.connected) {
			bt_connect_status.setText(R.string.connect_status_connect);
		}
		if (bt_status == my_datastruct.connecting) {
			bt_connect_status.setText(R.string.connect_status_ing);
		}
		if (bt_status == my_datastruct.dis_connect) {
			bt_connect_status.setText(R.string.connect_status_dis);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_color_light);
		b = getIntent().getExtras();
		//????????????
		mDeviceName = b.getString(EXTRAS_DEVICE_NAME);
		mDeviceAddress = b.getString(EXTRAS_DEVICE_ADDRESS);
		mRssi = b.getString(EXTRAS_DEVICE_RSSI);

		/* ????service */
		Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
		bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
		init();

	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		//???????
		unregisterReceiver(mGattUpdateReceiver);
		mBluetoothLeService.disconnect();
		mBluetoothLeService = null;

	}

	// Activity???????????????????????????
	@Override
	protected void onResume()
	{
		super.onResume();
		//???????
		registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
		if (mBluetoothLeService != null)
		{
			//???????????
			final boolean result = mBluetoothLeService.connect(mDeviceAddress);
			Log.d(TAG, "Connect request result=" + result);
		}
	}


	/**
	 * @Title: titile_init
	 * @Description: TODO(��ʼ��titile)
	 * @param
	 * @return void
	 * @throws
	 *
	 */
	public void titile_init()
	{
		bt_connect_status = (Button)findViewById(R.id.bt_connect_status);

		update_bt_conect_status(connect_state);


		/*
		myToolBar = (MyToolBar) findViewById(R.id.myToolBar);
		// ��������ұߵİ�ť�Ƿ���ʾ
		myToolBar.setToolBarBtnVisiable(false, true);
		// �����Ƿ���ʾ�м���⣬Ĭ�ϵ�����ʾ
		myToolBar.setToolBarTitleVisible(R.string.title_main,true);

		myToolBar.updateRightButton(connect_state);


		myToolBar.setOnMyToolBarClickListener(new MyToolBar.MyToolBarClickListener() {

			@Override
			public void rightBtnClick() {// �ұ߰�ť����¼�

			}

			@Override
			public void leftBtnClick() {// ��߰�ť����¼�
				//Toast.makeText(dsp_main.this, "����", Toast.LENGTH_SHORT).show();
			}
		});*/
	}

	/**
	 * @Title: init
	 * @Description: TODO(???UI??)
	 * @param  ?
	 * @return void
	 * @throws
	 */
	private void init()
	{

		my_datastruct = new data_struct();

		titile_init();
		rootLayout= (LinearLayout)findViewById(R.id.colorLayout);
		colorPalette= (ColorPalette) rootLayout.findViewById(zhou.colorpalette.R.id.zhou_fragment_color);

		cur_color = 0xffffff;//????
		colorPalette.setLastColor(cur_color);
		colorPalette.setCurrColor(cur_color);
		colorPalette.setOnColorSelectListener(new ColorPalette.OnColorSelectListener() {
			@Override
			public void onColorSelect(int color) {
				Log.v(TAG,"color is "+String.format("%08x",color));
				cur_color = color;
				lightMode = 0;//颜色模式
				sendcmd((byte)cmd_data_status);
			}
		});

		seekBarSpeed = (SeekBar)findViewById(R.id.seekbarSpeed);
		seekBarSpeed.setProgress(3);
		seekBarSpeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				speed = (byte) progress;
				sendcmd((byte)cmd_data_status);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});

		seekBarBright = (SeekBar) findViewById(R.id.seekbarLightseek);
		seekBarBright.setProgress(light_bright);
		seekBarBright.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				light_bright = (byte) progress;
				sendcmd((byte)cmd_data_status);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}
		});
		lightSwitch = 0;
		btSwitchAllList[0] = (Button)findViewById(R.id.open);
		btSwitchAllList[1] = (Button)findViewById(R.id.close);
		for (int i =0;i<2;i++)
		{
			btSwitchAllList[i].setOnClickListener(switchOnClickListener);
		}

		btPostionList[0] = (Button)findViewById(R.id.bt_light_left);
		btPostionList[1] = (Button)findViewById(R.id.bt_light_front);
		btPostionList[2] = (Button)findViewById(R.id.bt_light_right);
		btPostionList[3] = (Button)findViewById(R.id.bt_light_all);

		for (int i = 0;i<4;i++)
		{
			btPostionList[i].setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {

					for (int j = 0;j<4;j++)
					{
						if (v.getId() == btPostionList[j].getId())
						{
							position = j;
							my_datastruct.light_seclected[j] = !(my_datastruct.light_seclected[j]);
							falsh_position();
							//sendcmd((byte)cmd_data_status);
						}
					}
				}
			});
		}

		position = 2;//ȫ
		falsh_position();

		btModeList[0] = (Button)findViewById(R.id.bt_mode_color);
		btModeList[1] = (Button)findViewById(R.id.bt_mode_run);
		btModeList[2] = (Button)findViewById(R.id.bt_mode_multi_color);
		btModeList[3] = (Button)findViewById(R.id.bt_mode_twinkle);

		for (int i = 0;i<4;i++)
		{
			btModeList[i].setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					for (int j = 0;j<4;j++)
					{
						if (v.getId() == btModeList[j].getId())
						{
							lightMode = j;
							falsh_mode();
							sendcmd((byte)cmd_data_status);
						}
					}
				}
			});
		}

		lightMode = 0;
		falsh_mode();


		bt_connect_status.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (connect_state == my_datastruct.dis_connect)
				{
					//Intent intent = new Intent(Ble_Color_Light_Activity.this, MainActivity.class);
					//startActivity(intent);

					if (mBluetoothLeService != null)
					{
						//???????????
						final boolean result = mBluetoothLeService.connect(mDeviceAddress);
						Log.d(TAG, "Connect request result=" + result);
					}

				}
			}
		});

	}


		private View.OnClickListener switchOnClickListener = new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int id = v.getId();
				for (int j=0;j<2;j++)
				{
					if (id==btSwitchAllList[j].getId())
					{
						if (j==0)
						{
							flash_light_switch(true);
							lightSwitch = 1;
						}else
						{
							flash_light_switch(false);
							lightSwitch = 0;
						}
						sendcmd((byte)cmd_data_status);
					}
				}
			}
		};

	private void flash_light_switch(boolean on)
	{
		if (on == true)
		{
			btSwitchAllList[0].setBackgroundResource(R.drawable.btn_press);
			btSwitchAllList[1].setBackgroundResource(R.drawable.btn_normal);
		}else
		{
			btSwitchAllList[0].setBackgroundResource(R.drawable.btn_normal);
			btSwitchAllList[1].setBackgroundResource(R.drawable.btn_press);
		}
	}

	private void falsh_position_normal()
	{
		for (int i =0;i<4;i++)
		{
			btPostionList[i].setBackgroundResource(R.drawable.btn_normal);
		}
	}

	private void falsh_position()
	{
		//falsh_position_normal();

		if (my_datastruct.light_seclected[0])
		{
			btPostionList[0].setBackgroundResource(R.drawable.btn_press);
		}else
		{
			btPostionList[0].setBackgroundResource(R.drawable.btn_normal);
		}
		if (my_datastruct.light_seclected[1])
		{
			btPostionList[1].setBackgroundResource(R.drawable.btn_press);
		}else
		{
			btPostionList[1].setBackgroundResource(R.drawable.btn_normal);
		}
		if (my_datastruct.light_seclected[2])
		{
			btPostionList[2].setBackgroundResource(R.drawable.btn_press);
		}else
		{
			btPostionList[2].setBackgroundResource(R.drawable.btn_normal);
		}


	}

	private void falsh_mode_normal()
	{
		for (int i =0;i<4;i++)
		{
			btModeList[i].setBackgroundResource(R.drawable.btn_normal);
		}
	}

	private void falsh_mode()
	{
		//falsh_mode_normal();

		if(lightMode == my_datastruct.mode_index_breathe)
		{
			my_datastruct.is_breathe_mode = true;
			my_datastruct.is_flash_mode = false;
		}else if(lightMode == my_datastruct.mode_index_shake){
			my_datastruct.is_breathe_mode = false;
			my_datastruct.is_flash_mode = true;
		}else if (lightMode == my_datastruct.mode_index_colors)
		{
			if(my_datastruct.is_colcr_mode)
			{
				my_datastruct.is_colcr_mode = false;
			}else
			{
				my_datastruct.is_colcr_mode = true;
			}
		}

		if (my_datastruct.is_breathe_mode )
		{
			btModeList[my_datastruct.mode_index_breathe].setBackgroundResource(R.drawable.btn_press);
		}else
		{
			btModeList[my_datastruct.mode_index_breathe].setBackgroundResource(R.drawable.btn_normal);
		}

		if (my_datastruct.is_colcr_mode )
		{
			btModeList[my_datastruct.mode_index_colors].setBackgroundResource(R.drawable.btn_press);
		}else
		{
			btModeList[my_datastruct.mode_index_colors].setBackgroundResource(R.drawable.btn_normal);
		}

		if (my_datastruct.is_flash_mode )
		{
			btModeList[my_datastruct.mode_index_shake].setBackgroundResource(R.drawable.btn_press);
		}else
		{
			btModeList[my_datastruct.mode_index_shake].setBackgroundResource(R.drawable.btn_normal);
		}

		//btModeList[lightMode].setBackgroundResource(R.drawable.btn_press);

	}

	/* BluetoothLeService??????? */
	private final ServiceConnection mServiceConnection = new ServiceConnection()
	{

		@Override
		public void onServiceConnected(ComponentName componentName,
									   IBinder service)
		{
			mBluetoothLeService = ((BluetoothLeService.LocalBinder) service)
					.getService();
			if (!mBluetoothLeService.initialize())
			{
				Log.e(TAG, "Unable to initialize Bluetooth");
				finish();
			}
			// Automatically connects to the device upon successful start-up
			// initialization.
			// ???????????
			mBluetoothLeService.connect(mDeviceAddress);

		}

		@Override
		public void onServiceDisconnected(ComponentName componentName)
		{
			mBluetoothLeService = null;
		}

	};

	private  void showDatatoUi()
	{
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if (lightSwitch == 1)
					{
						flash_light_switch(true);
					}else
						flash_light_switch(false);

					falsh_mode();

					falsh_position();

					if(light_bright>=0 && light_bright <=100)
					{
						seekBarBright.setProgress(light_bright);
					}

					if(speed >0 &&speed <5)
					{
						seekBarSpeed.setProgress(speed);
					}

				}
			});
	}

	private void analysisData(byte [] data){
		if(data.length < 7)
		{
			Log.v(TAG,"data too short !");
			return;
		}

		if((data[0] == my_datastruct.protol_head )&& (data[1] == my_datastruct.protol_head_2))
		{
			lightSwitch = data[7];
			position = data[8];
			lightMode = data[9];
			cur_color = data[10]<<16+data[11]<<8+data[12];
			light_bright = data[13];

			showDatatoUi();
		}



	}

	/**
	 * ??????????BluetoothLeService??????
	 */
	private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			final String action = intent.getAction();
			if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))//Gatt????
			{
				if (target_chara != null)
				{
					connect_state = my_datastruct.connected;
					updateConnectionState(my_datastruct.connected);
					//sendinit();
					System.out.println("BroadcastReceiver :" + "device connected");
				}
			} else if (BluetoothLeService.ACTION_GATT_DISCONNECTED//Gatt????
					.equals(action))
			{
				connect_state = my_datastruct.dis_connect;
				updateConnectionState(my_datastruct.dis_connect);
				System.out.println("BroadcastReceiver :"
						+ "device disconnected");

			} else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED//??GATT???
					.equals(action))
			{
				// Show all the supported services and characteristics on the
				// user interface.
				//???????????
				displayGattServices(mBluetoothLeService
						.getSupportedGattServices());
				System.out.println("BroadcastReceiver :"
						+ "device SERVICES_DISCOVERED");
			} else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))//????
			{
				//?????????

				//analysisData(intent.getExtras().getByteArray(
				//		BluetoothLeService.EXTRA_DATA));
				//System.out.println("BroadcastReceiver onData:"
						//+ intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
			}
		}
	};

	/* ?????? */
	private void updateConnectionState(int status)
	{
		Message msg = new Message();
		msg.what = 1;

		msg.arg1 = status;

		myHandler.sendMessage(msg);
		System.out.println("connect_state:" + status);

	}

	/* ????? */
	private static IntentFilter makeGattUpdateIntentFilter()
	{
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
		intentFilter
				.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
		return intentFilter;
	}


	/**
	 * @Title: displayGattServices
	 * @Description: TODO(??????)
	 * @param ?
	 * @return void
	 * @throws
	 */
	private void displayGattServices(List<BluetoothGattService> gattServices)
	{

		if (gattServices == null)
			return;
		String uuid = null;
		String unknownServiceString = "unknown_service";
		String unknownCharaString = "unknown_characteristic";

		// ????,?????????????
		ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();

		// ??????????????????????
		ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData = new ArrayList<ArrayList<HashMap<String, String>>>();

		// ????????????
		mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

		// Loops through available GATT Services.
		for (BluetoothGattService gattService : gattServices)
		{

			// ??????
			HashMap<String, String> currentServiceData = new HashMap<String, String>();
			uuid = gattService.getUuid().toString();

			// ??????uuid??????????SampleGattAttributes?????????

			gattServiceData.add(currentServiceData);

			System.out.println("Service uuid:" + uuid);

			ArrayList<HashMap<String, String>> gattCharacteristicGroupData = new ArrayList<HashMap<String, String>>();

			// ???????????????????
			List<BluetoothGattCharacteristic> gattCharacteristics = gattService
					.getCharacteristics();

			ArrayList<BluetoothGattCharacteristic> charas = new ArrayList<BluetoothGattCharacteristic>();

			// Loops through available Characteristics.
			// ????????????????????
			for (final BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
			{
				charas.add(gattCharacteristic);
				HashMap<String, String> currentCharaData = new HashMap<String, String>();
				uuid = gattCharacteristic.getUuid().toString();

				if (gattCharacteristic.getUuid().toString()
						.equals(HEART_RATE_MEASUREMENT))
				{
					// ??????Characteristic??????mOnDataAvailable.onCharacteristicRead()
					mhandler.postDelayed(new Runnable()
					{

						@Override
						public void run()
						{
							// TODO Auto-generated method stub
							mBluetoothLeService
									.readCharacteristic(gattCharacteristic);
						}
					}, 200);

					// ??Characteristic?????,?????????????mOnDataAvailable.onCharacteristicWrite()
					mBluetoothLeService.setCharacteristicNotification(
							gattCharacteristic, true);
					connect_state = my_datastruct.connected;
					update_bt_conect_status(connect_state);
					target_chara = gattCharacteristic;

					Log.v(TAG, "find device");
					// ??????
					// ?????????
					// mBluetoothLeService.writeCharacteristic(gattCharacteristic);
				}
				List<BluetoothGattDescriptor> descriptors = gattCharacteristic
						.getDescriptors();
				for (BluetoothGattDescriptor descriptor : descriptors)
				{
					System.out.println("---descriptor UUID:"
							+ descriptor.getUuid());
					// ????????
					mBluetoothLeService.getCharacteristicDescriptor(descriptor);
					// mBluetoothLeService.setCharacteristicNotification(gattCharacteristic,
					// true);
				}

				gattCharacteristicGroupData.add(currentCharaData);
			}
			// ???????????????????????
			mGattCharacteristics.add(charas);
			// ???????????????????
			gattCharacteristicData.add(gattCharacteristicGroupData);

		}

	}

	public int sendLightCtrlData(byte cmd,byte []data,int len){

		byte [] sendbuf = new byte[len+7];

		sendbuf[0] = (byte) 0xac;
		sendbuf[1] = (byte) 0x55;

		sendbuf[2] = (byte) ((len>>8)&0xff);
		sendbuf[3] = (byte) (len&0xff);
		sendbuf[4] = (byte) 0x01;
		sendbuf[5] = cmd;

		if(data != null)
		{
			System.arraycopy(data,0,sendbuf,6,len);
		}


		int sum = 0;
		for (int i = 0; i < len; i++) {
			sum += data[i];
			Log.v(TAG,String.format("%02x ",data[i]));
		}
		sendbuf[len + 6] = (byte) (0xff - sum & 0xff);
		Log.v(TAG, "send crc:" + sendbuf[len + 6]);
		Log.v(TAG, "send len:" + sendbuf.length);
		Log.v(TAG, " len:" + len);

		for (int i = 0; i < len+7; i++) {
			Log.v(TAG,String.format("%02x ",sendbuf[i]));
			//target_chara.setValue(sendbuf[i],FORMAT_UINT8,i);
		}

		if (target_chara == null) {
			Toast.makeText(this,getResources().getText(R.string.connect_status_dis).toString(),Toast.LENGTH_SHORT).show();
			return -1;
		}
		target_chara.setValue(sendbuf);
		//target_chara.setValue(sendbuf);
		mBluetoothLeService.writeCharacteristic(target_chara);
		return 0;
	}

	//初始化，向mcu获取前一次连接的状态
	public boolean sendinit()
	{
		sendLightCtrlData((byte) 0x04,null,0);
		return  true;
	}

	public boolean sendcmd(byte cmd)
	{
		byte [] data;
		switch (cmd)
		{

			case cmd_data_status:
				Log.v(TAG,"cmd_data_status");
				data = new byte[7];
				if (lightMode==0xff)
				{
					Toast.makeText(this,getResources().getText(R.string.lightModeTips).toString(),Toast.LENGTH_SHORT).show();
					return false;
				}
				if (position==0xff)
				{
					Toast.makeText(this,getResources().getText(R.string.lightPositionTips).toString(),Toast.LENGTH_SHORT).show();
					return false;
				}
				data[0] = (byte)(lightSwitch);
				//data[1] = (byte)(position+1);
				data[2] = (byte)(lightMode+1);

				if (lightMode==0xff)
				{
					Toast.makeText(this,getResources().getText(R.string.lightModeTips).toString(),Toast.LENGTH_SHORT).show();
					return false;
				}

				data[6] = light_bright ;


				if (lightMode==2)
				{
					data[3] = speed;
					//sendLightCtrlData(cmd,data,data.length);
				}

				if (lightMode==0)
				{
					data[3] = (byte) ((cur_color >>16)&0xff);
					data[4] = (byte) ((cur_color >>8)&0xff);
					data[5] = (byte) ((cur_color)&0xff);
					//sendLightCtrlData(cmd,data,data.length);
				}

				if(my_datastruct.light_seclected[0])
				{
					data[1] = (byte)(1);
					sendLightCtrlData(cmd,data,data.length);
				}
				if(my_datastruct.light_seclected[1])
				{
					data[1] = (byte)(2);
					sendLightCtrlData(cmd,data,data.length);
				}
				if(my_datastruct.light_seclected[2])
				{
					data[1] = (byte)(3);
					sendLightCtrlData(cmd,data,data.length);
				}


				break;
			default:
				return false;

		}
		return true;
	}

	public void Color(View view) {
//		switch (view.getId()) {
//
//			case R.id.button3:
//				ColorSelectDialogFragment colorSelectDialogFragment=new ColorSelectDialogFragment();
//				colorSelectDialogFragment.setOnColorSelectListener(new ColorSelectDialogFragment.OnColorSelectListener() {
//					@Override
//					public void onSelectFinish(int color) {
//						lastColor=color;
//						//view_color.setBackgroundColor(lastColor);
//
//					}
//				});
//				colorSelectDialogFragment.setLastColor(lastColor);
//				FragmentTransaction ft = getFragmentManager().beginTransaction();
//				ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
//				colorSelectDialogFragment.show(ft, "colorSelectDialogFragment");
//				break;
//		}

	}

}
