package ru.rbkdev.lease.account.login.create;

import ru.rbkdev.lease.main.CID;
import ru.rbkdev.lease.R;
import ru.rbkdev.lease.main.CClient;
import ru.rbkdev.lease.main.CLISTID;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class CCreateActivity extends AppCompatActivity {

    Button m_btnOk;
    TextView m_txtMail;
    TextView m_txtName;
    TextView m_txtSurName;
    TextView m_txtPassword;
    ImageButton m_imgPhoto;

    ByteArrayOutputStream m_imageBuffer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.account_login_create_activity);

        m_btnOk = findViewById(R.id.btnAccountLoginCreateOk);
        m_txtMail = findViewById(R.id.txtAccountLoginCreateMail);
        m_txtName = findViewById(R.id.txtAccountLoginCreateName);
        m_txtSurName = findViewById(R.id.txtAccountLoginCreateSurName);
        m_txtPassword = findViewById(R.id.txtAccountLoginCreatePassword);
        m_imgPhoto =  findViewById(R.id.imgBtnAccountLoginCreateNamePhoto);

        // debug
        m_txtMail.setText("Mail");
        m_txtName.setText("Name");
        m_txtSurName.setText("SurName");
        m_txtPassword.setText("Password");
        // debug

        m_btnOk.setOnClickListener(CommandCreateAccount);
        m_imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                     Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                    photoPickerIntent.setType("image/*");
                    startActivityForResult(photoPickerIntent, CID.GALLERY_REQUEST); // GALLERY_REQUEST
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        int sizeImage;

        if(resultCode == RESULT_OK) {
            if(requestCode == CID.GALLERY_REQUEST) {
                try {
                    Uri uri = intent.getData();
                    if(uri != null) {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        if(inputStream != null) {

                            int len;
                            byte[] buffer = new byte[1024];
                            m_imageBuffer = new ByteArrayOutputStream();

                            while((len = inputStream.read(buffer)) != -1)
                                m_imageBuffer.write(buffer, 0, len);

                            sizeImage = m_imageBuffer.size();
                            if(sizeImage < CLISTID.MAX_SIZE_SEND) {
                                m_imgPhoto.setImageBitmap(BitmapFactory.decodeByteArray(m_imageBuffer.toByteArray(), 0, sizeImage));
                            } else {
                                Toast.makeText(this, "Изображение больше 1 мб", Toast.LENGTH_SHORT).show();
                                m_imageBuffer.reset();
                            }
                        }
                    }
                } catch(IOException | NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    View.OnClickListener CommandCreateAccount = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ArrayList<byte[]> data = new ArrayList<>();
            data.add(m_txtMail.getText().toString().getBytes());
            data.add(m_txtName.getText().toString().getBytes());
            data.add(m_txtSurName.getText().toString().getBytes());
            data.add(m_txtPassword.getText().toString().getBytes());

            /*ByteArrayOutputStream imgArray = new ByteArrayOutputStream();
            Drawable drawable = m_imgPhoto.getDrawable();
            if(drawable instanceof BitmapDrawable) {
                BitmapDrawable bitmapDrawable = (BitmapDrawable)drawable;
                Bitmap bitmap = bitmapDrawable.getBitmap();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, imgArray);
            }
            data.add(imgArray.toByteArray());*/

            if(m_imageBuffer == null) {
                Toast.makeText(v.getContext(), "Необходима фотография", Toast.LENGTH_SHORT).show();
                return;
            } else
                data.add(m_imageBuffer.toByteArray());

            for(byte[] p : data) {
                if(p.length == 0) {
                    Toast.makeText(v.getContext(), "Неправильно введены параметры", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Command "A"
            ArrayList<ArrayList<ByteArrayOutputStream>> bundle = CClient.DataExchange(data, (byte) 0x41, CCreateActivity.this);
            if(bundle != null) {

                Intent intent = new Intent();

                String a = m_txtMail.getText().toString();
                String b = bundle.get(1).get(1).toString();

                if(a.equals(b)) {
                    intent.putExtra(CID.INTENT_USER_MAIL, a);
                    setResult(RESULT_OK, intent);
                } else
                    setResult(RESULT_CANCELED, intent);

                finish();
            }
        }
    };
}
