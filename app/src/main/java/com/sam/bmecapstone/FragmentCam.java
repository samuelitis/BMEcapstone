package com.sam.bmecapstone;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
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
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.Collections;

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
    TextureView textureView;


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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cam, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        textureView = getView().findViewById(R.id.tv_result);
        txtTerminal = getView().findViewById(R.id.txt_terminal);
        txtTerminal.setMovementMethod(new ScrollingMovementMethod()); // 스크롤가능
        textureView.post(new Runnable() {
            @Override
            public void run() {
                int width = textureView.getWidth();
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
                textureView.setLayoutParams(params);
            }
        }); // 길이 높이 1:1로 고정
        if (txtTerminal != null) {
            txtTerminal.setMovementMethod(new ScrollingMovementMethod());
            txtTerminal.append("\n" + "[안내] 측정가능 여부를 확인하고 있습니다..");

            // 로그인 여부 확인 (임시로 true로 해둠)
            if (true) {
                txtTerminal.append("\n" + "[안내] 로그인이 되어 있습니다.");
                // 진단 받은 치료솔루션이 있는가? (임시로 true로 해둠)
                if (true) {
                    txtTerminal.append("\n" + "[안내] 치료 가능한 솔루션을 찾았습니다.");
                    // 어떤 치료인지 알려줄 것!

                    // Bluetooth 연결 여부 확인
                    Boolean BLE_Connect = ((MainActivity) getActivity()).allDevicesConnected();
                    BLE_Connect = true; // 삭제해야함
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
                } else {
                    txtTerminal.append("\n" + "[안내] 진단받은 치료 솔루션이 없습니다.");
                }
            } else {
                txtTerminal.append("\n" + "[안내] 로그인 여부 확인에 실패하였습니다.");
                txtTerminal.append("\n" + "[안내] 로그인을 진행한 뒤 시도해주세요");
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
    private void openCamera() {
        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIdList = manager.getCameraIdList();
            String cameraId = null;
            for (String id : cameraIdList) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
                // 원하는 카메라 방향 선택 (LENS_FACING_FRONT 또는 LENS_FACING_BACK)
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
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map != null) {
                // 원하는 해상도 선택
                Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
                int selectedWidth = 640; // 선택한 너비
                int selectedHeight = 480; // 선택한 높이
                Size selectedSize = null;
                for (Size size : sizes) {
                    if (size.getWidth() == selectedWidth && size.getHeight() == selectedHeight) {
                        selectedSize = size;
                        break;
                    }
                }
                if (selectedSize != null) {
                    // 선택한 해상도를 사용하도록 설정
                    textureView.getSurfaceTexture().setDefaultBufferSize(selectedSize.getWidth(), selectedSize.getHeight());
                } else {
                    Log.e("Camera", "Selected resolution not supported.");
                }
            }
            manager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    startCaptureSession();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    // 카메라와의 연결이 끊어졌을 때 수행할 작업을 여기에 추가합니다.
                    camera.close();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    // 카메라 오류가 발생했을 때 수행할 작업을 여기에 추가합니다.
                    camera.close();
                }
            }, backgroundHandler);
        } catch (CameraAccessException | SecurityException e) {
            e.printStackTrace();
            Log.e("BMECamera", "Error opening camera: " + e.getMessage()); // 오류 로그 추가
        }
    }

    private void startCaptureSession() {
        try {
            CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            SurfaceTexture texture = new SurfaceTexture(0);
            texture.setDefaultBufferSize(640, 480); // 예시로 설정한 해상도입니다.
            Surface surface = new Surface(textureView.getSurfaceTexture());
            requestBuilder.addTarget(surface);

            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    startRepeatingRequest();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    // 여기에 세션 구성이 실패했을 때의 처리를 추가합니다.
                    Log.e("CameraCaptureSession", "Configuration failed");
                }

                @Override
                public void onReady(@NonNull CameraCaptureSession session) {
                    // 세션이 준비되었을 때 여기에서 처리할 작업을 수행할 수 있습니다.
                }

                @Override
                public void onActive(@NonNull CameraCaptureSession session) {
                    // 세션이 활성화되었을 때 여기에서 처리할 작업을 수행할 수 있습니다.
                }

                @Override
                public void onClosed(@NonNull CameraCaptureSession session) {
                    // 세션이 닫혔을 때 여기에서 처리할 작업을 수행할 수 있습니다.
                }
                // 다른 콜백 메서드들도 구현함..
            }, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            Log.e("bmeCameraCaptureSession", "Error starting capture session: " + e.getMessage()); // 오류 로그 추가

        }
    }

    private Rect calculateCropRect(int fullWidth, int fullHeight, int desiredWidth, int desiredHeight) {
        int xCenter = fullWidth / 2;
        int yCenter = fullHeight / 2;

        int left = xCenter - (desiredWidth / 2);
        int top = yCenter - (desiredHeight / 2);
        int right = xCenter + (desiredWidth / 2);
        int bottom = yCenter + (desiredHeight / 2);

        return new Rect(left, top, right, bottom);
    }

    private void startRepeatingRequest() {
        try {
            CaptureRequest.Builder requestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            SurfaceTexture texture = textureView.getSurfaceTexture();
            if (texture != null) {
                texture.setDefaultBufferSize(640, 480);
                Surface surface = new Surface(texture);
                requestBuilder.addTarget(surface);

                // Crop 중앙 480x480 설정
                Rect cropRect = calculateCropRect(640, 480, 480, 480);
                requestBuilder.set(CaptureRequest.SCALER_CROP_REGION, cropRect);

                CaptureRequest request = requestBuilder.build();
                captureSession.setRepeatingRequest(request, null, backgroundHandler);
            } else {
                Log.e("Camera", "SurfaceTexture is null");
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
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

}
