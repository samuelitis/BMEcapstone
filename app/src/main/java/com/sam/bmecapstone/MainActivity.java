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
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    private int CamState = 0;


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
                .replace(R.id.frame, fragmentCam)
                .commit();

        btnBluetooth = findViewById(R.id.btn_bluetooth);
        btnCam = findViewById(R.id.btn_CAM);
        btnInfo = findViewById(R.id.btn_info);

        btnBluetooth.setOnClickListener(view -> {
            switchFragment(fragmentBluetooth);
            btnCam.setText("측정");
            CamState = 0;
        });

        btnInfo.setOnClickListener(view -> {
            switchFragment(fragmentInfo);
            btnCam.setText("측정");
            CamState = 0;
        });

        btnCam.setOnClickListener(view -> {
            if (CamState == 0) {
                // CamState가 false일 때
                switchFragment(fragmentCam);
                btnCam.setText("측정 시작");
                CamState = 1;
            } else if (CamState == 1) {
                // 측정 시작!!
                CamState = 2;
                startPeriodicDataSend();
                btnCam.setText("측정 종료");
            } else if (CamState == 2) {
                // 측정 종료!!
                CamState = 1;
                stopPeriodicDataSend();
                btnCam.setText("측정 시작");

            }
        });
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
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) { // 블루투스 연결 상태가 바뀌었다면
                if (status == BluetoothGatt.GATT_SUCCESS) { // 현재 상태가 GATT 서비스와 연결이 되어있다면
                    if (newState == BluetoothProfile.STATE_CONNECTED) { // 이 기기, gatt의 새로운 상태가 연결되어있다면
                        Log.i("BLEConnect", "Connected to GATT server.");  // 연결 준비가 되었다 말해줌(GATT와 연결된것임)
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
                } else {
                    // 연결 실패
                    Log.e("BLEConnect", "Connection failed with status: " + status);
                    // 추가로 필요한 오류 처리
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
                            String deviceName = gatt.getDevice().getName();
                            String deviceAddress = gatt.getDevice().getAddress(); // 또는 MAC 주소를 사용
                            Log.i("BLEConnect", "Connected to Device: " + deviceName + " (" + deviceAddress + ")");

                            gatt.setCharacteristicNotification(targetCharacteristic, true);
                            BluetoothGattDescriptor descriptor = targetCharacteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);

                            // 장치 이름에서 숫자 추출하는 로직
                            String[] nameParts = deviceName.split(" ");
                            int numberPart = -1;
                            if (nameParts.length == 2) {
                                try {
                                    numberPart = Integer.parseInt(nameParts[1]); // 디바이스 이름에서 숫자 부분을 추출
                                } catch (NumberFormatException e) {
                                    Log.e("BluetoothError", "Device name does not contain a valid number: " + deviceName);
                                }
                            }

                            if (numberPart >= 1 && numberPart <= 6) {
                                // Fragment에 정의된 메소드를 통해 UI 업데이트
                                fragmentBluetooth.updateDeviceConnectionStatus(numberPart, true);
                            }
                        } else {
                            Log.w("BLEConnect", "Target characteristic not found");
                        }
                    } else {
                        // 서비스를 찾지 못한 경우 로깅
                        Log.w("BLEConnect", "Target service not found");
                    }
                } else {
                    Log.w("BLEConnect", "onServicesDiscovered received: " + status);
                }
            }

            // 특성 값 변경 시
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                byte[] data = characteristic.getValue();
                // 데이터 파싱 로직 (예시)
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                String deviceName = gatt.getDevice().getName();
                float ax = bytesToFloat(data, 1); // X축 데이터 파싱
                float ay = bytesToFloat(data, 5); // Y축 데이터 파싱
                float az = bytesToFloat(data, 9); // Z축 데이터 파싱

                FragmentCam fragmentCam = null;

                // 모든 Fragment 중에서 FragmentCam 인스턴스 찾기
                List<Fragment> fragments = getSupportFragmentManager().getFragments();
                for (Fragment fragment : fragments) {
                    if (fragment instanceof FragmentCam) {
                        fragmentCam = (FragmentCam) fragment;
                        break;
                    }
                }

                // FragmentCam 인스턴스가 존재하면 해당 메소드 호출
                if (fragmentCam != null) {

                    fragmentCam.updateChart(deviceName, (char) data[0], ax, ay, az);
                }

                // SensorData 객체 생성 및 추가
                SensorDataStore.getInstance().addData(new SensorDataStore.SensorData(deviceName, ax, ay, az));
            }

            public float bytesToFloat(byte[] bytes, int start) {
                int asInt = (bytes[start] & 0xFF)
                        | ((bytes[start + 1] & 0xFF) << 8)
                        | ((bytes[start + 2] & 0xFF) << 16)
                        | ((bytes[start + 3] & 0xFF) << 24);
                return Float.intBitsToFloat(asInt);
            }

            // 다른 콜백 메서드들도 필요에 따라 오버라이드 할 수 있습니다.
        };

        // 연결 시도
        mBluetoothGatt = device.connectGatt(activity, false, gattCallback);
    }
    public static class SensorDataStore {

        private static SensorDataStore instance = null;
        private final int MAX_SIZE = 1000; // 최대 데이터 수
        private Map<String, ArrayDeque<SensorData>> deviceDataMap; // 각 장치별 데이터 저장소

        private SensorDataStore() {
            deviceDataMap = new HashMap<>();
        }

        public static synchronized SensorDataStore getInstance() {
            if (instance == null) {
                instance = new SensorDataStore();
            }
            return instance;
        }

        public synchronized void addData(SensorData data) {
            String deviceName = data.getDeviceName();
            deviceDataMap.putIfAbsent(deviceName, new ArrayDeque<>());
            ArrayDeque<SensorData> queue = deviceDataMap.get(deviceName);
            if (queue.size() >= MAX_SIZE) {
                queue.poll(); // 최대 크기 초과시 가장 오래된 데이터 제거
            }
            queue.add(data);
            // Log.d("SensorDataStore", "Data added for device " + deviceName + ": " + data.toString()); // 데이터 추가 로그
        }

        public synchronized List<SensorData> getAllDataForDeviceName(String deviceName) {
            return new ArrayList<>(deviceDataMap.getOrDefault(deviceName, new ArrayDeque<>()));
        }

        public synchronized int dataSize(String deviceName) {
            return deviceDataMap.getOrDefault(deviceName, new ArrayDeque<>()).size();
        }

        public synchronized void clearDataForDevice(String deviceName) {
            if (deviceDataMap.containsKey(deviceName)) {
                deviceDataMap.get(deviceName).clear();
            }
        }

        public static class SensorData {
            private boolean isAcceleration; // 가속도 데이터 여부
            private String deviceName; // 장치 식별자
            public float ax, ay, az; // 가속도 데이터

            public SensorData(String deviceName, float ax, float ay, float az) {
                this.isAcceleration = true; // 가속도 데이터임을 가정
                this.ax = ax;
                this.ay = ay;
                this.az = az;
                this.deviceName = deviceName;
            }

            @Override
            public String toString() {
                return "SensorData{" +
                        "deviceName='" + deviceName + '\'' +
                        ", ax=" + ax +
                        ", ay=" + ay +
                        ", az=" + az +
                        '}';
            }

            public String getDeviceName() {
                return deviceName;
            }
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void sendToServer(JSONObject sensorData) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        RequestBody body = RequestBody.create(JSON, sensorData.toString());
        Request request = new Request.Builder()
                .url("http://192.168.34.132:5055/api/upload")
                .post(body)
                .build();

        // 서버로 데이터를 비동기적으로 전송 (메인 스레드에서 네트워크 작업을 실행하지 않도록)
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("BLEServer", "onFailure: " + e.getMessage()); // 오류 메시지 로그 출력
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    Log.e("BLEServer", "Unexpected code " + response);
                } else {
                    Log.i("BLEServer", "Data sent successfully: " + response.body().string());
                }
            }
        });
    }
    // 주기적으로 데이터를 서버로 전송
    private void sendDataPeriodically() {
        JSONObject allSensorData = new JSONObject();

        try {
            for (int i = 1; i <= 6; i++) {
                String deviceName = "BCAP " + i;
                List<SensorDataStore.SensorData> dataList = SensorDataStore.getInstance().getAllDataForDeviceName(deviceName);
//                Log.d("BLEServer", deviceName + ": " + dataList.toString()); // 데이터 로그 출력

                if (!dataList.isEmpty()) {
                    JSONArray sensorDataJson = sensorDataToJsonArray(dataList);
                    allSensorData.put(deviceName, sensorDataJson);  // JSON 객체에 추가

                    // 전송한 데이터 초기화
                    SensorDataStore.getInstance().clearDataForDevice(deviceName);
                } else {
                    // Log.d("BLEServer", "Empty data for device " + deviceName);
                }
            }

            if (allSensorData.length() > 0) {
                sendToServer(allSensorData);  // 서버로 전송
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("BLEServer", "Error in sendDataPeriodically: " + e.toString());
        }
    }
    public JSONArray sensorDataToJsonArray(List<SensorDataStore.SensorData> dataList) {
        JSONArray jsonArray = new JSONArray();

        for (SensorDataStore.SensorData data : dataList) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("deviceName", data.getDeviceName());
                jsonObject.put("ax", data.ax);
                jsonObject.put("ay", data.ay);
                jsonObject.put("az", data.az);
                // 필요한 다른 센서 데이터도 추가
                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray;
    }
    private Handler handler = new Handler();
    private Runnable sendDataRunnable = new Runnable() {
        @Override
        public void run() {
            sendDataPeriodically();
            handler.postDelayed(this, 1000); // 1초 후에 다시 실행
//            Log.w("BLEServer", "Send Handler");
        }
    };

    // 측정 시작 시 호출
    public void startPeriodicDataSend() {
        handler.post(sendDataRunnable);
        Log.w("BLEServer", "Starting Periodic Data Send ");
    }

    // 측정 중지 시 호출
    public void stopPeriodicDataSend() {
        handler.removeCallbacks(sendDataRunnable);
        Log.w("BLEServer", "Stop Periodic Data Send ");
    }

}
