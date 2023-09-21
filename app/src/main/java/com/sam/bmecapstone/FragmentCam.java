package com.sam.bmecapstone;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentCam extends Fragment {

    private TextView txtTerminal;
    public FragmentCam(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cam, container, false);
    }

    @Override
    public void onStart(){
        super.onStart();

        txtTerminal = getView().findViewById(R.id.txt_terminal);
        if (txtTerminal != null){
            txtTerminal.setMovementMethod(new ScrollingMovementMethod());
            txtTerminal.append("\n" + "[안내] 측정가능 여부를 확인하고 있습니다..");


            if (true){ // 로그인 여부
                if (true){ // Bluetooth 연결 여부
                    if (true){ // 카메라 사용가능 여부
                        // 다 완료시 통신 테스트 진행
                        
                    }
                    
                }
                
            }
            // 측정 가능 조건 : 블루투스 6개 연결, 캡 사용 가능, 서버와 통신을 통해..
            // 사용자의 정보를 기입.. (mainActivity 이전에 로그인 창을 통해 진행)
            // 사용자가 허가를 받았는지 확인

        }
    }
}
