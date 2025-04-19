package ru.rbkdev.rent.rent;
import ru.rbkdev.rent.R;
import ru.rbkdev.rent.main.CClient;
import ru.rbkdev.rent.main.CMainActivity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.content.Intent;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.View.OnClickListener;

import java.util.Random;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

public class CFarmActivity extends AppCompatActivity {

    TextView m_owner;
    TextView m_town;
    TextView m_streetSave;
    TextView m_houseSave;
    TextView m_geo;
    TextView m_ipHouse;
    TextView m_distanceSave;
    TextView m_numberPersonSave;
    TextView m_numberRoomsSave;
    TextView m_priceSave;
    TextView m_description;

    ArrayList<String> m_foto;
    ByteArrayOutputStream m_buffer;
    ArrayList<ByteArrayOutputStream> m_bufferFoto;

    byte[] m_setImg = new byte[5];
    int m_idImg = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_add_ferm);

        m_foto = new ArrayList<>();
        m_bufferFoto = new ArrayList<>();
        m_buffer = new ByteArrayOutputStream();

        Button btnSave = findViewById(R.id.btnSaveFerm);

        final ArrayList<ImageButton> arrayImgBtn = new ArrayList<>();
        arrayImgBtn.add((ImageButton)findViewById(R.id.imgBtnSaveFerm0));
        arrayImgBtn.add((ImageButton)findViewById(R.id.imgBtnSaveFerm1));
        arrayImgBtn.add((ImageButton)findViewById(R.id.imgBtnSaveFerm2));
        arrayImgBtn.add((ImageButton)findViewById(R.id.imgBtnSaveFerm3));
        arrayImgBtn.add((ImageButton)findViewById(R.id.imgBtnSaveFerm4));

        for(ImageButton p : arrayImgBtn) {
            p.setOnClickListener(SetImageInGallery);
            p.setVisibility(View.INVISIBLE);
        }
        arrayImgBtn.get(0).setVisibility(View.VISIBLE);

        m_owner = findViewById(R.id.txtOwner);
        m_town = findViewById(R.id.txtTown);
        m_streetSave = findViewById(R.id.txtStreetSave);
        m_houseSave = findViewById(R.id.txtHouseSave);
        m_geo = findViewById(R.id.txtGeo);
        m_ipHouse = findViewById(R.id.txtIpHouse);
        m_distanceSave = findViewById(R.id.txtDistanceSave);
        m_numberPersonSave = findViewById(R.id.txtNumberPersonSave);
        m_numberRoomsSave = findViewById(R.id.txtNumberRoomsSave);
        m_priceSave = findViewById(R.id.txtPriceSave);
        m_description = findViewById(R.id.txtDescription);

        // debug
        m_owner.setText("Иванов Иван Иванович");
        m_town.setText("Москва");
        m_streetSave.setText("Петровская");
        m_houseSave.setText("5");
        m_geo.setText("34.4568 55.6723");
        m_ipHouse.setText("192.168.1.72");
        m_distanceSave.setText("12.5");
        m_numberPersonSave.setText("3");
        m_numberRoomsSave.setText("3");
        m_priceSave.setText("10000");
        m_description.setText("Самая лучша квартира в мире");
        // debug

        btnSave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                Random random = new Random();

                ArrayList<byte[]> data = new ArrayList<>();
                data.add(CMainActivity.g_userId.getBytes());
                data.add("111".getBytes());
                data.add(m_numberPersonSave.getText().toString().getBytes());
                data.add(m_streetSave.getText().toString().getBytes());
                data.add(m_houseSave.getText().toString().getBytes());
                data.add((random.nextInt(10) + "." + random.nextInt(10)).getBytes());
                data.add(m_distanceSave.getText().toString().getBytes());
                data.add(m_numberRoomsSave.getText().toString().getBytes());
                data.add(m_priceSave.getText().toString().getBytes());
                data.add(m_owner.getText().toString().getBytes());
                data.add(m_geo.getText().toString().getBytes());
                data.add(m_description.getText().toString().getBytes());
                data.add(m_ipHouse.getText().toString().getBytes());

                ArrayList<byte[]> arrayBytePhoto = new ArrayList<>();
                int i = 0;
                for(ImageButton p : arrayImgBtn) {
                    if(m_setImg[i++] == 1) {
                        ByteArrayOutputStream temp = new ByteArrayOutputStream();
                        Bitmap bitmap = ((BitmapDrawable) p.getDrawable()).getBitmap();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, temp);
                        arrayBytePhoto.add(temp.toByteArray());
                    }
                }

                data.add(new byte[]{ (byte)arrayBytePhoto.size() });
                data.addAll(arrayBytePhoto);

                ByteArrayOutputStream message = CClient.CreateMessage(data, (byte)0x53); // "S"
                Intent intent = new Intent();
                intent.putExtra("message", message.toByteArray());
                setResult(1889, intent);
                finish();
            }
        });
    }

    OnClickListener SetImageInGallery = new OnClickListener() {
        @Override
        public void onClick(View v) {
            int index = -1;
            m_idImg = v.getId();
            switch (m_idImg) {
                case (int)R.id.imgBtnSaveFerm0:
                    index = 0;
                    break;
                case (int)R.id.imgBtnSaveFerm1:
                    index = 1;
                    break;
                case (int)R.id.imgBtnSaveFerm2:
                    index = 2;
                    break;
                case (int)R.id.imgBtnSaveFerm3:
                    index = 3;
                    break;
                case (int)R.id.imgBtnSaveFerm4:
                    index = 4;
                    break;
                default:
            }

            if(index != -1) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, index); // GALLERY_REQUEST
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent); // requestCode == GALLERY_REQUEST

        int visible = View.INVISIBLE;
        byte established = 0;

        if(resultCode == RESULT_OK) {
            Uri uri = imageReturnedIntent.getData();
            if (uri != null) {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    if(inputStream != null) {

                        int len;
                        byte[] buffer = new byte[1024];
                        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();

                        while((len = inputStream.read(buffer)) != -1)
                            byteBuffer.write(buffer, 0, len);

                        ImageButton img = findViewById(m_idImg);
                        img.setImageBitmap(BitmapFactory.decodeByteArray(byteBuffer.toByteArray(), 0, byteBuffer.size()));

                        visible = View.VISIBLE;
                        established = 1;
                    }
                } catch (IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        } else
            ((ImageButton)findViewById(m_idImg)).setImageResource(R.mipmap.baseline_add_black_48);

        m_setImg[requestCode] = established;

        switch(m_idImg) {
            case (int)R.id.imgBtnSaveFerm0:
                findViewById(R.id.imgBtnSaveFerm1).setVisibility(visible);
                break;
            case (int)R.id.imgBtnSaveFerm1:
                findViewById(R.id.imgBtnSaveFerm2).setVisibility(visible);
                break;
            case (int)R.id.imgBtnSaveFerm2:
                findViewById(R.id.imgBtnSaveFerm3).setVisibility(visible);
                break;
            case (int)R.id.imgBtnSaveFerm3:
                findViewById(R.id.imgBtnSaveFerm4).setVisibility(visible);
                break;
        }
    }
}