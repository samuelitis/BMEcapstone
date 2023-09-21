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
    MainActivity mainActivity = (MainActivity) getActivity();
    public FragmentBluetooth(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bluetooth, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }
    @Override
    public void onRefresh() {
        // 지연 후 디스커버리 시작
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 디스커버리 부분
                Toast.makeText(getContext(), "주변 기기를 찾고 있습니다.", Toast.LENGTH_LONG).show();
                mainActivity.discoverDevices();
                swipeRefreshLayout.setRefreshing(false);
            }
        }, 1500); //딜레이 타임 조절
    }
    @Override
    public void onStart() {
        super.onStart();


        // 기기 이미지
        ImageView bluetoothImage1 = getView().findViewById(R.id.bluetooth_image_1);
        ImageView bluetoothImage2 = getView().findViewById(R.id.bluetooth_image_2);
        ImageView bluetoothImage3 = getView().findViewById(R.id.bluetooth_image_3);
        ImageView bluetoothImage4 = getView().findViewById(R.id.bluetooth_image_4);
        ImageView bluetoothImage5 = getView().findViewById(R.id.bluetooth_image_5);
        ImageView bluetoothImage6 = getView().findViewById(R.id.bluetooth_image_6);

        // 기기 이름
        TextView deviceNameValue1 = getView().findViewById(R.id.device_name_value_1);
        TextView deviceNameValue2 = getView().findViewById(R.id.device_name_value_2);
        TextView deviceNameValue3 = getView().findViewById(R.id.device_name_value_3);
        TextView deviceNameValue4 = getView().findViewById(R.id.device_name_value_4);
        TextView deviceNameValue5 = getView().findViewById(R.id.device_name_value_5);
        TextView deviceNameValue6 = getView().findViewById(R.id.device_name_value_6);

        //연결 상태
        TextView statusValue1 = getView().findViewById(R.id.status_value_1);
        TextView statusValue2 = getView().findViewById(R.id.status_value_2);
        TextView statusValue3 = getView().findViewById(R.id.status_value_3);
        TextView statusValue4 = getView().findViewById(R.id.status_value_4);
        TextView statusValue5 = getView().findViewById(R.id.status_value_5);
        TextView statusValue6 = getView().findViewById(R.id.status_value_6);

        // 센서 상태
        TextView sensorStatusValue1 = getView().findViewById(R.id.sensor_status_value_1);
        TextView sensorStatusValue2 = getView().findViewById(R.id.sensor_status_value_2);
        TextView sensorStatusValue3 = getView().findViewById(R.id.sensor_status_value_3);
        TextView sensorStatusValue4 = getView().findViewById(R.id.sensor_status_value_4);
        TextView sensorStatusValue5 = getView().findViewById(R.id.sensor_status_value_5);
        TextView sensorStatusValue6 = getView().findViewById(R.id.sensor_status_value_6);

        // 배터리 상태
        TextView batteryValue1 = getView().findViewById(R.id.battery_value_1);
        TextView batteryValue2 = getView().findViewById(R.id.battery_value_2);
        TextView batteryValue3 = getView().findViewById(R.id.battery_value_3);
        TextView batteryValue4 = getView().findViewById(R.id.battery_value_4);
        TextView batteryValue5 = getView().findViewById(R.id.battery_value_5);
        TextView batteryValue6 = getView().findViewById(R.id.battery_value_6);

        // 연결 속도
        TextView transferRateValue1 = getView().findViewById(R.id.transfer_rate_value_1);
        TextView transferRateValue2 = getView().findViewById(R.id.transfer_rate_value_2);
        TextView transferRateValue3 = getView().findViewById(R.id.transfer_rate_value_3);
        TextView transferRateValue4 = getView().findViewById(R.id.transfer_rate_value_4);
        TextView transferRateValue5 = getView().findViewById(R.id.transfer_rate_value_5);
        TextView transferRateValue6 = getView().findViewById(R.id.transfer_rate_value_6);

        // 연결 버튼
        Button btnBluetoothConnect1 = getView().findViewById(R.id.btn_bluetooth_connect_1);
        Button btnBluetoothConnect2 = getView().findViewById(R.id.btn_bluetooth_connect_2);
        Button btnBluetoothConnect3 = getView().findViewById(R.id.btn_bluetooth_connect_3);
        Button btnBluetoothConnect4 = getView().findViewById(R.id.btn_bluetooth_connect_4);
        Button btnBluetoothConnect5 = getView().findViewById(R.id.btn_bluetooth_connect_5);
        Button btnBluetoothConnect6 = getView().findViewById(R.id.btn_bluetooth_connect_6);

        // 새로고침을 통한 디스커버리

        // 버튼 설정
    }

    // 블루투스 디스커버리 함수

    // 블루투스 페어링 함수

    // 블루투스 상태 갱신 함수

    // 중간에 끊키면!

}
