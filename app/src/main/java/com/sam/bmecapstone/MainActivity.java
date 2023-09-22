package com.sam.bmecapstone;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;


import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    Button btnBluetooth, btnCam, btnInfo;
    List<BluetoothDevice> discoveredDevices;
    private FragmentBluetooth fragmentBluetooth;
    private FragmentCam fragmentCam;
    private FragmentInfo fragmentInfo;
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




        // 프래그먼트 초기화
        fragmentBluetooth = new FragmentBluetooth();
        fragmentCam = new FragmentCam();
        fragmentInfo = new FragmentInfo();

        // 초기 프래그먼트 설정
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame, fragmentBluetooth)
                .commit();

        btnBluetooth = findViewById(R.id.btn_bluetooth);
        btnCam = findViewById(R.id.btn_CAM);
        btnInfo = findViewById(R.id.btn_info);

        btnBluetooth.setOnClickListener(view -> switchFragment(fragmentBluetooth));
        btnCam.setOnClickListener(view -> switchFragment(fragmentCam));
        btnInfo.setOnClickListener(view -> switchFragment(fragmentInfo));
    }
    private void switchFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (fragment.isAdded()) {
            transaction.show(fragment);
        } else {
            transaction.add(R.id.frame, fragment);
        }

        // 다른 프래그먼트들은 숨김
        if (fragment != fragmentBluetooth) transaction.hide(fragmentBluetooth);
        if (fragment != fragmentCam) transaction.hide(fragmentCam);
        if (fragment != fragmentInfo) transaction.hide(fragmentInfo);

        transaction.commit();
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

    // 블루투스 LE 검색
    public void discoverDevices(Activity activity) {
        try {
            discoveredDevices = new ArrayList<>();
            final BluetoothLeScanner bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

            if (bluetoothLeScanner != null) {
                ScanCallback scanCallback = new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        BluetoothDevice device = result.getDevice();
                        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        String deviceName = device.getName();
                        String deviceAddress = device.getAddress();
                        if (deviceName != null && !discoveredDevices.contains(device)) {
                            discoveredDevices.add(device);

                            // 여기서 바로 장치 처리
                            if (deviceName.startsWith("BCAP ")) {
                                String numberPartStr = deviceName.replace("BCAP ", "");
                                try {
                                    int numberPart = Integer.parseInt(numberPartStr);
                                    int[] statusIds = {
                                            R.id.status_value_1, R.id.status_value_2, R.id.status_value_3,
                                            R.id.status_value_4, R.id.status_value_5, R.id.status_value_6
                                    };

                                    int[] btnIds = {
                                            R.id.btn_bluetooth_connect_1, R.id.btn_bluetooth_connect_2, R.id.btn_bluetooth_connect_3,
                                            R.id.btn_bluetooth_connect_4, R.id.btn_bluetooth_connect_5, R.id.btn_bluetooth_connect_6
                                    };

                                    if (numberPart >= 1 && numberPart <= 6) {
                                        TextView transText = findViewById(statusIds[numberPart - 1]);
                                        Button transBtn = findViewById(btnIds[numberPart - 1]);

                                        transBtn.setVisibility(View.VISIBLE);
                                        transBtn.setText("연결 시도");
                                        transText.setText("연결 가능");
                                    }
                                } catch (NumberFormatException e) {
                                    Log.e("Error", "Failed to parse the number from device name", e);
                                }
                            }
                        }

                        if (deviceName != null) {
                            Log.d("BLEDiscovery", "Found device: " + deviceName + " - " + deviceAddress);
                        }
                    }

                    @Override
                    public void onScanFailed(int errorCode) {
                        super.onScanFailed(errorCode);
                    }
                };

                bluetoothLeScanner.startScan(scanCallback);
            }
        } catch (Exception e) {
            Log.e("Error", "An error occurred during device discovery", e);
        }
    }


    // LE에 페어링 시도
    @SuppressLint("MissingPermission")
    public void pairDevice(BluetoothDevice device) {
        if (device != null) {
            device.createBond();
        }
    }

    // LE에 통신을 위한 소켓연결
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
    }


}
