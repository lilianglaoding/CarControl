package com.liangyi.carcontrol;

import static java.lang.String.format;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.liangyi.carcontrol.RockView.RockerView;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener {

    private static final int SENSITIVE_LEVEL = 4;

    private Button cs;
    private Button akm;
    private Button mknm;
    private EditText ip;
    private EditText port;
    private Button steer1_up;
    private Button steer1_down;
    private Button steer2_up;
    private Button steer2_down;
    private Button steer3_in;
    private Button steer3_out;
    private RockerView rocker;
    private RockerView rotate;
    private Button left_rotate;
    private Button right_rotate;

    private WebView webView;
    private MediaController mMediaController;
    //private VideoView video;
    private Socket socket;
    private OutputStream os;
    private PrintWriter pw;
    private boolean is_connected = false;
    private RockerView.Direction last_direction = RockerView.Direction.DIRECTION_CENTER;

    /*
    private enum Command {
        TURN_LEFT, // 左
        TURN_RIGHT, // 右
        TURN_UP, // 上
        TURN_DOWN, // 下
        TURN_UP_LEFT, // 左上
        TURN_UP_RIGHT, // 右上
        TURN_DOWN_LEFT, // 左下
        TURN_DOWN_RIGHT, // 右下
        LEFT_ROTATE, // 左转
        RIGHT_ROTATE, // 右转
        FRONT_LEFT_ROTATE, // 沿前轴中点左旋
        FRONT_RIGHT_ROTATE, // 沿前轴中点右旋
        REAR_LEFT_ROTATE, // 沿后轴中点左旋
        REAR_RIGHT_ROTATE, // 沿后轴中点右旋
        STOP, // 停止
        CS, // 差速模式
        AKM, // 阿克曼模式
        MKNM // 麦克纳姆模式
    }*/

    // direction:
/*
     5  6  7
      \ | /
       \|/
   4 ------- 0
       /|\
      / | \
     3  2  1

     8 - left rotate
     9 - right rotate
     10 - FRONT_LEFT_ROTATE, // 沿前轴中点左旋
     11 - FRONT_RIGHT_ROTATE, // 沿前轴中点右旋
     12 - REAR_LEFT_ROTATE, // 沿后轴中点左旋
     13 - REAR_RIGHT_ROTATE, // 沿后轴中点右旋
*/

    private enum Command {
        TURN_RIGHT, // 右 0
        TURN_DOWN_RIGHT, // 右下 1
        TURN_DOWN, // 下 2
        TURN_DOWN_LEFT, // 左下 3
        TURN_LEFT, // 左 4
        TURN_UP_LEFT, // 左上 5
        TURN_UP, // 上 6
        TURN_UP_RIGHT, // 右上 7
        LEFT_ROTATE, // 左转 8
        RIGHT_ROTATE, // 右转 9
        FRONT_LEFT_ROTATE, // 沿前轴中点左旋 10
        FRONT_RIGHT_ROTATE, // 沿前轴中点右旋 11
        REAR_LEFT_ROTATE, // 沿后轴中点左旋 12
        REAR_RIGHT_ROTATE, // 沿后轴中点右旋 13
        STOP, // 停止 14
        CS, // 差速模式 15
        AKM, // 阿克曼模式 16
        MKNM, // 麦克纳姆模式 17
        S1_UP, // 1号舵机升起 18
        S1_DOWN, // 1号舵机下降 19
        S1_STOP, // 1号舵机停止 20
        S2_UP, // 2号舵机升起 21
        S2_DOWN, // 2号舵机下降 22
        S2_STOP, // 2号舵机停止 23
        S3_IN, // 3号舵机向内夹住 24
        S3_OUT, // 3号舵机向外打开 25
        S3_STOP // 3号舵机停止 26
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        rocker = (RockerView) findViewById(R.id.rocker);
        rocker.setOnShakeAngleDistanceLevelListener(new RockerView.OnShakeAngleDistanceLevelListener() {
            @Override
            public void onStart() {
                //rotate.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onShakeAngleDistanceLevel(RockerView.Direction direction, double angle, int level) {
                if (is_connected == false) {
                    Toast.makeText(getApplicationContext(), "Please connect car first", Toast.LENGTH_LONG).show();
                    return;
                }
                if (direction != last_direction && level > SENSITIVE_LEVEL) {
                    switch (direction) {
                        case DIRECTION_LEFT:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String command = format("%02d", Command.TURN_LEFT.ordinal());
                                        os = socket.getOutputStream();
                                        pw = new PrintWriter(os);
                                        pw.write(command);
                                        pw.flush();
                                    } catch (IOException e) {
                                        Log.e("carcontrol/turn_left", e.toString());
                                    }
                                }
                            }).start();
                            break;
                        case DIRECTION_RIGHT:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String command = format("%02d", Command.TURN_RIGHT.ordinal());
                                        os = socket.getOutputStream();
                                        pw = new PrintWriter(os);
                                        pw.write(command);
                                        pw.flush();
                                    } catch (IOException e) {
                                        Log.e("carcontrol/turn_right", e.toString());
                                    }
                                }
                            }).start();
                            break;
                        case DIRECTION_UP:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String command = format("%02d", Command.TURN_UP.ordinal());
                                        os = socket.getOutputStream();
                                        pw = new PrintWriter(os);
                                        pw.write(command);
                                        pw.flush();
                                    } catch (IOException e) {
                                        Log.e("carcontrol/turn_up", e.toString());
                                    }
                                }
                            }).start();
                            break;
                        case DIRECTION_DOWN:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String command = format("%02d", Command.TURN_DOWN.ordinal());
                                        os = socket.getOutputStream();
                                        pw = new PrintWriter(os);
                                        pw.write(command);
                                        pw.flush();
                                    } catch (IOException e) {
                                        Log.e("carcontrol/turn_down", e.toString());
                                    }
                                }
                            }).start();
                            break;
                        case DIRECTION_UP_LEFT:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String command = format("%02d", Command.TURN_UP_LEFT.ordinal());
                                        os = socket.getOutputStream();
                                        pw = new PrintWriter(os);
                                        pw.write(command);
                                        pw.flush();
                                    } catch (IOException e) {
                                        Log.e("carcontrol/turn_up_left", e.toString());
                                    }
                                }
                            }).start();
                            break;
                        case DIRECTION_UP_RIGHT:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String command = format("%02d", Command.TURN_UP_RIGHT.ordinal());
                                        os = socket.getOutputStream();
                                        pw = new PrintWriter(os);
                                        pw.write(command);
                                        pw.flush();
                                    } catch (IOException e) {
                                        Log.e("carcontrol/turn_up_right", e.toString());
                                    }
                                }
                            }).start();
                            break;
                        case DIRECTION_DOWN_LEFT:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String command = format("%02d", Command.TURN_DOWN_LEFT.ordinal());
                                        os = socket.getOutputStream();
                                        pw = new PrintWriter(os);
                                        pw.write(command);
                                        pw.flush();
                                    } catch (IOException e) {
                                        Log.e("carcontrol/turn_down_left", e.toString());
                                    }
                                }
                            }).start();
                            break;
                        case DIRECTION_DOWN_RIGHT:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String command = format("%02d", Command.TURN_DOWN_RIGHT.ordinal());
                                        os = socket.getOutputStream();
                                        pw = new PrintWriter(os);
                                        pw.write(command);
                                        pw.flush();
                                    } catch (IOException e) {
                                        Log.e("carcontrol/turn_down_right", e.toString());
                                    }
                                }
                            }).start();
                            break;
                        case DIRECTION_CENTER:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String command = format("%02d", Command.STOP.ordinal());
                                        os = socket.getOutputStream();
                                        pw = new PrintWriter(os);
                                        pw.write(command);
                                        pw.flush();
                                    } catch (IOException e) {
                                        Log.e("carcontrol/stop", e.toString());
                                    }
                                }
                            }).start();
                            break;
                        default:
                            break;
                    }
                    last_direction = direction;
                }
            }

            @Override
            public void onFinish() {
                if (is_connected == false) {
                    Toast.makeText(getApplicationContext(), "Please connect car first", Toast.LENGTH_LONG).show();
                    return;
                }
                //rotate.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String command = format("%02d", Command.STOP.ordinal());
                            os = socket.getOutputStream();
                            pw = new PrintWriter(os);
                            pw.write(command);
                            pw.flush();
                        } catch (IOException e) {
                            Log.e("carcontrol/on_finish_stop", e.toString());
                        }
                    }
                }).start();
                last_direction = RockerView.Direction.DIRECTION_CENTER;
            }
        });

        rotate = (RockerView) findViewById(R.id.rotate);
        rotate.mDirectionMode = RockerView.DirectionMode.DIRECTION_4_ROTATE_0;
        rotate.setOnShakeAngleDistanceLevelListener(new RockerView.OnShakeAngleDistanceLevelListener() {
            @Override
            public void onStart() {
                //rocker.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onShakeAngleDistanceLevel(RockerView.Direction direction, double angle, int level) {
                if (is_connected == false) {
                    Toast.makeText(getApplicationContext(), "Please connect car first", Toast.LENGTH_LONG).show();
                    return;
                }
                if (direction !=last_direction && level > SENSITIVE_LEVEL) {
                    switch (direction) {
                        case DIRECTION_UP_LEFT:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String command = format("%02d", Command.FRONT_LEFT_ROTATE.ordinal());
                                        os = socket.getOutputStream();
                                        pw = new PrintWriter(os);
                                        pw.write(command);
                                        pw.flush();
                                    } catch (IOException e) {
                                        Log.e("carcontrol/turn_up_left", e.toString());
                                    }
                                }
                            }).start();
                            break;
                        case DIRECTION_UP_RIGHT:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String command = format("%02d", Command.FRONT_RIGHT_ROTATE.ordinal());
                                        os = socket.getOutputStream();
                                        pw = new PrintWriter(os);
                                        pw.write(command);
                                        pw.flush();
                                    } catch (IOException e) {
                                        Log.e("carcontrol_rotate/turn_up_right", e.toString());
                                    }
                                }
                            }).start();
                            break;
                        case DIRECTION_DOWN_LEFT:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String command = format("%02d", Command.REAR_LEFT_ROTATE.ordinal());
                                        os = socket.getOutputStream();
                                        pw = new PrintWriter(os);
                                        pw.write(command);
                                        pw.flush();
                                    } catch (IOException e) {
                                        Log.e("carcontrol_rotate/turn_down_left", e.toString());
                                    }
                                }
                            }).start();
                            break;
                        case DIRECTION_DOWN_RIGHT:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String command = format("%02d", Command.REAR_RIGHT_ROTATE.ordinal());
                                        os = socket.getOutputStream();
                                        pw = new PrintWriter(os);
                                        pw.write(command);
                                        pw.flush();
                                    } catch (IOException e) {
                                        Log.e("carcontrol_rotate/turn_down_right", e.toString());
                                    }
                                }
                            }).start();
                            break;
                        case DIRECTION_CENTER:
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        String command = format("%02d", Command.STOP.ordinal());
                                        os = socket.getOutputStream();
                                        pw = new PrintWriter(os);
                                        pw.write(command);
                                        pw.flush();
                                    } catch (IOException e) {
                                        Log.e("carcontrol_rotate/stop", e.toString());
                                    }
                                }
                            }).start();
                            break;
                        default:
                            break;
                    }
                    last_direction = direction;
                }
            }

            @Override
            public void onFinish() {
                if (is_connected == false) {
                    Toast.makeText(getApplicationContext(), "Please connect car first", Toast.LENGTH_LONG).show();
                    return;
                }
                //rocker.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String command = format("%02d", Command.STOP.ordinal());
                            os = socket.getOutputStream();
                            pw = new PrintWriter(os);
                            pw.write(command);
                            pw.flush();
                        } catch (IOException e) {
                            Log.e("carcontrol/on_rotate_finish_stop", e.toString());
                        }
                    }
                }).start();
                last_direction = RockerView.Direction.DIRECTION_CENTER;
            }
        });

        ip = (EditText) findViewById(R.id.ip);
        port = (EditText) findViewById(R.id.port);
        Button connect = (Button) findViewById(R.id.connect);
        connect.setOnClickListener(this);

        cs = (Button) findViewById(R.id.cs);
        cs.setOnClickListener(this);
        akm = (Button) findViewById(R.id.akm);
        akm.setOnClickListener(this);
        mknm = (Button) findViewById(R.id.mknm);
        mknm.setOnClickListener(this);
        mknm.setEnabled(false);
        cs.setEnabled(true);
        akm.setEnabled(true);

        steer1_up = (Button) findViewById(R.id.steer1_up);
        steer1_up.setOnTouchListener(this);
        steer1_down = (Button) findViewById(R.id.steer1_down);
        steer1_down.setOnTouchListener(this);
        steer2_up = (Button) findViewById(R.id.steer2_up);
        steer2_up.setOnTouchListener(this);
        steer2_down = (Button) findViewById(R.id.steer2_down);
        steer2_down.setOnTouchListener(this);
        steer3_in = (Button) findViewById(R.id.steer3_in);
        steer3_in.setOnTouchListener(this);
        steer3_out = (Button) findViewById(R.id.steer3_out);
        steer3_out.setOnTouchListener(this);

        left_rotate = (Button) findViewById(R.id.left_rotate);
        left_rotate.setOnTouchListener(this);
        right_rotate = (Button) findViewById(R.id.right_rotate);
        right_rotate.setOnTouchListener(this);

        webView = (WebView) findViewById(R.id.video);
        //webView.getSettings().setJavaScriptEnabled(true);
        //webView.setWebViewClient(new WebViewClient());

        mMediaController = new MediaController(this);
        //video = (VideoView) findViewById(R.id.video);
        //String url="http://192.168.1.11:8081/";//视频链接
        //webView.loadUrl(url);//打开指定URL的html文件

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.INTERNET}, 1);
        } else {
            initVideoView();
        }

        is_connected = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connect:
                String input_ip = ip.getText().toString().isEmpty() ? "192.168.1.121" : ip.getText().toString();
                int input_port = port.getText().toString().isEmpty() ? 12345: Integer.parseInt(port.getText().toString());
                String url="http://" + input_ip + ":8081/"; //视频链接
                webView.loadUrl(url);//打开指定URL的html文件
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                           socket = new Socket(input_ip, input_port);
                           if (socket == null) {
                               Toast.makeText(getApplicationContext(), "Connect car failed", Toast.LENGTH_LONG).show();
                               return;
                           }
                           is_connected = true;
                        } catch (IOException e) {
                            Log.e("carcontrol/connect", e.toString());
                        }
                    }
                }).start();
                is_connected = true;
                break;
            case R.id.cs:
                if (is_connected == false) {
                    Toast.makeText(getApplicationContext(), "Please connect car first", Toast.LENGTH_LONG).show();
                    return;
                }
                cs.setEnabled(false);
                akm.setEnabled(true);
                mknm.setEnabled(true);
                rotate.setVisibility(View.INVISIBLE);
                left_rotate.setVisibility(View.VISIBLE);
                right_rotate.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String command = format("%02d", Command.CS.ordinal());
                            os = socket.getOutputStream();
                            pw = new PrintWriter(os);
                            pw.write(command);
                            pw.flush();
                        } catch (IOException e) {
                            Log.e("carcontrol/cs", e.toString());
                        }
                    }
                }).start();
                break;
            case R.id.akm:
                if (is_connected == false) {
                    Toast.makeText(getApplicationContext(), "Please connect car first", Toast.LENGTH_LONG).show();
                    return;
                }
                akm.setEnabled(false);
                cs.setEnabled(true);
                mknm.setEnabled(true);
                rotate.setVisibility(View.GONE);
                left_rotate.setVisibility(View.GONE);
                right_rotate.setVisibility(View.GONE);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String command = format("%02d", Command.AKM.ordinal());
                            os = socket.getOutputStream();
                            pw = new PrintWriter(os);
                            pw.write(command);
                            pw.flush();
                        } catch (IOException e) {
                            Log.e("carcontrol/cs", e.toString());
                        }
                    }
                }).start();
                break;
            case R.id.mknm:
                if (is_connected == false) {
                    Toast.makeText(getApplicationContext(), "Please connect car first", Toast.LENGTH_LONG).show();
                    return;
                }
                mknm.setEnabled(false);
                akm.setEnabled(true);
                cs.setEnabled(true);
                rotate.setVisibility(View.VISIBLE);
                left_rotate.setVisibility(View.VISIBLE);
                right_rotate.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String command = format("%02d", Command.MKNM.ordinal());
                            os = socket.getOutputStream();
                            pw = new PrintWriter(os);
                            pw.write(command);
                            pw.flush();
                        } catch (IOException e) {
                            Log.e("carcontrol/mknm", e.toString());
                        }
                    }
                }).start();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (is_connected == false) {
            Toast.makeText(getApplicationContext(), "Please connect car first", Toast.LENGTH_LONG).show();
            return true;
        }
        switch (view.getId()) {
            case R.id.left_rotate:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String command = format("%02d", Command.LEFT_ROTATE.ordinal());
                                os = socket.getOutputStream();
                                pw = new PrintWriter(os);
                                pw.write(command);
                                pw.flush();
                            } catch (IOException e) {
                                Log.e("carcontrol/left_rotate", e.toString());
                            }
                        }
                    }).start();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String command = format("%02d", Command.STOP.ordinal());
                                os = socket.getOutputStream();
                                pw = new PrintWriter(os);
                                pw.write(command);
                                pw.flush();
                            } catch (IOException e) {
                                Log.e("carcontrol/on_rotate_finish_stop", e.toString());
                            }
                        }
                    }).start();
                }
                break;
            case R.id.right_rotate:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String command = format("%02d", Command.RIGHT_ROTATE.ordinal());
                                os = socket.getOutputStream();
                                pw = new PrintWriter(os);
                                pw.write(command);
                                pw.flush();
                            } catch (IOException e) {
                                Log.e("carcontrol/right_rotate", e.toString());
                            }
                        }
                    }).start();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String command = format("%02d", Command.STOP.ordinal());
                                os = socket.getOutputStream();
                                pw = new PrintWriter(os);
                                pw.write(command);
                                pw.flush();
                            } catch (IOException e) {
                                Log.e("carcontrol/on_rotate_finish_stop", e.toString());
                            }
                        }
                    }).start();
                }
                break;
            case R.id.steer1_up:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String command = format("%02d", Command.S1_UP.ordinal());
                                os = socket.getOutputStream();
                                pw = new PrintWriter(os);
                                pw.write(command);
                                pw.flush();
                            } catch (IOException e) {
                                Log.e("carcontrol/right_rotate", e.toString());
                            }
                        }
                    }).start();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String command = format("%02d", Command.S1_STOP.ordinal());
                                os = socket.getOutputStream();
                                pw = new PrintWriter(os);
                                pw.write(command);
                                pw.flush();
                            } catch (IOException e) {
                                Log.e("carcontrol/on_rotate_finish_stop", e.toString());
                            }
                        }
                    }).start();
                }
                break;
            case R.id.steer1_down:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String command = format("%02d", Command.S1_DOWN.ordinal());
                                os = socket.getOutputStream();
                                pw = new PrintWriter(os);
                                pw.write(command);
                                pw.flush();
                            } catch (IOException e) {
                                Log.e("carcontrol/right_rotate", e.toString());
                            }
                        }
                    }).start();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String command = format("%02d", Command.S1_STOP.ordinal());
                                os = socket.getOutputStream();
                                pw = new PrintWriter(os);
                                pw.write(command);
                                pw.flush();
                            } catch (IOException e) {
                                Log.e("carcontrol/on_rotate_finish_stop", e.toString());
                            }
                        }
                    }).start();
                }
                break;
            case R.id.steer2_up:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String command = format("%02d", Command.S2_UP.ordinal());
                                os = socket.getOutputStream();
                                pw = new PrintWriter(os);
                                pw.write(command);
                                pw.flush();
                            } catch (IOException e) {
                                Log.e("carcontrol/right_rotate", e.toString());
                            }
                        }
                    }).start();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String command = format("%02d", Command.S2_STOP.ordinal());
                                os = socket.getOutputStream();
                                pw = new PrintWriter(os);
                                pw.write(command);
                                pw.flush();
                            } catch (IOException e) {
                                Log.e("carcontrol/on_rotate_finish_stop", e.toString());
                            }
                        }
                    }).start();
                }
                break;
            case R.id.steer2_down:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String command = format("%02d", Command.S2_DOWN.ordinal());
                                os = socket.getOutputStream();
                                pw = new PrintWriter(os);
                                pw.write(command);
                                pw.flush();
                            } catch (IOException e) {
                                Log.e("carcontrol/right_rotate", e.toString());
                            }
                        }
                    }).start();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String command = format("%02d", Command.S2_STOP.ordinal());
                                os = socket.getOutputStream();
                                pw = new PrintWriter(os);
                                pw.write(command);
                                pw.flush();
                            } catch (IOException e) {
                                Log.e("carcontrol/on_rotate_finish_stop", e.toString());
                            }
                        }
                    }).start();
                }
                break;
            case R.id.steer3_in:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String command = format("%02d", Command.S3_IN.ordinal());
                                os = socket.getOutputStream();
                                pw = new PrintWriter(os);
                                pw.write(command);
                                pw.flush();
                            } catch (IOException e) {
                                Log.e("carcontrol/right_rotate", e.toString());
                            }
                        }
                    }).start();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String command = format("%02d", Command.S3_STOP.ordinal());
                                os = socket.getOutputStream();
                                pw = new PrintWriter(os);
                                pw.write(command);
                                pw.flush();
                            } catch (IOException e) {
                                Log.e("carcontrol/on_rotate_finish_stop", e.toString());
                            }
                        }
                    }).start();
                }
                break;
            case R.id.steer3_out:
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String command = format("%02d", Command.S3_OUT.ordinal());
                                os = socket.getOutputStream();
                                pw = new PrintWriter(os);
                                pw.write(command);
                                pw.flush();
                            } catch (IOException e) {
                                Log.e("carcontrol/right_rotate", e.toString());
                            }
                        }
                    }).start();
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                String command = format("%02d", Command.S3_STOP.ordinal());
                                os = socket.getOutputStream();
                                pw = new PrintWriter(os);
                                pw.write(command);
                                pw.flush();
                            } catch (IOException e) {
                                Log.e("carcontrol/on_rotate_finish_stop", e.toString());
                            }
                        }
                    }).start();
                }
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initVideoView();
                } else {
                    Toast.makeText(this, "没有足够的权限", Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            pw.close();
            os.close();
            socket.close();
        } catch (IOException e) {
            Log.e("carcontrol/Destroy", e.toString());
        }
    }

    private void initVideoView() {
        //String url="http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4";
        //String url = "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear2/prog_index.m3u8";
        String url="http://192.168.1.11:8081/stream.html";
        //video.setMediaController(new MediaController(this));
        //video.setVideoURI(Uri.parse(url));
        //video.setVideoPath("/storage/sdcard0/DCIM/Camera/hd.mp4");
        //video.setVideoPath(url);
        //video.start();
        //video.requestFocus();
        //Uri uri = Uri.parse()
        //video.setVideoPath("192.168.1.11:8081");
    }
}