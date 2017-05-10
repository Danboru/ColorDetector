package website.timrobinson.opencvtutorial;

import android.app.Activity;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends Activity implements OnTouchListener, CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mRgba;
    private Scalar mBlobColorHsv;
    private Scalar mBlobColorRgba;

    TextView touch_coordinates;
    TextView touch_color;

    double x = -1;
    double y = -1;

    //Fungsi untuk mengaembalikan ke keadaan awal
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    //Mengaktfkan view ketika status dari
                    mOpenCvCameraView.enableView();
                    mOpenCvCameraView.setOnTouchListener(MainActivity.this);
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    //Fungsi yang akan di jalankan setelah contruvtor di jalankan

    /**
     *
     * Fungsi ini berisikan beberapa perintah utama
     * yang akan menerima dan mengolah beberapa fungsi yang ada di dalam kelas ini
     * dan yang ada di dalam kelas library yang di gunakan
     *
     * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Request screen agar screen bisa fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Men set view yang akan di gunakan
        setContentView(R.layout.activity_main);

        //Menjaga kamera agar tetap menyala
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        touch_coordinates = (TextView) findViewById(R.id.touch_coordinates);
        touch_color = (TextView) findViewById(R.id.touch_color);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.opencv_tutorial_activity_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);
        } else {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    //
    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat();
        mBlobColorRgba = new Scalar(255);
        mBlobColorHsv = new Scalar(255);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        return mRgba;
    }

    /**
     * Fungs yang di gunakan untuk menerima action dari screen yang di sentuh
     * */
    @Override
    public boolean onTouch(View v, MotionEvent event) {

        int cols = mRgba.cols();
        int rows = mRgba.rows();

        //Get koordinat
        double yLow = (double)mOpenCvCameraView.getHeight() * 0.2401961;
        double yHigh = (double)mOpenCvCameraView.getHeight() * 0.7696078;

        double xScale = (double)cols / (double)mOpenCvCameraView.getWidth();
        double yScale = (double)rows / (yHigh - yLow);

        //Mendapatkan koordinat view dari action yang di berikan oleh user
        //dari action tersebuat data akan di simpan dan nantinya akan di tampilkan di view yang bersangkutan
        x = event.getX();
        y = event.getY();

        //
        y = y - yLow;

        x = x * xScale;
        y = y * yScale;

        if((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        touch_coordinates.setText("X: " + Double.valueOf(x) + ", Y: " + Double.valueOf(y));

        Rect touchedRect = new Rect();

        //Apa ini variable ini di gunakan untuk menympan koordinat yang duidapat dari even touch ?

        /**
         *
         *
         *
         * */
        touchedRect.x = (int)x;
        touchedRect.y = (int)y;

        touchedRect.width = 8;
        touchedRect.height = 8;

        //Varible yang masih belum tahu fungsi nya buat apa
        Mat touchedRegionRgba = mRgba.submat(touchedRect);
        Mat touchedRegionHsv = new Mat();
        Imgproc.cvtColor(touchedRegionRgba, touchedRegionHsv, Imgproc.COLOR_RGB2HSV_FULL);

        //Fungsi ini untuk menentukan
        mBlobColorHsv = Core.sumElems(touchedRegionHsv);
        int pointCount = touchedRect.width * touchedRect.height;

        for (int i = 0; i < mBlobColorHsv.val.length; i++)
        {
            mBlobColorHsv.val[i] /= pointCount;
        }

        //
        mBlobColorRgba = convertScalarHsv2Rgba(mBlobColorHsv);

        //Set color yang sudah di dapatkan dari proses sebelumnya
        /**
         * Setiap hasil yang di dapatkan akan di set ke dalam view yang sebelumnya sudah di tentukan
         * di sini di gunakan 2 view untuk menampilkan data yang sebelumnya sudah di dapatkan
         *
         * */
        touch_color.setText("Color: #" + String.format("%02X", (int)mBlobColorRgba.val[0])
                + String.format("%02X", (int)mBlobColorRgba.val[1]) //Melakukan
                + String.format("%02X", (int)mBlobColorRgba.val[2]));

//        touch_color.setTextColor(Color.rgb((int) mBlobColorRgba.val[0],
//                (int) mBlobColorRgba.val[1],
//                (int) mBlobColorRgba.val[2]));
//
//        touch_coordinates.setTextColor(Color.rgb((int)mBlobColorRgba.val[0],
//                (int)mBlobColorRgba.val[1],
//                (int)mBlobColorRgba.val[2]));

        //Menginisialisasi view yang akan di gunakan
        View view = findViewById(R.id.percobaanubah);
        view.setBackgroundColor(Color.rgb((int)mBlobColorRgba.val[0],
                (int)mBlobColorRgba.val[1],
                (int)mBlobColorRgba.val[2]));

        //Toast.makeText(this, "Berhasil", Toast.LENGTH_SHORT).show();

        return false;
    }

    //Belum tahu ini itu fungsinya buat apa
    private Scalar convertScalarHsv2Rgba(Scalar hsvColor) {
        Mat pointMatRgba = new Mat();
        Mat pointMatHsv = new Mat(1, 1, CvType.CV_8UC3, hsvColor);
        Imgproc.cvtColor(pointMatHsv, pointMatRgba, Imgproc.COLOR_HSV2RGB_FULL, 4);

        //Mengembalikan Scalar yang di gunakan untuk Menentukan koordinat (masih kemungkinan)
        return new Scalar(pointMatRgba.get(0, 0));
    }
}
