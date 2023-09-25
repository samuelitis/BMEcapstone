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
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
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
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCallback gattCallback;
    List<BluetoothDevice> bluetoothDevices = new ArrayList<>();

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
    public boolean allDevicesConnected() {
        // 모든 기기가 연결되었는지 확인하기 위해 bluetoothDevices의 크기가 6인지 확인
        if (bluetoothDevices.size() != 6) {
            return false;
        }

        // bluetoothDevices 리스트에서 null 값이 있는지 확인
        for (BluetoothDevice device : bluetoothDevices) {
            if (device == null) {
                return false;  // 하나라도 연결되지 않은 기기가 있으면 false 반환
            }
        }

        return true;  // 모든 조건이 만족되면 true 반환
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

                            if (deviceName.startsWith("BCAP ")) {
                                String numberPartStr = deviceName.replace("BCAP ", "");
                                try {
                                    int numberPart = Integer.parseInt(numberPartStr);

                                    // 저장된 device list의 크기가 numberPart보다 작을 경우 리스트를 채워넣습니다.
                                    while (bluetoothDevices.size() < numberPart) {
                                        bluetoothDevices.add(null);
                                    }

                                    bluetoothDevices.set(numberPart - 1, device);

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

    public void connectToDevice(final BluetoothDevice device, Activity activity) {
        // BLE 디바이스 연결을 위한 콜백
        gattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i("BLEConnect", "Connected to GATT server.");
                    // 연결 성공 후에는 서비스를 발견하도록 요청할 수 있습니다.
                    if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    gatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i("BLEConnect", "Disconnected from GATT server.");
                    // 연결이 끊긴 기기의 위치에 null 설정
                    for (int i = 0; i < bluetoothDevices.size(); i++) {
                        if (bluetoothDevices.get(i).equals(gatt.getDevice())) {
                            bluetoothDevices.set(i, null);
                            break;
                        }
                    }
                    gatt.close();  // BluetoothGatt 리소스를 해제
                }
            }
            // 연결 후 서비스 발견시
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    // 원하는 서비스 및 특성을 탐색
                    BluetoothGattService targetService = gatt.getService(UUID.fromString("0000179B-0000-1000-8000-00805F9B34FB")); // arduino : BLEService "179B";
                    if (targetService != null) {
                        BluetoothGattCharacteristic targetCharacteristic = targetService.getCharacteristic(UUID.fromString("00002A58-0000-1000-8000-00805F9B34FB")); // arduino : BLECharacteristic "2A58"
                        if (targetCharacteristic != null) {
                            // 특성에 대한 알림 활성화
                            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            gatt.setCharacteristicNotification(targetCharacteristic, true);
                            BluetoothGattDescriptor descriptor = targetCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }
                    }
                } else {
                    Log.w("BLEConnect", "onServicesDiscovered received: " + status);
                }
            }

            // 특성 값 변경 시
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                byte[] data = characteristic.getValue();
                // 받은 데이터를 여기서 처리...
            }

            // 다른 콜백 메서드들도 필요에 따라 오버라이드 할 수 있습니다.
        };

        // 연결 시도
        mBluetoothGatt = device.connectGatt(activity, false, gattCallback);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


}
