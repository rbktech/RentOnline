package ru.rbkdev.rent.search.result.pageradapter.listfarm;
import ru.rbkdev.rent.R;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.View;
import android.content.Intent;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ArrayAdapter;
import android.graphics.BitmapFactory;

import java.util.ArrayList;

public class CViewFarmActivity extends AppCompatActivity {

    float m_proportion = 0.7f;
    SpannableString m_sps;

    String m_town = "Город:\n";
    String m_address = "Адрес:\n";

    String m_owner = "Собственник:\n";
    String m_rating = "Рейтинг:\n";

    String m_number_rooms = "Количество комнат:\n";
    String m_distance = "Растояние до центра:\n";
    String m_geo = "Координаты:\n";
    String m_description = "Описание:\n";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_viewferm);

        final ImageView imgMain = findViewById(R.id.imgMain);
        ImageView imgPhoto0 = findViewById(R.id.imgFerm0);
        ImageView imgPhoto1 = findViewById(R.id.imgFerm1);
        ImageView imgPhoto2 = findViewById(R.id.imgFerm2);
        ImageView imgPhoto3 = findViewById(R.id.imgFerm3);
        ImageView imgPhoto4 = findViewById(R.id.imgFerm4);

        TextView txtTown = findViewById(R.id.txtTownView);
        TextView txtAddress = findViewById(R.id.txtAddressView);
        //TextView txtHouse = findViewById(R.id.txtHouseView);
        TextView txtOwner = findViewById(R.id.txtOwnerView);
        TextView txtRating = findViewById(R.id.txtRatingView);
        TextView txtDistance = findViewById(R.id.txtDistanceView);
        TextView txtNumberRooms = findViewById(R.id.txtNumberRoomsView);
        TextView txtGeo = findViewById(R.id.txtGeoView);
        TextView txtDescription = findViewById(R.id.txtDescriptionView);
        ListView lstComments = findViewById(R.id.lstCommentsView);

        Intent intent = getIntent();

        m_sps = new SpannableString(m_town + intent.getStringExtra("town"));
        m_sps.setSpan(new RelativeSizeSpan(m_proportion), 0, m_town.length(), 0);
        m_sps.setSpan(new ForegroundColorSpan(Color.BLACK), 0, m_town.length(), 0);
        txtTown.setText(m_sps);

        String address =  intent.getStringExtra("street") + ", " + intent.getStringExtra("house");

        m_sps = new SpannableString(m_address + address);
        m_sps.setSpan(new RelativeSizeSpan(m_proportion), 0, m_address.length(), 0);
        m_sps.setSpan(new ForegroundColorSpan(Color.BLACK), 0, m_address.length(), 0);
        txtAddress.setText(m_sps);

        m_sps = new SpannableString(m_owner + intent.getStringExtra("owner"));
        m_sps.setSpan(new RelativeSizeSpan(m_proportion), 0, m_owner.length(), 0);
        m_sps.setSpan(new ForegroundColorSpan(Color.BLACK), 0, m_owner.length(), 0);
        txtOwner.setText(m_sps);

        m_sps = new SpannableString(m_rating + intent.getStringExtra("rating"));
        m_sps.setSpan(new RelativeSizeSpan(m_proportion), 0, m_rating.length(), 0);
        m_sps.setSpan(new ForegroundColorSpan(Color.BLACK), 0, m_rating.length(), 0);
        txtRating.setText(m_sps);

        m_sps = new SpannableString(m_distance + intent.getStringExtra("distance"));
        m_sps.setSpan(new RelativeSizeSpan(m_proportion), 0, m_distance.length(), 0);
        m_sps.setSpan(new ForegroundColorSpan(Color.BLACK), 0, m_distance.length(), 0);
        txtDistance.setText(m_sps);

        m_sps = new SpannableString(m_number_rooms + intent.getStringExtra("number_rooms"));
        m_sps.setSpan(new RelativeSizeSpan(m_proportion), 0, m_number_rooms.length(), 0);
        m_sps.setSpan(new ForegroundColorSpan(Color.BLACK), 0, m_number_rooms.length(), 0);
        txtNumberRooms.setText(m_sps);

        m_sps = new SpannableString(m_geo + intent.getStringExtra("geo"));
        m_sps.setSpan(new RelativeSizeSpan(m_proportion), 0, m_geo.length(), 0);
        m_sps.setSpan(new ForegroundColorSpan(Color.BLACK), 0, m_geo.length(), 0);
        txtGeo.setText(m_sps);

        m_sps = new SpannableString(m_description + intent.getStringExtra("description"));
        m_sps.setSpan(new RelativeSizeSpan(m_proportion), 0, m_description.length(), 0);
        m_sps.setSpan(new ForegroundColorSpan(Color.BLACK), 0, m_description.length(), 0);
        txtDescription.setText(m_sps);

        byte[] tempPhoto;
        int sizePhoto;

        int number_photo = intent.getIntExtra("number_photo", 0);
        for(int i = 0; i < number_photo; i++) {
            tempPhoto = intent.getByteArrayExtra("photo_" + i);
            sizePhoto = tempPhoto.length;
            switch (i) {
                case 0:
                    imgMain.setImageBitmap(BitmapFactory.decodeByteArray(tempPhoto, 0, sizePhoto));
                    imgPhoto0.setImageBitmap(BitmapFactory.decodeByteArray(tempPhoto, 0, sizePhoto));
                    break;
                case 1:
                    imgPhoto1.setImageBitmap(BitmapFactory.decodeByteArray(tempPhoto, 0, sizePhoto));
                    break;
                case 2:
                    imgPhoto2.setImageBitmap(BitmapFactory.decodeByteArray(tempPhoto, 0, sizePhoto));
                    break;
                case 3:
                    imgPhoto3.setImageBitmap(BitmapFactory.decodeByteArray(tempPhoto, 0, sizePhoto));
                    break;
                case 4:
                    imgPhoto4.setImageBitmap(BitmapFactory.decodeByteArray(tempPhoto, 0, sizePhoto));
                    break;
            }
        }

        ArrayList<String> comments = new ArrayList<>();

        int number_comments = intent.getIntExtra("number_comments", 0);
        if (number_comments != 0) {
            for (int i = 0; i < number_comments; i++)
                comments.add(intent.getStringExtra("comments_" + i));
        } else {
            comments.add("Нет комментариев");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, comments);
        lstComments.setAdapter(adapter);

        View.OnClickListener SwitchImage = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imgMain.setImageDrawable(((ImageView)findViewById(v.getId())).getDrawable());
            }
        };

        imgPhoto0.setOnClickListener(SwitchImage);
        imgPhoto1.setOnClickListener(SwitchImage);
        imgPhoto2.setOnClickListener(SwitchImage);
        imgPhoto3.setOnClickListener(SwitchImage);
        imgPhoto4.setOnClickListener(SwitchImage);
    }
}
