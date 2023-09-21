package com.sam.bmecapstone;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.Manifest;


import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btnBluetooth, btnCam, btnInfo;
    private ActivityResultLauncher<Intent> enableBluetoothLauncher;

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private BluetoothManager bluetoothManager; // 블루투스 매니저
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice_1; // 블루투스 디바이스
    private BluetoothDevice bluetoothDevice_2; // 블루투스 디바이스
    private BluetoothDevice bluetoothDevice_3; // 블루투스 디바이스
    private BluetoothDevice bluetoothDevice_4; // 블루투스 디바이스
    private BluetoothDevice bluetoothDevice_5; // 블루투스 디바이스
    private BluetoothDevice bluetoothDevice_6; // 블루투스 디바이스
    private static final int PERMISSION_REQUEST_CODE = 1002;
    private static final int REQUEST_ENABLE_BT = 992;


    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
                    String deviceName = device.getName();
                    String deviceAddress = device.getAddress();
                    Log.d("BluetoothDiscovery", "Found device: " + deviceName + " - " + deviceAddress);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // 권한 요청
        String[] permissions = {
                Manifest.permission.CAMERA,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
                // 추가할 권한들
        };

        // 권한 확인
        if (checkPermissions(permissions)) {
            // 모든 권한이 이미 허가된 경우, 필요한 작업을 수행
        } else {
            // 하나 이상의 권한이 거부된 경우, 권한 요청
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }


        // 프래그먼트 설정 및 초기화
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        FragmentBluetooth fragmentBluetooth = new FragmentBluetooth();
        FragmentCam fragmentCam = new FragmentCam();
        FragmentInfo fragmentInfo = new FragmentInfo();


        transaction.replace(R.id.frame, fragmentBluetooth);
        transaction.commit();


        enableBluetoothLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // Bluetooth was enable by the user
                        Toast.makeText(this, "블루투스가 활성화 되었습니다.", Toast.LENGTH_LONG).show();
                    } else {
                        // 블루투스 요청 거부하면
                        Toast.makeText(this, "거부하면 일부 기능이 비활성화됩니다.", Toast.LENGTH_LONG).show();

                    }
                }
        );
        // 블루투스 사용 가능 확인
        if (bluetoothAdapter == null || !getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Log.d("BluetoothDebug", "Device does not support Bluetooth");
            Toast.makeText(this, "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBluetoothIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBluetoothLauncher.launch(enableBluetoothIntent);
            } else {
                Log.d("BluetoothDebug", "Bluetooth is already enabled");
                Toast.makeText(this, "블루투스가 활성화 되어있습니다.", Toast.LENGTH_LONG).show();
            }
        }


        btnBluetooth = findViewById(R.id.btn_bluetooth);
        btnCam = findViewById(R.id.btn_CAM);
        btnInfo = findViewById(R.id.btn_info);




        btnBluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                // 초기에 추가된 적이 없으면 추가
                if (!fragmentBluetooth.isAdded()) {
                    transaction.add(R.id.frame, fragmentBluetooth);
                }
                transaction.hide(fragmentCam).hide(fragmentInfo).show(fragmentBluetooth).commit();
            }
        });

        btnCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                if (!fragmentCam.isAdded()) {
                    transaction.add(R.id.frame, fragmentCam);
                }

                transaction.hide(fragmentBluetooth).hide(fragmentInfo).show(fragmentCam).commit();
            }
        });

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                if (!fragmentInfo.isAdded()) {
                    transaction.add(R.id.frame, fragmentInfo);
                }

                transaction.hide(fragmentBluetooth).hide(fragmentCam).show(fragmentInfo).commit();
            }
        });
    }

    private boolean checkPermissions(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }




    // 권한이 이미 승인되었는지 확인
    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    // 권한을 사용자에게 요청
    private void requestPermission(String permission) {
        ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
    }

    // 블루투스 활성화 여부
    @SuppressLint("MissingPermission")
    public void checkAndEnableBluetooth() {
        if (bluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                enableBluetoothLauncher.launch(enableBtIntent);
            }
        }
    }

    // 블루투스 검색
    @SuppressLint("MissingPermission")
    public void discoverDevices() {

        // 디스커버리 시작
        final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        if (bluetoothLeScanner != null) {
            ScanCallback scanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {

                    BluetoothDevice device = result.getDevice();
                    String deviceName = device.getName();          // 기기 이름
                    String deviceAddress = device.getAddress();    // 기기의 MAC 주소

                    // 추가적으로 ScanResult에서 다른 정보도 얻을 수 있습니다.
                    int rssi = result.getRssi();                   // 신호 강도
                    ScanRecord scanRecord = result.getScanRecord();
                    byte[] rawBytes = scanRecord.getBytes();       // 원시 광고 데이터
                    // 디스커버리 이후 저장하거나 활용하는 코드
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    // 스캔 실패 시 처리
                }
            };

            bluetoothLeScanner.startScan(scanCallback);


            Handler handler = new Handler();
            new Handler().postDelayed(() -> {
                bluetoothLeScanner.stopScan(scanCallback);
            }, 10000); // 10초 지연
        }
    }

    // 페어링 시도
    @SuppressLint("MissingPermission")
    public void pairDevice(BluetoothDevice device) {
        if (device != null) {
            device.createBond();
        }
    }

    // 통신을 위한 소켓연결
    // RFCOMM Bluetooth socket 생성
    // 성공적으로 연결된 Bluetooth socket 반환
    @SuppressLint("MissingPermission")
    public BluetoothSocket connectToDevice(BluetoothDevice device, UUID uuid) throws IOException {
        BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);
        socket.connect();
        return socket;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver); // 리시버 해제
    }

    // 디바이스 목록
    public class DeviceManager {
        private static DeviceManager instance;
        private final List<BluetoothDevice> discoveredDevices = new ArrayList<>();

        private DeviceManager() {}

        public static synchronized DeviceManager getInstance() {
            if (instance == null) {
                instance = new DeviceManager();
            }
            return instance;
        }

        public void addDevice(BluetoothDevice device) {
            if (!discoveredDevices.contains(device)) {
                discoveredDevices.add(device);
            }
        }

        public List<BluetoothDevice> getDevices() {
            return discoveredDevices;
        }

        public void clearDevices() {
            discoveredDevices.clear();
        }
    }
}
