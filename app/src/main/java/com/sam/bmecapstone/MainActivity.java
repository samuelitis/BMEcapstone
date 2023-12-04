package com.sam.bmecapstone;

import android.Manifest;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
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
    public String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    public Bitmap decodeBase64ToImage(String base64String) {
        byte[] decodedString = Base64.decode(base64String, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        return decodedByte;
    }

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
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            if (newState == BluetoothProfile.STATE_CONNECTED) {
                                Toast.makeText(activity, "블루투스에 연결되었습니다.", Toast.LENGTH_SHORT).show();
                                Log.i("BLEConnect", "Connected to GATT server.");
                                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                gatt.discoverServices();
                            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                                Toast.makeText(activity, "블루투스 연결이 해제되었습니다.", Toast.LENGTH_SHORT).show();
                                Log.i("BLEConnect", "Disconnected from GATT server.");
                                for (int i = 0; i < bluetoothDevices.size(); i++) {
                                    if (bluetoothDevices.get(i).equals(gatt.getDevice())) {
                                        bluetoothDevices.set(i, null);
                                        break;
                                    }
                                }
                                gatt.close();
                            }
                        } else {
                            Toast.makeText(activity, "블루투스 연결 실패: 상태 코드 " + status, Toast.LENGTH_SHORT).show();
                            Log.e("BLEConnect", "Connection failed with status: " + status);
                        }
                    }
                });
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
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // 권한 확인 로직
                    return;
                }
                String deviceName = gatt.getDevice().getName();
                char dataType = (char) data[0]; // 데이터 타입 확인 ('A' 또는 'G')

                float x = bytesToFloat(data, 1); // X축 데이터 파싱
                float y = bytesToFloat(data, 5); // Y축 데이터 파싱
                float z = bytesToFloat(data, 9); // Z축 데이터 파싱

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
                    fragmentCam.updateChart(deviceName, dataType, x, y, z);
                }

                // SensorData 객체 생성 및 추가
                String sensorDataType = (dataType == 'A') ? "acceleration" : "gyroscope";
                SensorDataStore.getInstance().addData(new SensorDataStore.SensorData(deviceName, sensorDataType, x, y, z));
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
            private String dataType; // 데이터 유형 (가속도 "acceleration" 또는 자이로스코프 "gyroscope")
            private String deviceName; // 장치 식별자
            public float ax, ay, az; // 가속도 데이터
            public float gx, gy, gz; // 자이로스코프 데이터

            public SensorData(String deviceName, String dataType, float x, float y, float z) {
                this.deviceName = deviceName;
                this.dataType = dataType;
                if (dataType.equals("acceleration")) {
                    this.ax = x;
                    this.ay = y;
                    this.az = z;
                } else if (dataType.equals("gyroscope")) {
                    this.gx = x;
                    this.gy = y;
                    this.gz = z;
                }
            }

            @Override
            public String toString() {
                return "SensorData{" +
                        "dataType='" + dataType + '\'' +
                        ", deviceName='" + deviceName + '\'' +
                        ", ax=" + ax +
                        ", ay=" + ay +
                        ", az=" + az +
                        ", gx=" + gx +
                        ", gy=" + gy +
                        ", gz=" + gz +
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

    public void sendToServer(JSONObject data) {
        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");

        FragmentCam fragmentCam = findFragmentCam();
        Bitmap cameraImage = null;
        if (fragmentCam != null) {
            cameraImage = fragmentCam.getCurrentPreviewBitmap();
        }
        // 이미지를 Base64 인코딩 문자열로 변환
        String encodedImage = "";
        if (cameraImage != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            cameraImage.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
        }

        // 현재 시간을 ISO 8601 형식으로 추가
        String currentTime = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(new Date());

        try {
            // 기존 JSON 객체에 timestamp 추가
            data.put("timestamp", currentTime);
            data.put("imageData", encodedImage); // 이미지 데이터 추가
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String jsonData = data.toString();
        Log.d("BLEServer", "Sending data: " + jsonData); // JSON 데이터 로그 출력

        RequestBody body = RequestBody.create(JSON, jsonData);
        Request request = new Request.Builder()
                .url("http://192.168.7.132:5055/api/upload")
                .post(body)
                .build();

        // 서버로 데이터를 비동기적으로 전송
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.e("BLEServer", "onFailure: " + e.getMessage()); // 오류 메시지 로그 출력
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String responseData = response.body().string();
                        JSONObject jsonResponse = new JSONObject(responseData);

                        // 서버 응답에서 키포인트 정보 추출
                        JSONArray keypointsJson = jsonResponse.optJSONArray("keypoints");
                        if (keypointsJson != null) {
                            ArrayList<FragmentCam.KeyPoint> keypoints = new ArrayList<>();
                            for (int i = 0; i < keypointsJson.length(); i++) {
                                JSONObject keypointJson = keypointsJson.getJSONObject(i);
                                int id = keypointJson.getInt("id");
                                float x = (float) keypointJson.getDouble("x");
                                float y = (float) keypointJson.getDouble("y");
                                FragmentCam.KeyPoint keypoint = fragmentCam.new KeyPoint(id, x, y); // FragmentCam의 인스턴스 필요
                                keypoints.add(keypoint);
                            }

                            // FragmentCam에 키포인트 정보 업데이트 메서드 호출
                            FragmentCam fragmentCam = findFragmentCam();
                            if (fragmentCam != null) {
                                fragmentCam.updateKeypoints(keypoints);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    // 서버로부터의 오류 응답 처리
                }
            }
        });
    }
    // 주기적으로 데이터를 서버로 전송
    private void sendDataPeriodically() {
        JSONArray allSensorDataArray = new JSONArray();

        try {
            for (int i = 1; i <= 6; i++) {
                String deviceName = "BCAP " + i;
                List<SensorDataStore.SensorData> dataList = SensorDataStore.getInstance().getAllDataForDeviceName(deviceName);

                if (!dataList.isEmpty()) {
                    JSONArray sensorDataJson = sensorDataToJsonArray(dataList);
                    for (int j = 0; j < sensorDataJson.length(); j++) {
                        allSensorDataArray.put(sensorDataJson.get(j));  // 개별 센서 데이터를 전체 배열에 추가
                    }

                    // 전송한 데이터 초기화
                    SensorDataStore.getInstance().clearDataForDevice(deviceName);
                }
            }

            if (allSensorDataArray.length() > 0) {
                JSONObject finalDataToSend = new JSONObject();
                finalDataToSend.put("sensorData", allSensorDataArray);
                sendToServer(finalDataToSend);  // 수정된 데이터를 서버로 전송
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
                jsonObject.put("gx", data.gx); // 자이로스코프 데이터 추가
                jsonObject.put("gy", data.gy);
                jsonObject.put("gz", data.gz);
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
            handler.postDelayed(this, 500); // 1초 후에 다시 실행
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
    private FragmentCam findFragmentCam() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof FragmentCam) {
                return (FragmentCam) fragment;
            }
        }
        return null;
    }

    public class KeyPoint {
        private int id;
        private float x, y;

        public KeyPoint(int id, float x, float y) {
            this.id = id;
            this.x = x;
            this.y = y;
        }

        // Getter 메서드
        public int getId() { return id; }
        public float getX() { return x; }
        public float getY() { return y; }
    }
}
