package com.sam.bmecapstone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;

import java.net.CookieManager;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class FragmentCam extends Fragment {
    private static final int REQUEST_IMAGE_CAPTURE = 672;
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001; // 추가: 권한 요청 코드
    private Uri photoUri;
    private TextView txtTerminal;
    private CameraDevice cameraDevice;
    private CameraCaptureSession captureSession;
    private ImageReader imageReader;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    public TextureView textureView;
    public FrameLayout frameLayout;
    public TextureView overlayTextureView;
    private Paint paint;

    private XYPlot plot1, plot2, plot3, plot4, plot5, plot6;
    private SimpleXYSeries series1, series2, series3, series4, series5, series6;
    private final int MAX_DATA_POINTS = 30;
    private Queue<Float> dataQueue1 = new LinkedList<>();
    private Queue<Float> dataQueue2 = new LinkedList<>();
    private Queue<Float> dataQueue3 = new LinkedList<>();
    private Queue<Float> dataQueue4 = new LinkedList<>();
    private Queue<Float> dataQueue5 = new LinkedList<>();
    private Queue<Float> dataQueue6 = new LinkedList<>();


    // 외부에서 호출할 수 있는 공개 메서드
    public void appendText(String text) {
        if (txtTerminal != null) {
            txtTerminal.append(text);
        }
    }
    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("Camera Background");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public FragmentCam(){

    }
    // 차트 업데이트 메서드
    private int dataIndex = 0;
    public void updateChart(String deviceName, char dtype, float ax, float ay, float az) {
        getActivity().runOnUiThread(() -> {
            // 예: 첫 번째 장치의 X축 데이터를 차트1에 표시
            if (deviceName.equals("BCAP 1")) {
                if (dtype=='A'){
                    dataQueue1.add(ax);
                    dataQueue1.add(ay);
                    dataQueue1.add(az);
                } else if (dtype=='G') {
                }
                if (dataQueue1.size() > MAX_DATA_POINTS * 3) {
                    // 큐에 있는 데이터 개수가 최대 데이터 개수의 3배를 초과하면
                    // 초과한 데이터를 큐에서 제거합니다.
                    for (int i = 0; i < dataQueue1.size() - MAX_DATA_POINTS * 3; i++) {
                        dataQueue1.poll();
                    }
                }
                series1.setModel((List<? extends Number>) dataQueue1, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
                plot1.redraw();
            } else if (deviceName.equals("BCAP 2")) {
                if (dtype=='A'){
                    dataQueue2.add(ax);
                    dataQueue2.add(ay);
                    dataQueue2.add(az);
                } else if (dtype=='G') {
                }
                if (dataQueue2.size() > MAX_DATA_POINTS * 3) {
                    // 큐에 있는 데이터 개수가 최대 데이터 개수의 3배를 초과하면
                    // 초과한 데이터를 큐에서 제거합니다.
                    for (int i = 0; i < dataQueue2.size() - MAX_DATA_POINTS * 3; i++) {
                        dataQueue2.poll();
                    }
                }
                series2.setModel((List<? extends Number>) dataQueue2, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
                plot2.redraw();
            } else if (deviceName.equals("BCAP 3")) {
                if (dtype=='A'){
                    dataQueue3.add(ax);
                    dataQueue3.add(ay);
                    dataQueue3.add(az);
                } else if (dtype=='G') {
                }
                if (dataQueue3.size() > MAX_DATA_POINTS * 3) {
                    // 큐에 있는 데이터 개수가 최대 데이터 개수의 3배를 초과하면
                    // 초과한 데이터를 큐에서 제거합니다.
                    for (int i = 0; i < dataQueue3.size() - MAX_DATA_POINTS * 3; i++) {
                        dataQueue3.poll();
                    }
                }
                series3.setModel((List<? extends Number>) dataQueue3, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
                plot3.redraw();
            } else if (deviceName.equals("BCAP 4")) {

                if (dtype=='A'){
                    dataQueue4.add(ax);
                    dataQueue4.add(ay);
                    dataQueue4.add(az);
                } else if (dtype=='G') {
                }
                if (dataQueue4.size() > MAX_DATA_POINTS * 3) {
                    // 큐에 있는 데이터 개수가 최대 데이터 개수의 3배를 초과하면
                    // 초과한 데이터를 큐에서 제거합니다.
                    for (int i = 0; i < dataQueue4.size() - MAX_DATA_POINTS * 3; i++) {
                        dataQueue4.poll();
                    }
                }
                series4.setModel((List<? extends Number>) dataQueue4, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
                plot4.redraw();
            } else if (deviceName.equals("BCAP 5")) {

                if (dtype=='A'){
                    dataQueue5.add(ax);
                    dataQueue5.add(ay);
                    dataQueue5.add(az);
                } else if (dtype=='G') {
                }
                if (dataQueue5.size() > MAX_DATA_POINTS * 3) {
                    // 큐에 있는 데이터 개수가 최대 데이터 개수의 3배를 초과하면
                    // 초과한 데이터를 큐에서 제거합니다.
                    for (int i = 0; i < dataQueue5.size() - MAX_DATA_POINTS * 3; i++) {
                        dataQueue5.poll();
                    }
                }
                series5.setModel((List<? extends Number>) dataQueue5, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
                plot5.redraw();
            } else if (deviceName.equals("BCAP 6")) {

                if (dtype=='A'){
                    dataQueue6.add(ax);
                    dataQueue6.add(ay);
                    dataQueue6.add(az);
                } else if (dtype=='G') {
                }
                if (dataQueue6.size() > MAX_DATA_POINTS * 3) {
                    // 큐에 있는 데이터 개수가 최대 데이터 개수의 3배를 초과하면
                    // 초과한 데이터를 큐에서 제거합니다.
                    for (int i = 0; i < dataQueue6.size() - MAX_DATA_POINTS * 3; i++) {
                        dataQueue6.poll();
                    }
                }
                series6.setModel((List<? extends Number>) dataQueue6, SimpleXYSeries.ArrayFormat.Y_VALS_ONLY);
                plot6.redraw();
            }
            // ... 다른 장치 및 차트에 대한 로직 ...
        });
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cam, container, false);
    }


    // Paint 객체 초기화 메서드
    private void initPaint() {
        paint = new Paint();
        paint.setColor(Color.RED); // 키포인트 색상 설정
        paint.setStyle(Paint.Style.FILL); // 키포인트 스타일 설정
        paint.setStrokeWidth(10); // 선의 두께 설정
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        initPaint();

        // 차트 초기화
        plot1 = view.findViewById(R.id.chart1);
        plot2 = view.findViewById(R.id.chart2);
        plot3 = view.findViewById(R.id.chart3);
        plot4 = view.findViewById(R.id.chart4);
        plot5 = view.findViewById(R.id.chart5);
        plot6 = view.findViewById(R.id.chart6);

        series1 = new SimpleXYSeries("Left hand");
        series2 = new SimpleXYSeries("Right hand");
        series3 = new SimpleXYSeries("Left Arm");
        series4 = new SimpleXYSeries("Right Arm");
        series5 = new SimpleXYSeries("Left foot");
        series6 = new SimpleXYSeries("Right foot");

        plot1.addSeries(series1, new LineAndPointFormatter(Color.BLUE, null, null, null));
        plot2.addSeries(series2, new LineAndPointFormatter(Color.BLUE, null, null, null));
        plot3.addSeries(series3, new LineAndPointFormatter(Color.BLUE, null, null, null));
        plot4.addSeries(series4, new LineAndPointFormatter(Color.BLUE, null, null, null));
        plot5.addSeries(series5, new LineAndPointFormatter(Color.BLUE, null, null, null));
        plot6.addSeries(series6, new LineAndPointFormatter(Color.BLUE, null, null, null));
        try {
            textureView = getView().findViewById(R.id.tv_result);
            overlayTextureView = getView().findViewById(R.id.tv_overlay);
            frameLayout = getView().findViewById(R.id.frame_layout); // 여기서 문제가 발생한다면
        } catch (Exception e) {
            Log.e("BLE_CANVAS", "Error finding views", e); // 로그를 출력
        }
        textureView = getView().findViewById(R.id.tv_result);
        overlayTextureView = getView().findViewById(R.id.tv_overlay);
        frameLayout = getView().findViewById(R.id.frame_layout); // FrameLayout의 ID를 지정하세요.


        txtTerminal = getView().findViewById(R.id.txt_terminal);
        txtTerminal.setMovementMethod(new ScrollingMovementMethod()); // 스크롤가능
        if (txtTerminal != null) {
            txtTerminal.setMovementMethod(new ScrollingMovementMethod());
            txtTerminal.append("\n" + "[안내] 측정가능 여부를 확인하고 있습니다..");

            // Bluetooth 연결 여부 확인
            txtTerminal.append("\nTest");
            Boolean BLE_Connect = ((MainActivity) getActivity()).allDevicesConnected();
            txtTerminal.append("\n" + BLE_Connect);
            if (BLE_Connect) { // 예: MainActivity에 allDevicesConnected() 메서드가 블루투스 연결을 확인합니다.
                txtTerminal.append("\n" + "[안내] 6개의 블루투스 기기가 연결되어있습니다.");
                if (checkCameraPermission()) {
                    txtTerminal.append("\n" + "[안내] 카메라 권한이 확인되었습니다.");
                    // 다 완료시 통신 테스트 진행
                    // MainActivity에 있는 btn_CAM 버튼을 누른다면 진행!
                    // 해당 프래그먼트에서 이 조건문들이 만족할때만 버튼클릭시 카메라 작동부가 작동해야함
                } else {
                    txtTerminal.append("\n" + "[안내] 카메라 권한이 필요합니다.");
                }
            }
        }
        textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                openCamera();
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                // Optional: If needed, handle the change in texture size here.
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return true; // Return true if you want the SurfaceTexture to be released, otherwise return false.
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                // Handle updates to the SurfaceTexture if needed.
            }
        });

    }

    private boolean checkCameraPermission() {
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }
    static class CompareSizesByArea implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }
    }
    private void openCamera() {
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);

        try {
            String[] cameraIdList = manager.getCameraIdList();
            String cameraId = null;
            CameraCharacteristics characteristics = null;

            for (String id : cameraIdList) {
                characteristics = manager.getCameraCharacteristics(id);
                Integer lensFacing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
                    cameraId = id;
                    break;
                }
            }

            if (cameraId == null) {
                Log.e("Camera", "No front-facing camera found.");
                return;
            }
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map != null) {
                Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
                Size largestSize = Collections.max(Arrays.asList(sizes), new CompareSizesByArea());
                textureView.getSurfaceTexture().setDefaultBufferSize(largestSize.getWidth(), largestSize.getHeight());
                // 카메라를 여는 부분 추가
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                        @Override
                        public void onOpened(@NonNull CameraDevice camera) {
                            cameraDevice = camera;

                            startCaptureSession(largestSize.getWidth(), largestSize.getHeight()); // 예제에서 사용했던 세션 시작 메서드 호출
                        }

                        @Override
                        public void onDisconnected(@NonNull CameraDevice camera) {
                            camera.close();
                            cameraDevice = null;
                        }

                        @Override
                        public void onError(@NonNull CameraDevice camera, int error) {
                            camera.close();
                            cameraDevice = null;
                            Log.e("Camera", "Error opening camera: " + error);
                        }
                    }, backgroundHandler);
                } else {
                    // 권한 요청 또는 관련 메시지 표시
                }
            } else {
                Log.e("Camera", "SCALER_STREAM_CONFIGURATION_MAP is null.");
            }


        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.e("Camera", "Error accessing camera: " + e.getMessage());
        }
    }

    private void startCaptureSession(int maxWidth, int maxHeight) {
        try {
            CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            // Directly get the SurfaceTexture from the TextureView
            SurfaceTexture texture = textureView.getSurfaceTexture();

            // Set the buffer size to the desired maximum resolution
            if (texture != null) {
                texture.setDefaultBufferSize(maxWidth, maxHeight);
                Surface surface = new Surface(texture);
                requestBuilder.addTarget(surface);

                cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        captureSession = session;
                        startRepeatingRequest(maxWidth, maxHeight);
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        Log.e("CameraCaptureSession", "Configuration failed");
                    }

                    // ... 나머지 콜백 메서드들...

                }, backgroundHandler);
            } else {
                Log.e("CameraCaptureSession", "SurfaceTexture is null");
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.e("CameraCaptureSession", "Error starting capture session: " + e.getMessage());
        }
    }


    private Rect calculateCropRectForSquare(int maxWidth, int maxHeight) {
        int desiredSize = Math.min(maxWidth, maxHeight);

        int xCenter = maxWidth / 2;
        int yCenter = maxHeight / 2;

        int left = xCenter - (desiredSize / 2);
        int top = yCenter - (desiredSize / 2);
        int right = xCenter + (desiredSize / 2);
        int bottom = yCenter + (desiredSize / 2);

        return new Rect(left, top, right, bottom);
    }

    private void startRepeatingRequest(int maxWidth, int maxHeight) {
        try {
            CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            SurfaceTexture texture = textureView.getSurfaceTexture();
            if (texture != null) {
                texture.setDefaultBufferSize(maxWidth, maxHeight);
                Surface surface = new Surface(texture);
                requestBuilder.addTarget(surface);

                // 중앙에서부터 1:1로 crop 설정
                Rect cropRect = calculateCropRectForSquare(maxWidth, maxHeight);
                Toast.makeText(getActivity(),cropRect+"",Toast.LENGTH_LONG).show();
                requestBuilder.set(CaptureRequest.SCALER_CROP_REGION, cropRect);

                CaptureRequest request = requestBuilder.build();
                captureSession.setRepeatingRequest(request, null, backgroundHandler);
            } else {
                Log.e("Camera", "SurfaceTexture is null");
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.e("Camera", "Error starting capture session: " + e.getMessage());
        }
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        // 다시 들어왔을때 치료를 계속 진행할 것인지
        // 뷰를 초기화하고 연결 다시하라고할것인지?
    }

    @Override
    public void onPause() {
        stopBackgroundThread();
        super.onPause();
    }

    public Bitmap getCurrentPreviewBitmap() {
        if (textureView.isAvailable()) {
            Bitmap bitmap = textureView.getBitmap();
            return bitmap;
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
    // POSE_PAIRS를 Java 배열로 정의
    int[][] posePairs = {
            // 얼굴
            {1, 2}, {1, 3}, {2, 4}, {3, 5},
            // 상체
            {6, 7}, {6, 8}, {7, 9}, {8, 10}, {9, 11},
            // 하체
            {12, 13}, {12, 14}, {13, 15}, {14, 16}, {15, 17}
    };

    public void updateKeypoints(List<KeyPoint> keypoints) {
        overlayTextureView.setOpaque(false);

        getActivity().runOnUiThread(() -> {
            TextureView overlayTextureView = getView().findViewById(R.id.tv_overlay);
            if (overlayTextureView.isAvailable()) {
                final Canvas canvas = overlayTextureView.lockCanvas();
                if (canvas == null) {
                    Log.w("BLE_CANVAS", "Overlay Canvas is null, skipping drawing");
                    return;
                }
                try {
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR); // 기존 그림 지우기

                    // 키포인트에 점 그리기
                    for (KeyPoint keypoint : keypoints) {
                        float x = keypoint.x * canvas.getWidth();
                        float y = keypoint.y * canvas.getHeight();
                        canvas.drawCircle(x, y, 10, paint); // 키포인트 그리기
                    }

                    // 스켈레톤 그리기
                    for (int[] pair : posePairs) {
                        int partA = pair[0] - 1;
                        int partB = pair[1] - 1;

                        // 각 키포인트가 유효한지 확인
                        if (partA < keypoints.size() && partB < keypoints.size()) {
                            KeyPoint kpA = keypoints.get(partA);
                            KeyPoint kpB = keypoints.get(partB);
                            if (kpA != null && kpB != null && kpA.x != 0 && kpA.y != 0 && kpB.x != 0 && kpB.y != 0) {
                                float x1 = kpA.x * canvas.getWidth();
                                float y1 = kpA.y * canvas.getHeight();
                                float x2 = kpB.x * canvas.getWidth();
                                float y2 = kpB.y * canvas.getHeight();

                                // 선을 그립니다.
                                canvas.drawLine(x1, y1, x2, y2, paint);
                            }
                        }
                    }
                } finally {
                    overlayTextureView.unlockCanvasAndPost(canvas);
                }
            }
        });
    }
}
