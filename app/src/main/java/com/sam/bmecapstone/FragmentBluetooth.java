package com.sam.bmecapstone;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class FragmentBluetooth extends Fragment implements SwipeRefreshLayout.OnRefreshListener{
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isInitialized = false;
    // 기기 이미지
// 기기 이미지
    private ImageView bluetoothImage1, bluetoothImage2, bluetoothImage3, bluetoothImage4, bluetoothImage5, bluetoothImage6;
    // 기기 이름
    private TextView deviceNameValue1, deviceNameValue2, deviceNameValue3, deviceNameValue4, deviceNameValue5, deviceNameValue6;
    // 연결 상태
    private TextView statusValue1, statusValue2, statusValue3, statusValue4, statusValue5, statusValue6;
    // 센서 상태
    private TextView sensorStatusValue1, sensorStatusValue2, sensorStatusValue3, sensorStatusValue4, sensorStatusValue5, sensorStatusValue6;
    // 배터리 상태
    private TextView batteryValue1, batteryValue2, batteryValue3, batteryValue4, batteryValue5, batteryValue6;
    // 연결 속도
    private TextView transferRateValue1, transferRateValue2, transferRateValue3, transferRateValue4, transferRateValue5, transferRateValue6;
    // 연결 버튼
    private Button btnBluetoothConnect1, btnBluetoothConnect2, btnBluetoothConnect3, btnBluetoothConnect4, btnBluetoothConnect5, btnBluetoothConnect6;

    MainActivity mainActivity;
    public FragmentBluetooth() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mainActivity = (MainActivity) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        if (!isInitialized) {
            // 기기 이미지
            ImageView bluetoothImage1 = view.findViewById(R.id.bluetooth_image_1);
            ImageView bluetoothImage2 = view.findViewById(R.id.bluetooth_image_2);
            ImageView bluetoothImage3 = view.findViewById(R.id.bluetooth_image_3);
            ImageView bluetoothImage4 = view.findViewById(R.id.bluetooth_image_4);
            ImageView bluetoothImage5 = view.findViewById(R.id.bluetooth_image_5);
            ImageView bluetoothImage6 = view.findViewById(R.id.bluetooth_image_6);

            // 기기 이름
            TextView deviceNameValue1 = view.findViewById(R.id.device_name_value_1);
            TextView deviceNameValue2 = view.findViewById(R.id.device_name_value_2);
            TextView deviceNameValue3 = view.findViewById(R.id.device_name_value_3);
            TextView deviceNameValue4 = view.findViewById(R.id.device_name_value_4);
            TextView deviceNameValue5 = view.findViewById(R.id.device_name_value_5);
            TextView deviceNameValue6 = view.findViewById(R.id.device_name_value_6);

            //연결 상태
            TextView statusValue1 = view.findViewById(R.id.status_value_1);
            TextView statusValue2 = view.findViewById(R.id.status_value_2);
            TextView statusValue3 = view.findViewById(R.id.status_value_3);
            TextView statusValue4 = view.findViewById(R.id.status_value_4);
            TextView statusValue5 = view.findViewById(R.id.status_value_5);
            TextView statusValue6 = view.findViewById(R.id.status_value_6);

            // 센서 상태
            TextView sensorStatusValue1 = view.findViewById(R.id.sensor_status_value_1);
            TextView sensorStatusValue2 = view.findViewById(R.id.sensor_status_value_2);
            TextView sensorStatusValue3 = view.findViewById(R.id.sensor_status_value_3);
            TextView sensorStatusValue4 = view.findViewById(R.id.sensor_status_value_4);
            TextView sensorStatusValue5 = view.findViewById(R.id.sensor_status_value_5);
            TextView sensorStatusValue6 = view.findViewById(R.id.sensor_status_value_6);

            // 배터리 상태
            TextView batteryValue1 = view.findViewById(R.id.battery_value_1);
            TextView batteryValue2 = view.findViewById(R.id.battery_value_2);
            TextView batteryValue3 = view.findViewById(R.id.battery_value_3);
            TextView batteryValue4 = view.findViewById(R.id.battery_value_4);
            TextView batteryValue5 = view.findViewById(R.id.battery_value_5);
            TextView batteryValue6 = view.findViewById(R.id.battery_value_6);

            // 연결 속도
            TextView transferRateValue1 = view.findViewById(R.id.transfer_rate_value_1);
            TextView transferRateValue2 = view.findViewById(R.id.transfer_rate_value_2);
            TextView transferRateValue3 = view.findViewById(R.id.transfer_rate_value_3);
            TextView transferRateValue4 = view.findViewById(R.id.transfer_rate_value_4);
            TextView transferRateValue5 = view.findViewById(R.id.transfer_rate_value_5);
            TextView transferRateValue6 = view.findViewById(R.id.transfer_rate_value_6);

            // 연결 버튼
            Button btnBluetoothConnect1 = view.findViewById(R.id.btn_bluetooth_connect_1);
            Button btnBluetoothConnect2 = view.findViewById(R.id.btn_bluetooth_connect_2);
            Button btnBluetoothConnect3 = view.findViewById(R.id.btn_bluetooth_connect_3);
            Button btnBluetoothConnect4 = view.findViewById(R.id.btn_bluetooth_connect_4);
            Button btnBluetoothConnect5 = view.findViewById(R.id.btn_bluetooth_connect_5);
            Button btnBluetoothConnect6 = view.findViewById(R.id.btn_bluetooth_connect_6);

            // 처음엔 연결 버튼을 숨김
            btnBluetoothConnect1.setVisibility(View.INVISIBLE);
            btnBluetoothConnect2.setVisibility(View.INVISIBLE);
            btnBluetoothConnect3.setVisibility(View.INVISIBLE);
            btnBluetoothConnect4.setVisibility(View.INVISIBLE);
            btnBluetoothConnect5.setVisibility(View.INVISIBLE);
            btnBluetoothConnect6.setVisibility(View.INVISIBLE);
            isInitialized = true;
        }

        swipeRefreshLayout = view.findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(this);
        int[] btnIds = {
                R.id.btn_bluetooth_connect_1, R.id.btn_bluetooth_connect_2, R.id.btn_bluetooth_connect_3,
                R.id.btn_bluetooth_connect_4, R.id.btn_bluetooth_connect_5, R.id.btn_bluetooth_connect_6
        };

        for (int i = 0; i < btnIds.length; i++) {
            final int index = i;
            Button btn = view.findViewById(btnIds[i]);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (index < mainActivity.bluetoothDevices.size() && mainActivity.bluetoothDevices.get(index) != null) {
                        mainActivity.connectToDevice(mainActivity.bluetoothDevices.get(index), mainActivity);
                    } else {
                        // 해당 인덱스의 BluetoothDevice가 없는 경우의 처리, 예를 들어 오류 메시지 표시
                    }
                }
            });
        }


    }
    @Override
    public void onRefresh() {
        // 지연 후 디스커버리 시작
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 디스커버리 부분
                mainActivity.discoverDevices(getActivity());
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 1500); //딜레이 타임 조절
        // 딜레이까지 종료 이후
        // 디바이스 매니저에서 리스트를 불러온 이후
        // 기기 찾은 여부에따라 버튼을 생성 , 숨기기 기능
    }
    @Override
    public void onStart() {
        super.onStart();

        // 새로고침을 통한 디스커버리

        // 버튼 설정
    }

    // 블루투스 디스커버리 함수

    // 블루투스 페어링 함수

    // 블루투스 상태 갱신 함수

    // 중간에 끊키면!

}
